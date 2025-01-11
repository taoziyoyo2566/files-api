package com.taoziyoyo.files.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MediaMetadata {
    private String format;
    private Long duration;
    private String codec;
    private Integer width;
    private Integer height;
    private Integer bitrate;
    private String resolution;
}
