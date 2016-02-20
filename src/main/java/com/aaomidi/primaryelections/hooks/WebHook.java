package com.aaomidi.primaryelections.hooks;

import com.aaomidi.primaryelections.PrimaryElections;
import com.aaomidi.primaryelections.model.Candidate;
import com.aaomidi.primaryelections.model.Party;
import com.aaomidi.primaryelections.util.Log;
import com.aaomidi.primaryelections.util.NumberTools;
import com.jaunt.Document;
import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.UserAgent;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * Created by amir on 2016-02-20.
 */
public class WebHook {
    private final PrimaryElections instance;
    @Getter
    private ReentrantLock lock = new ReentrantLock(true);
    @Getter
    private HashMap<Party, HashMap<String, Candidate>> candidates = new HashMap<>();
    @Getter
    @Setter
    private volatile boolean changesMade = false;
    @Getter
    private HashMap<Party, Float> precinctsReporting = new HashMap<>();

    public WebHook(PrimaryElections instance) {
        this.instance = instance;

        this.setupRunnable();
    }

    public void setupRunnable() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                new Thread(() -> {
                    setupResults();
                }).start();
            }
        };

        timer.schedule(task, 0, 5000);
    }

    public void setupResults() {
        UserAgent userAgent;
        try {
            lock.lock();
            userAgent = new UserAgent();
            userAgent.visit("http://www.decisiondeskhq.com/nevada-democratic-caucus/");
            setup(Party.DEMOCRAT, userAgent.doc);

            userAgent.visit("http://www.decisiondeskhq.com/south-carolina-gop-primary/");
            setup(Party.REPUBLICAN, userAgent.doc);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private void setup(Party party, Document doc) throws Exception {
        Elements elements = doc.findEvery("<div class=\"totals-row\">");
        for (Element element : elements) {

            Element nameElement = element.findFirst("<div class=\"square-name\">");
            Elements voteInfo = element.findEvery("<div class=\"four-blocks2\">");

            Iterator<Element> iterator = voteInfo.iterator();

            if (elements.size() < 3) {
                Log.log(Level.SEVERE, "Not enough info!");
                continue;
            }

            Element votePercentElm = iterator.next();
            Element votesElm = iterator.next();
            Element votesBehindElm = iterator.next();

            String name = nameElement.getText();
            float votePercent = Float.valueOf(votePercentElm.getText().replace("%", ""));
            Integer votes = NumberTools.getInteger(votesElm.getText().replace(",", ""));
            Integer votesBehind = NumberTools.getInteger(votesBehindElm.getText().replace(",", ""));

            if (votes == null)
                votes = 0;

            if (votesBehind == null)
                votesBehind = 0;

            Candidate candidate = new Candidate(name, votePercent, votes, votesBehind, party);
            setup(candidate);
        }
        if (changesMade) {
            Element element = doc.findFirst("<div class=\"line-totals\">").findFirst("<div class=\"four-blocks2\">");
            float reporting = Float.valueOf(element.getText().replace("%", ""));
            precinctsReporting.put(party, reporting);
        }
    }

    private void setup(Candidate candidate) {
        HashMap<String, Candidate> map = candidates.getOrDefault(candidate.getParty(), new HashMap<>());
        Candidate oldCandidate = map.get(candidate.getName());
        if (oldCandidate != null) {
            if (!candidate.hasChanged(oldCandidate)) {
                return;
            }
        }
        map.put(candidate.getName(), candidate);
        candidates.put(candidate.getParty(), map);
        changesMade = true;
    }
}
