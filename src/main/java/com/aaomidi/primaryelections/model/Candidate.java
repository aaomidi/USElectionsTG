package com.aaomidi.primaryelections.model;

import com.aaomidi.primaryelections.model.helper.Party;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by amir on 2016-02-20.
 */
@RequiredArgsConstructor
public class Candidate implements Comparable<Candidate> {
    @Getter
    private final String name;
    @Getter
    private final float percent;
    @Getter
    private final int votes;
    @Getter
    private final int votesBehind;
    @Getter
    private final Party party;


    public String getCandidateInfo() {
        return String.format("\n*%s*(*%.2f%%*)\n_Votes:_ %d\n_Votes Behind:_ %d\n",
                name,
                percent,
                votes,
                votesBehind);
    }

    public boolean hasChanged(Candidate candidate) {
        return candidate.getVotes() != this.getVotes();
    }

    @Override
    public int compareTo(Candidate o) {
        return ((Integer) votes).compareTo(o.getVotes()) * -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Candidate)) return false;

        Candidate candidate = (Candidate) o;

        return name.equals(candidate.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
