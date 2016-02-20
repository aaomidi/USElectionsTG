package com.aaomidi.primaryelections.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by amir on 2016-02-20.
 */
@RequiredArgsConstructor
public enum Party {
    DEMOCRAT("Democrat"),
    REPUBLICAN("Republican"),
    OTHER("Other");
    @Getter
    private final String partyName;
}
