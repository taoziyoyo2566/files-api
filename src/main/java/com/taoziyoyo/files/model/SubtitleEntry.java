package com.taoziyoyo.files.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubtitleEntry {
    private final int sequenceNumber;
    private final String startTime;
    private final String endTime;
    private final String text;

}
