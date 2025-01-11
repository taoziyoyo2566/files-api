package com.taoziyoyo.files.service;

import com.taoziyoyo.files.config.MediaProperties;
import com.taoziyoyo.files.exception.MediaException;
import com.taoziyoyo.files.model.MediaFile;
import com.taoziyoyo.files.model.MediaTypeUtils;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpRange;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.nio.file.attribute.*;
import org.slf4j.Logger;

@Service
public class MediaService {
    private static final Logger logger = LoggerFactory.getLogger(MediaService.class);
    private final ResourceLoader resourceLoader;
    private  final SubtitleService subtitleService;
    private final MediaProperties mediaProperties;

    public MediaService(ResourceLoader resourceLoader, SubtitleService subtitleService, MediaProperties mediaProperties) {
        this.resourceLoader = resourceLoader;
        this.subtitleService = subtitleService;
        this.mediaProperties = mediaProperties;
    }

    public List<MediaFile> getMediaFiles() {
        logger.debug("mediaProperties: {}", mediaProperties);
        Path rootDir = Paths.get(mediaProperties.getRootPath());
        List<MediaFile> mediaFiles = new ArrayList<>();
        logger.debug("rootDir: {}", rootDir);
        try (Stream<Path> paths = Files.walk(rootDir, mediaProperties.getMaxDepth())) {
            paths.filter(Files::isRegularFile)
                    .filter(MediaTypeUtils::isMediaFile)
                    .forEach(path -> {
                        try {
                            MediaFile mediaFile = createMediaFileObject(path, rootDir);
                            mediaFiles.add(mediaFile);
                        } catch (Exception e) {
                            logger.error("Error processing file: " + path, e);
                        }
                    });
        } catch (IOException e) {
            throw new MediaException("Failed to scan media files", e);
        }
        return mediaFiles;
    }

    private MediaFile createMediaFileObject(Path path, Path rootDir) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        String filename = path.getFileName().toString();
        String relativePath = rootDir.relativize(path).toString();
        logger.debug("relativePath: {}",relativePath);
        return MediaFile.builder()
                .id(relativePath)
                .filename(filename)
                .path("/api/media/stream/" + relativePath)
                .type(MediaTypeUtils.getMediaType(filename))
                .size(attrs.size())
                .lastModified(attrs.lastModifiedTime().toMillis())
                .subtitlePath(subtitleService.findSubtitlePath(path, rootDir))
                .relativePath(relativePath)
                .metadata(MediaTypeUtils.extractMetadata(path))
                .build();
    }

    public ResourceRegion getMediaRegion(String filename, HttpRange range) throws IOException {
        Path mediaPath = Paths.get(mediaProperties.getRootPath(), filename);
        Resource media = resourceLoader.getResource("file:" + mediaPath);
        long contentLength = media.contentLength();

        long start = range != null ? range.getRangeStart(contentLength) : 0;
        long end = range != null ? range.getRangeEnd(contentLength) : contentLength - 1;
        long rangeLength = Math.min(1024 * 1024, end - start + 1);

        return new ResourceRegion(media, start, rangeLength);
    }

    private String getMediaType(String filename) {
        filename = filename.toLowerCase();
        if (mediaProperties.getSupportedVideoTypes().stream().anyMatch(filename::endsWith)) {
            return "video";
        } else if (mediaProperties.getSupportedAudioTypes().stream().anyMatch(filename::endsWith)) {
            return "audio";
        }
        return "unknown";
    }

    /**
     * 查找字幕文件路径
     */
    private String findSubtitlePath(Path mediaPath, Path rootDir) {
        String filename = mediaPath.getFileName().toString();
        String baseName = filename.substring(0, filename.lastIndexOf('.'));
        Path parentDir = mediaPath.getParent();

        try (Stream<Path> paths = Files.list(parentDir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.startsWith(baseName.toLowerCase()) &&
                                mediaProperties.getSupportedSubtitleTypes().stream()
                                        .anyMatch(name::endsWith);
                    })
                    .findFirst()
                    .map(path -> "/api/media/subtitle/" + rootDir.relativize(path))
                    .orElse(null);
        } catch (IOException e) {
            logger.warn("Error finding subtitle for: {}", mediaPath, e);
            return null;
        }
    }

    /**
     * 检查是否有字幕文件
     */
    private boolean hasSubtitleFile(Path mediaPath, Path rootDir) {
        return findSubtitlePath(mediaPath, rootDir) != null;
    }
}