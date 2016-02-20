package com.aaomidi.primaryelections.model;

import lombok.Data;

/**
 * Created by amir on 2016-02-20.
 */
@Data
public class Candidate {
    private final String name;
    private final float percent;
    private final int votes;
    private final int votesBehind;
    private final Party party;


    public String getCandidateInfo() {
        return String.format("Name: %s\n\tVote Percent: %.2f%%\n\tVotes: %d\n\tVotes Behind: %d\n",
                name,
                percent,
                votes,
                votesBehind);
    }

    public boolean hasChanged(Candidate candidate) {
        return candidate.getVotes() != this.getVotes();
    }
}
