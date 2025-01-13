package com.taoziyoyo.files.utils;

import com.taoziyoyo.files.config.MediaProperties;
import com.taoziyoyo.files.exception.MediaException;
import com.taoziyoyo.files.model.SubtitleEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SrtSubtitleReader {
    private final MediaProperties mediaProperties;
    private final ResourceLoader resourceLoader;
    private static final Logger logger = LoggerFactory.getLogger(SrtSubtitleReader.class);

    public SrtSubtitleReader(MediaProperties mediaProperties, ResourceLoader resourceLoader) {
        this.mediaProperties = mediaProperties;
        this.resourceLoader = resourceLoader;
    }

    public List<SubtitleEntry> getSubtitle(String filename) throws IOException {
        Path subtitlePath = Paths.get(mediaProperties.getRootPath()+"/subtitle", filename);
        logger.info("subtitlePath: {}", subtitlePath);
        validateSubtitleFile(subtitlePath, filename);


        List<SubtitleEntry> subtitleEntries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(subtitlePath), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Read sequence number
                int sequenceNumber = Integer.parseInt(line.trim());

                // Read timestamp line
                String timestamp = reader.readLine();
                String[] times = timestamp.split(" --> ");
                String startTime = times[0].trim();
                String endTime = times[1].trim();

                // Read subtitle text (may be multiple lines)
                StringBuilder subtitleText = new StringBuilder();
                while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                    if (subtitleText.length() > 0) {
                        subtitleText.append("\n");
                    }
                    subtitleText.append(line.trim());
                }

                subtitleEntries.add(new SubtitleEntry(
                        sequenceNumber,
                        startTime,
                        endTime,
                        subtitleText.toString()
                ));
            }
        }

        return subtitleEntries;
    }

    private void validateSubtitleFile(Path subtitlePath, String filename) {
        if (!Files.exists(subtitlePath)) {
            throw new MediaException("Subtitle file not found: " + filename);
        }

        String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase();
        if (!".srt".equals(extension)) {
            throw new MediaException("Invalid subtitle format. Only .srt files are supported");
        }
    }
}
