package com.aaomidi.primaryelections.hooks;

import com.aaomidi.primaryelections.PrimaryElections;
import com.aaomidi.primaryelections.model.Race;
import com.aaomidi.primaryelections.util.Log;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.Chat;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.user.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

/**
 * Created by amir on 2016-02-20.
 */
public class TelegramHook {
    private final PrimaryElections instance;
    @Getter
    private final TelegramBot bot;
    @Getter
    private final Chat channel;
    private final Map<Race, Message> cachedMessages = new HashMap<>();

    public TelegramHook(PrimaryElections instance, String auth) {
        User user;
        this.instance = instance;

        this.bot = TelegramBot.login(auth);
        bot.startUpdates(false);

        this.channel = bot.getChat("@USElections");
        //this.channel = TelegramBot.getChat("55395012");
        //this.channel = TelegramBot.getChat("-104142561");
        //this.channel = bot.getChat("-14978569");
        //this.channel = bot.getChat("-1001033454849");

        this.setupRunnable();
    }

    public void setupRunnable() {
        WebHook webHook = instance.getWebHook();
        try {
            webHook.getLock().lock();
            sendInitialMessage();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            webHook.getLock().unlock();
        }

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

        timer.schedule(task, 5000, 5000);
    }

    public void sendInitialMessage() {
        WebHook webHook = instance.getWebHook();
        for (Race race : webHook.getRaces()) {
            String message = race.getInitialMessage();
            SendableTextMessage msg = SendableTextMessage
                    .builder()
                    .disableNotification(true)
                    .parseMode(ParseMode.MARKDOWN)
                    .message(message)
                    .build();

            Message m = channel.sendMessage(msg);
            cachedMessages.put(race, m);
        }
    }

    public void sendResults() {
        WebHook webHook = instance.getWebHook();
        try {
            webHook.getLock().lock();
            if (!webHook.shouldReport()) {
                return;
            }
            /*SendableTextMessage intro =
                    SendableTextMessage.builder().message("\uD83D\uDD14\uD83D\uDD14\uD83D\uDD14 *New results incoming!* \uD83D\uDD14\uD83D\uDD14\uD83D\uDD14").parseMode(ParseMode.MARKDOWN).disableWebPagePreview(true).disableNotification(true).build();

            channel.sendMessage(intro);
            Thread.sleep(200); */

            for (Race race : webHook.getRaces()) {
                String result = race.getResults();
                if (!race.isChangesMade())
                    continue;

                if (result == null)
                    continue;

                race.setChangesMade(false);
                Log.log(Level.INFO, String.format("Reporting %s, %s right now", race.getParty(), race.getState()));

                //bot.editMessageText(cachedMessages.get(race), result, ParseMode.MARKDOWN, true, null);
                bot.editMessageText("@USElections", cachedMessages.get(race).getMessageId(), result, ParseMode.MARKDOWN, true, null);

                Thread.sleep(1000);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            webHook.getLock().unlock();
        }
    }
}


