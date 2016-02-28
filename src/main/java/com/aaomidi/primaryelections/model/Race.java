package com.aaomidi.primaryelections.model;

import com.aaomidi.primaryelections.model.helper.Party;
import com.aaomidi.primaryelections.model.helper.RaceType;
import com.aaomidi.primaryelections.model.helper.State;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by amir on 2016-02-28.
 */
@Data
public class Race {
    private final State state;
    private final Party party;
    private final RaceType raceType;
    private final String url;
    private HashMap<String, Candidate> candidates;
    private boolean changesMade = true;
    private float reportingPercent = 0;

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
