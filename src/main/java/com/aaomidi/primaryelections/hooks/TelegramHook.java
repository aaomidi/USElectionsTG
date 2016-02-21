package com.aaomidi.primaryelections.hooks;

import com.aaomidi.primaryelections.PrimaryElections;
import com.aaomidi.primaryelections.model.Candidate;
import com.aaomidi.primaryelections.model.Party;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.Chat;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by amir on 2016-02-20.
 */
public class TelegramHook {
    private final PrimaryElections instance;
    @Getter
    private final TelegramBot bot;
    @Getter
    private final Chat channel;

    public TelegramHook(PrimaryElections instance, String auth) {
        this.instance = instance;

        this.bot = TelegramBot.login(auth);
        bot.startUpdates(false);

        this.channel = TelegramBot.getChat("@USElections");
        this.setupRunnable();
    }

    public void setupRunnable() {
        WebHook webHook = instance.getWebHook();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    webHook.getLock().lock();
                    if (!webHook.isChangesMade()) {
                        return;
                    }
                    webHook.setChangesMade(false);
                    channel.sendMessage("\uD83D\uDD14\uD83D\uDD14\uD83D\uDD14 New results incoming! \uD83D\uDD14\uD83D\uDD14\uD83D\uDD14", bot);
                    Thread.sleep(5000);
                    for (Set<Candidate> set : webHook.getSortedCandidates().values()) {
                        StringBuilder sb = new StringBuilder();
                        Candidate randomCandidate = null;
                        for (Candidate candidate : set) {
                            sb.append(candidate.getCandidateInfo());
                            randomCandidate = candidate;
                        }

                        if (randomCandidate == null) {
                            throw new Error("No candidates?");
                        }
                        Party party = randomCandidate.getParty();
                        if (webHook.getPrecinctsReporting().get(party) < 0.1) {
                            return;
                        }
                        if (party == Party.DEMOCRAT) {
                            sb.insert(0, String.format("\uD83D\uDC34 %s Caucus from Nevada:\n", randomCandidate.getParty().getPartyName()));
                        } else {
                            sb.insert(0, String.format("\uD83D\uDC18 %s Primary from South Carolina:\n", randomCandidate.getParty().getPartyName()));
                        }

                        sb.append(String.format("Precincts Reporting: %.2f%%", webHook.getPrecinctsReporting().get(party)));
                        sb.append("\nStay up to date with @USElections!");
                        String msg = sb.toString();
                        channel.sendMessage(msg, bot);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    webHook.getLock().unlock();
                }
            }
        };

        timer.schedule(task, 5000, 60000);
    }

}
