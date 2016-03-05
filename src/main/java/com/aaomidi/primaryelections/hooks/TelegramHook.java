package com.aaomidi.primaryelections.hooks;

import com.aaomidi.primaryelections.PrimaryElections;
import com.aaomidi.primaryelections.model.Race;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.Chat;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.user.User;

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
        User user;
        this.instance = instance;

        this.bot = TelegramBot.login(auth);
        bot.startUpdates(false);

        this.channel = TelegramBot.getChat("@USElections");
        //this.channel = TelegramBot.getChat("55395012");
        //this.channel = TelegramBot.getChat("-104142561");
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
                    sendResults();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    webHook.getLock().unlock();
                }
            }
        };

        timer.schedule(task, 5000, 60000);
    }

    public void sendResults() {
        WebHook webHook = instance.getWebHook();
        try {
            webHook.getLock().lock();
            if (!webHook.shouldReport()) {
                return;
            }
            SendableTextMessage intro =
                    SendableTextMessage.builder().message("\uD83D\uDD14\uD83D\uDD14\uD83D\uDD14 *New results incoming!* \uD83D\uDD14\uD83D\uDD14\uD83D\uDD14").parseMode(ParseMode.MARKDOWN).disableWebPagePreview(true).disableNotification(true).build();

            channel.sendMessage(intro, bot);
            Thread.sleep(200);

            for (Race race : webHook.getRaces()) {
                String result = race.getResults();
                if (!race.isChangesMade())
                    continue;

                if (result == null)
                    continue;

                if (race.getReportingPercent() < 0.1) {
                    continue;
                }

                if (race.getReportingPercent() > 95) {
                    continue;
                }
                race.setChangesMade(false);
                SendableTextMessage message =
                        SendableTextMessage.builder().message(result).parseMode(ParseMode.MARKDOWN).disableNotification(true).disableWebPagePreview(true).build();

                channel.sendMessage(message, bot);
                Thread.sleep(1000);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            webHook.getLock().unlock();
        }
    }
}


