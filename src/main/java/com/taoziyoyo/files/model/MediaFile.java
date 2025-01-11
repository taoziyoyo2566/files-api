package com.taoziyoyo.files.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MediaFile {
    private String id;
    private String filename;
    private String path;
    private String type;
    private long size;
    private long lastModified;
    private String subtitlePath;
    private String relativePath;
    private MediaMetadata metadata;
    private Boolean hasSubtitle;
}
