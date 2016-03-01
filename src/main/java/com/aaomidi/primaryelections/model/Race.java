package com.aaomidi.primaryelections.model;

import com.aaomidi.primaryelections.model.helper.Party;
import com.aaomidi.primaryelections.model.helper.RaceType;
import com.aaomidi.primaryelections.model.helper.State;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by amir on 2016-02-28.
 */
public class Race {
    @Getter
    private final State state;
    @Getter
    private final Party party;
    @Getter
    private final RaceType raceType;
    @Getter
    private final String url;
    @Getter
    @Setter
    private HashMap<String, Candidate> candidates;
    @Getter
    @Setter
    private boolean changesMade = true;
    @Getter
    @Setter
    private float reportingPercent = 0;

    public Race(State state, Party party, RaceType raceType, String url) {
        this.state = state;
        this.party = party;
        this.raceType = raceType;
        if (url.equals("")) {
            switch (party) {
                case DEMOCRAT:
                    this.url = "http://www.decisiondeskhq.com/results/2016/primary/dem/president/" + state.getName().toLowerCase().replace(" ", "-") + "/";
                    break;
                case REPUBLICAN:
                    this.url = "http://www.decisiondeskhq.com/results/2016/primary/gop/president/" + state.getName().toLowerCase().replace(" ", "-") + "/";
                    break;
                default:
                    this.url = url;
            }
        } else {
            this.url = url;
        }
    }

    public boolean hasChanged(List<Candidate> list) {
        if (candidates == null) {
            return true;
        }
        for (Candidate c : list) {
            Candidate candidate = candidates.get(c.getName());
            if (candidate == null) {
                return true;
            }
            if (candidate.getVotes() != c.getVotes()) {
                return true;
            }
        }
        return false;
    }

    public void updateCandidates(List<Candidate> list) {
        changesMade = true;
        candidates = new HashMap<>();
        for (Candidate c : list) {
            candidates.put(c.getName(), c);
        }
    }

    public List<Candidate> getSortedCandidates() {
        if (candidates == null) {
            return new ArrayList<>();
        }
        List<Candidate> list = candidates.values().stream().collect(Collectors.toList());
        Collections.sort(list);
        return list;
    }

    public String getResults() {
        List<Candidate> list = getSortedCandidates();
        if (list == null) return null;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("*%s %s Primary from %s:*\n", party.getEmoji(), party.getPartyName(), state.getName()));
        for (Candidate candidate : list) {
            sb.append(candidate.getCandidateInfo());
        }
        sb.append(String.format("\n*Precincts Reporting: %.2f%%*", getReportingPercent()));
        sb.append("\n*Stay up to date with* @USElections*!*");
        return sb.toString();
    }
}
