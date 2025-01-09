package com.taoziyoyo.files.service;
import com.taoziyoyo.files.model.MediaFile;
import com.taoziyoyo.files.utils.MediaScanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaService {

    private final MediaScanner mediaScanner;

    public MediaService(@Value("${media.base-dir}") String baseDir) {
        this.mediaScanner = new MediaScanner(baseDir);
    }

    public List<MediaFile> getMediaFiles() {
        try {
            return mediaScanner.scanMediaFiles();
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan media files", e);
        }
    }
}