package com.taoziyoyo.files.service;

import com.taoziyoyo.files.config.MediaProperties;
import com.taoziyoyo.files.exception.MediaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class SubtitleService {
    private static final Logger logger = LoggerFactory.getLogger(SubtitleService.class);

    private final ResourceLoader resourceLoader;
    private final MediaProperties mediaProperties;

    public SubtitleService(ResourceLoader resourceLoader, MediaProperties mediaProperties) {
        this.resourceLoader = resourceLoader;
        this.mediaProperties = mediaProperties;
    }

    /**
     * Find subtitle file path for a given media file
     * @param mediaPath The path of the media file
     * @param rootDir The root directory path
     * @return Optional containing the relative subtitle path if found, empty otherwise
     */
    public Optional<String> findSubtitlePath(Path mediaPath, Path rootDir) {
        try {
            // 1. First check in the same directory
            Optional<String> sameDir = findSubtitleInDirectory(mediaPath.getParent(), getBaseFileName(mediaPath), rootDir);
            if (sameDir.isPresent()) {
                return sameDir;
            }

            logger.info("mediaPath.getParent(): {}",mediaPath.getParent());
            // 2. Check in a "subtitle" subdirectory if it exists
            Path subtitleDir = mediaPath.getParent().resolve("subtitle");
            if (Files.exists(subtitleDir) && Files.isDirectory(subtitleDir)) {
                Optional<String> inSubtitleDir = findSubtitleInDirectory(subtitleDir, getBaseFileName(mediaPath), rootDir);
                if (inSubtitleDir.isPresent()) {
                    return inSubtitleDir;
                }
            }

            // 3. Check in root/subtitle directory
            Path rootSubtitleDir = rootDir.resolve("subtitle");
            if (Files.exists(rootSubtitleDir) && Files.isDirectory(rootSubtitleDir)) {
                return findSubtitleInDirectory(rootSubtitleDir, getBaseFileName(mediaPath), rootDir);
            }

            return Optional.empty();
        } catch (IOException e) {
            logger.warn("Error finding subtitle for: {}", mediaPath, e);
            return Optional.empty();
        }
    }

    private String getBaseFileName(Path mediaPath) {
        String filename = mediaPath.getFileName().toString();
        // Handle filenames with multiple dots
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(0, lastDotIndex) : filename;
    }

    private Optional<String> findSubtitleInDirectory(Path directory, String baseFileName, Path rootDir) throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        String baseName = baseFileName.toLowerCase();
                        // Check if the subtitle file starts with the media file name
                        // Also check for exact match without extension
                        return (name.startsWith(baseName) || getBaseFileName(path).equalsIgnoreCase(baseFileName)) &&
                                mediaProperties.getSupportedSubtitleTypes().stream()
                                        .anyMatch(name::endsWith);
                    })
                    .findFirst()
                    .map(path -> "/api/media/subtitle/" + rootDir.relativize(path));
        }
    }

    /**
     * Get subtitle file as a Resource
     * @param filename The relative path of the subtitle file
     * @return Resource containing the subtitle file
     * @throws IOException if the file cannot be accessed
     */
    public Resource getSubtitle(String filename) throws IOException {
        Path subtitlePath = Paths.get(mediaProperties.getRootPath(), filename);

        if (!Files.exists(subtitlePath)) {
            throw new MediaException("Subtitle file not found: " + filename);
        }

        String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase();
        if (!mediaProperties.getSupportedSubtitleTypes().contains(extension)) {
            throw new MediaException("Unsupported subtitle format: " + extension);
        }

        return resourceLoader.getResource("file:" + subtitlePath);
    }

    /**
     * Check if a subtitle file exists for the given media file
     * @param mediaPath The path of the media file
     * @param rootDir The root directory path
     * @return true if a subtitle file exists, false otherwise
     */
    public boolean hasSubtitleFile(Path mediaPath, Path rootDir) {
        return findSubtitlePath(mediaPath, rootDir).isPresent();
    }
}