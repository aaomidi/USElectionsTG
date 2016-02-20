package com.aaomidi.primaryelections;

import com.aaomidi.primaryelections.hooks.TelegramHook;
import com.aaomidi.primaryelections.hooks.WebHook;
import com.aaomidi.primaryelections.util.Log;
import lombok.Getter;

import java.util.logging.Level;

/**
 * Created by amir on 2016-02-20.
 */
public class PrimaryElections {
    private TelegramHook telegramHook;
    @Getter
    private WebHook webHook;

    public PrimaryElections(String... args) {
        if (args.length == 0) {
            Log.log(Level.SEVERE, "No telegram key specified. Shutting down.");
            System.exit(0);
            return;
        }

        this.setupWebhooks();
        this.setupTelegram(args[0]);
    }

    public static void main(String... args) {
        new PrimaryElections(args);
    }

    private void setupTelegram(String key) {
        Log.log(Level.INFO, "Starting telegram...");
        telegramHook = new TelegramHook(this, key);
        Log.log(Level.INFO, "\tConnected.");
    }

    private void setupWebhooks() {
        Log.log(Level.INFO, "Creating webhooks...");
        webHook = new WebHook(this);
        Log.log(Level.INFO, "\tCreated");
    }


}
