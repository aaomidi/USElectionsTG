package com.aaomidi.primaryelections.hooks;

import com.aaomidi.primaryelections.PrimaryElections;
import com.aaomidi.primaryelections.model.Candidate;
import com.aaomidi.primaryelections.model.Race;
import com.aaomidi.primaryelections.model.helper.Party;
import com.aaomidi.primaryelections.model.helper.RaceType;
import com.aaomidi.primaryelections.model.helper.State;
import com.aaomidi.primaryelections.util.Log;
import com.aaomidi.primaryelections.util.NumberTools;
import com.jaunt.Document;
import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.UserAgent;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * Created by amir on 2016-02-20.
 */
public class WebHook {
    private final PrimaryElections instance;
    @Getter
    private final List<Race> races = new ArrayList<>();
    @Getter
    private ReentrantLock lock = new ReentrantLock(true);
    @Getter
    private HashMap<Party, Map<String, Candidate>> candidates = new HashMap<>();
    @Getter
    private HashMap<Party, List<Candidate>> sortedCandidates = new HashMap<>();
    @Getter
    @Setter
    private volatile boolean changesMade = true;
    @Getter
    private HashMap<Party, Float> precinctsReporting = new HashMap<>();

    public WebHook(PrimaryElections instance) {
        this.instance = instance;
        races.add(new Race(State.SOUTH_CAROLINA, Party.DEMOCRAT, RaceType.PRIMARY, "http://www.decisiondeskhq.com/south-carolina-democratic-primary/"));

        this.setupRunnable();
    }

    public void setupRunnable() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                new Thread(() -> {
                    //setupResults();
                    setupRaces();
                }).start();
            }
        };

        timer.schedule(task, 0, 5000);
    }

    public void setupRaces() {
        try {
            lock.lock();
            for (Race race : races) {
                try {
                    UserAgent userAgent = new UserAgent();
                    userAgent.visit(race.getUrl());
                    setupRace(race, userAgent.doc);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void setupRace(Race race, Document doc) throws Exception {
        List<Candidate> candidates = new ArrayList<>();
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

            Candidate candidate = new Candidate(name, votePercent, votes, votesBehind, race.getParty());
            candidates.add(candidate);
        }
        if (race.hasChanged(candidates)) {
            race.updateCandidates(candidates);
        }
        Element element = doc.findFirst("<div class=\"line-totals\">").findFirst("<div class=\"four-blocks2\">");
        float reporting = Float.valueOf(element.getText().replace("%", ""));

        race.setReportingPercent(reporting);
    }


    public boolean shouldReport() {
        for (Race race : races) {
            if (race.isChangesMade())
                return true;
        }
        for (Race race : races) {
            if (race.getReportingPercent() > 0.1) {
                return true;
            }
        }
        return false;
    }
}
