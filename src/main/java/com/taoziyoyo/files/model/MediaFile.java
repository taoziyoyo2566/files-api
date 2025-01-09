package com.taoziyoyo.files.model;

import lombok.Data;

@Data
public class MediaFile {
    private String filename;
    private String path;
    private String type;
    private long size;
    private long lastModified;
    private double duration; // 时长，单位：秒
    private String codec;

    public MediaFile(String filename, String path, String type, long size, long lastModified, double duration, String codec) {
        this.filename = filename;
        this.path = path;
        this.type = type;
        this.size = size;
        this.lastModified = lastModified;
        this.duration = duration;
        this.codec = codec;
    }
}
