package com.taoziyoyo.files.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Subtitle {
    private Long id;
    private Integer seek;
    private Double start;
    private Double end;
    private String text;
    private List<Integer> tokens;
    private Double avgLogprob;
    private Double compressionRatio;
    private Double noSpeechProb;
    private Object words;
    private Double temperature;
}
