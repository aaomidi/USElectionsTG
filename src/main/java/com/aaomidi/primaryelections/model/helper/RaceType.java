package com.aaomidi.primaryelections.model.helper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by amir on 2016-02-28.
 */
@RequiredArgsConstructor
public enum RaceType {
    PRIMARY("Primary"),
    CAUCUS("Caucus");
    @Getter
    private final String typeName;
}
