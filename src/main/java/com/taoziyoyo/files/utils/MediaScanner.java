package com.taoziyoyo.files.utils;

import com.taoziyoyo.files.model.MediaFile;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class MediaScanner {

    private final String baseDir;
    private final Tika tika;

    public MediaScanner(String baseDir) {
        this.baseDir = baseDir;
        this.tika = new Tika();
    }

    public List<MediaFile> scanMediaFiles() throws Exception {
        List<MediaFile> mediaFiles = new ArrayList<>();
        File directory = new File(baseDir);

        if (!directory.exists() || !directory.isDirectory()) {
            throw new RuntimeException("Invalid media base directory: " + baseDir);
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    MediaFile mediaFile = extractMetadata(file);
                    if (mediaFile != null) {
                        mediaFiles.add(mediaFile);
                    }
                }
            }
        }

        return mediaFiles;
    }

    private MediaFile extractMetadata(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(inputStream, new BodyContentHandler(), metadata, new ParseContext());

            String fileType = tika.detect(file);
            String duration = metadata.get("xmpDM:duration");
            String codec = metadata.get("xmpDM:audioCompressor");
            long fileSize = file.length();
            long lastModified = file.lastModified();

            return new MediaFile(
                    file.getName(),
                    file.getAbsolutePath(),
                    fileType,
                    fileSize,
                    lastModified,
                    duration != null ? Double.parseDouble(duration) : 0,
                    codec != null ? codec : "unknown"
            );
        } catch (Exception e) {
            System.err.println("Error extracting metadata for file: " + file.getName() + " - " + e.getMessage());
            return null;
        }
    }
}