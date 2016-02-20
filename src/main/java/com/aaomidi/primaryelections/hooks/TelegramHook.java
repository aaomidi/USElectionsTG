package com.aaomidi.primaryelections.hooks;

import com.aaomidi.primaryelections.PrimaryElections;
import com.aaomidi.primaryelections.model.Candidate;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.Chat;

import java.util.HashMap;
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
                    for (HashMap<String, Candidate> map : webHook.getCandidates().values()) {
                        StringBuilder sb = new StringBuilder();
                        Candidate randomCandidate = null;
                        for (Candidate candidate : map.values()) {
                            sb.append(candidate.getCandidateInfo());
                            randomCandidate = candidate;
                        }
                        if (randomCandidate == null) {
                            throw new Error("No candidates?");
                        }
                        sb.insert(0, String.format("%s results:\n", randomCandidate.getParty().getPartyName()));
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

        timer.schedule(task, 2000, 15000);
    }

}