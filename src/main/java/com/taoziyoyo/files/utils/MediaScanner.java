//package com.taoziyoyo.files.utils;
//
//import com.taoziyoyo.files.config.MediaProperties;
//import com.taoziyoyo.files.model.MediaFile;
//import com.taoziyoyo.files.model.MediaTypeUtils;
//import com.taoziyoyo.files.service.SubtitleService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.tika.Tika;
//import org.apache.tika.metadata.Metadata;
//import org.apache.tika.parser.AutoDetectParser;
//import org.apache.tika.parser.ParseContext;
//import org.apache.tika.sax.BodyContentHandler;
//
//import java.io.IOException;
//import java.nio.file.attribute.*;
//import java.io.File;
//import java.io.FileInputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Stream;
//
//
//@Slf4j
//public class MediaScanner {
//
//    private final String baseDir;
//    private final Tika tika;
//    private final MediaProperties mediaProperties;
//    private  final SubtitleService subtitleService;
//    public MediaScanner(String baseDir, MediaProperties mediaProperties, SubtitleService subtitleService) {
//        this.baseDir = baseDir;
//        this.mediaProperties = mediaProperties;
//        this.subtitleService = subtitleService;
//        this.tika = new Tika();
//    }
//
//    public List<MediaFile> scanMediaFiles() throws Exception {
//        Path rootDir = Paths.get(mediaProperties.getRootPath());
//        List<MediaFile> mediaFiles = new ArrayList<>();
//        try (Stream<Path> paths = Files.walk(rootDir, mediaProperties.getMaxDepth())) {
//            paths.filter(Files::isRegularFile)
//                    .filter(MediaTypeUtils::isMediaFile)
//                    .filter(this::isMediaFile)
//                    .forEach(path -> {
//                        try {
//                            MediaFile mediaFile = createMediaFileObject(path, rootDir);
//                            mediaFiles.add(mediaFile);
//                        } catch (IOException e) {
//                            log.error("Error processing file: " + path, e);
//                        }
//                    });
//
//        }
//
//        File directory = new File(baseDir);
//        if (!directory.exists() || !directory.isDirectory()) {
//            throw new RuntimeException("Invalid media base directory: " + baseDir);
//        }
//
//        File[] files = directory.listFiles();
//        if (files != null) {
//            for (File file : files) {
//                if (file.isFile()) {
//                    MediaFile mediaFile = extractMetadata(file);
//                    if (mediaFile != null) {
//                        mediaFiles.add(mediaFile);
//                    }
//                }
//            }
//        }
//        return mediaFiles;
//    }
//
//    private boolean isMediaFile(Path path) {
//        String filename = path.getFileName().toString().toLowerCase();
//        return filename.endsWith(".mp4") ||
//                filename.endsWith(".mp3") ||
//                filename.endsWith(".wav");
//    }
//
//    private MediaFile createMediaFileObject(Path path, Path rootDir) throws IOException {
//        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
//        String filename = path.getFileName().toString();
//        String relativePath = rootDir.relativize(path).toString();
//
//        return MediaFile.builder()
//                .id(relativePath)
//                .filename(filename)
//                .path("/api/media/stream/" + relativePath)
//                .type(MediaTypeUtils.getMediaType(filename))
//                .size(attrs.size())
//                .lastModified(attrs.lastModifiedTime().toMillis())
//                .subtitlePath(subtitleService.findSubtitlePath(path, rootDir))
//                .relativePath(relativePath)
//                .metadata(MediaTypeUtils.extractMetadata(path))
//                .build();
//    }
//
//    private MediaFile buildMediaFileDto(Path path, Path rootDir) throws IOException {
//        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
//
//        String filename = path.getFileName().toString();
//        String relativePath = rootDir.relativize(path).toString();
//
//        return MediaFile.builder()
//                .id(relativePath)
//                .filename(filename)
//                .path("/api/media/stream/" + relativePath)
//                .type(getMediaType(filename))
//                .size(attrs.size())
//                .lastModified(attrs.lastModifiedTime().toMillis())
//                .subtitlePath(findSubtitlePath(path, rootDir))
//                .relativePath(relativePath)
//                .metadata(MediaTypeUtils.extractMetadata(path))
//                .hasSubtitle(hasSubtitleFile(path, rootDir))
//                .build();
//    }
//
//
//    private String getMediaType(String filename) {
//        filename = filename.toLowerCase();
//        if (mediaProperties.getSupportedVideoTypes().stream().anyMatch(filename::endsWith)) {
//            return "video";
//        } else if (mediaProperties.getSupportedAudioTypes().stream().anyMatch(filename::endsWith)) {
//            return "audio";
//        }
//        return "unknown";
//    }
//
//    /**
//     * 查找字幕文件路径
//     */
//    private String findSubtitlePath(Path mediaPath, Path rootDir) {
//        String filename = mediaPath.getFileName().toString();
//        String baseName = filename.substring(0, filename.lastIndexOf('.'));
//        Path parentDir = mediaPath.getParent();
//
//        try (Stream<Path> paths = Files.list(parentDir)) {
//            return paths
//                    .filter(Files::isRegularFile)
//                    .filter(path -> {
//                        String name = path.getFileName().toString().toLowerCase();
//                        return name.startsWith(baseName.toLowerCase()) &&
//                                mediaProperties.getSupportedSubtitleTypes().stream()
//                                        .anyMatch(name::endsWith);
//                    })
//                    .findFirst()
//                    .map(path -> "/api/media/subtitle/" + rootDir.relativize(path))
//                    .orElse(null);
//        } catch (IOException e) {
//            log.warn("Error finding subtitle for: {}", mediaPath, e);
//            return null;
//        }
//    }
//
//    /**
//     * 检查是否有字幕文件
//     */
//    private boolean hasSubtitleFile(Path mediaPath, Path rootDir) {
//        return findSubtitlePath(mediaPath, rootDir) != null;
//    }
//
//    private MediaFile extractMetadata(File file) {
//        try (FileInputStream inputStream = new FileInputStream(file)) {
//            Metadata metadata = new Metadata();
//            AutoDetectParser parser = new AutoDetectParser();
//            parser.parse(inputStream, new BodyContentHandler(), metadata, new ParseContext());
//
//            String fileType = tika.detect(file);
//            String duration = metadata.get("xmpDM:duration");
//            String codec = metadata.get("xmpDM:audioCompressor");
//            long fileSize = file.length();
//            long lastModified = file.lastModified();
//            return null;
//
//        } catch (Exception e) {
//            System.err.println("Error extracting metadata for file: " + file.getName() + " - " + e.getMessage());
//            return null;
//        }
//    }
//}