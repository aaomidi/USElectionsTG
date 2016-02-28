package com.aaomidi.primaryelections.model.helper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by amir on 2016-02-20.
 */
@RequiredArgsConstructor
public enum Party {
    DEMOCRAT("Democrat", "\uD83D\uDC34"),
    REPUBLICAN("Republican", "\uD83D\uDC18"),
    OTHER("Other", "");
    @Getter
    private final String partyName;
    @Getter
    private final String emoji;
}
