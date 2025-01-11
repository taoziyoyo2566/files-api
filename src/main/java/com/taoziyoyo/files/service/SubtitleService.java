package com.taoziyoyo.files.service;

import com.taoziyoyo.files.config.MediaProperties;
import com.taoziyoyo.files.exception.MediaException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
public class SubtitleService {

    private static final Logger logger = LoggerFactory.getLogger(SubtitleService.class);
    private final MediaProperties mediaProperties;

    private static final Pattern TIME_PATTERN =
            Pattern.compile("(\\d{2}:){1,2}\\d{2},\\d{3} --> (\\d{2}:){1,2}\\d{2},\\d{3}");

    /**
     * 查找匹配的字幕文件
     */
    public String findSubtitlePath(Path mediaPath, Path rootDir) {
        logger.debug("查找匹配的字幕文件: {}", "findSubtitlePath");
        try {
            String filename = mediaPath.getFileName().toString();
            String baseName = filename.substring(0, filename.lastIndexOf('.'));
            Path parentDir = mediaPath.getParent();
            logger.debug("fileName: {}",filename);
            logger.debug("baseName: {}",baseName);
            logger.debug("parentDir: {}",parentDir);
            logger.debug("mediaPath: {}",mediaPath.toString());
            if (parentDir == null || !Files.exists(parentDir)) {
                return null;
            }

            // 遍历目录查找匹配的字幕文件
            try (var files = Files.list(parentDir)) {
                return files
                        .filter(Files::isRegularFile)
                        .filter(this::isSubtitleFile)
                        .filter(path -> {
                            String name = path.getFileName().toString();
                            return name.startsWith(baseName) ||
                                    name.startsWith(baseName.toLowerCase());
                        })
                        .findFirst()
                        .map(path -> rootDir.relativize(path).toString())
                        .orElse(null);
            }
        } catch (IOException e) {
            logger.warn("Error finding subtitle for media: {}", mediaPath, e);
            return null;
        }
    }

    /**
     * 判断是否为字幕文件
     */
    private boolean isSubtitleFile(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        logger.debug("filename: {}",filename);
        logger.debug("isSubtitleFile: {}",mediaProperties.getSupportedSubtitleTypes().stream()
                .anyMatch(filename::endsWith));
        return mediaProperties.getSupportedSubtitleTypes().stream()
                .anyMatch(filename::endsWith);
    }

    /**
     * 解析SRT格式字幕文件
     */
    public List<SubtitleEntry> parseSrtFile(Path subtitlePath) {
        if (!Files.exists(subtitlePath)) {
            throw new MediaException("Subtitle file not found: " + subtitlePath);
        }

        List<SubtitleEntry> subtitles = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(subtitlePath, StandardCharsets.UTF_8)) {
            String line;
            SubtitleEntry currentEntry = null;
            StringBuilder textBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    if (currentEntry != null && textBuilder.length() > 0) {
                        currentEntry.setText(textBuilder.toString().trim());
                        subtitles.add(currentEntry);
                        currentEntry = null;
                        textBuilder.setLength(0);
                    }
                    continue;
                }

                if (currentEntry == null) {
                    currentEntry = new SubtitleEntry();
                    continue;
                }

                Matcher matcher = TIME_PATTERN.matcher(line);
                if (matcher.matches()) {
                    String[] times = line.split(" --> ");
                    currentEntry.setStartTime(parseTime(times[0]));
                    currentEntry.setEndTime(parseTime(times[1]));
                } else {
                    if (textBuilder.length() > 0) {
                        textBuilder.append("\n");
                    }
                    textBuilder.append(line);
                }
            }

            // 处理最后一个字幕条目
            if (currentEntry != null && textBuilder.length() > 0) {
                currentEntry.setText(textBuilder.toString().trim());
                subtitles.add(currentEntry);
            }

        } catch (IOException e) {
            throw new MediaException("Failed to parse subtitle file: " + subtitlePath, e);
        }

        return subtitles;
    }

    /**
     * 解析时间字符串为毫秒值
     */
    private long parseTime(String timeStr) {
        // 标准化时间格式
        if (timeStr.length() == 9) { // MM:SS,mmm
            timeStr = "00:" + timeStr;
        }
        timeStr = timeStr.replace(',', '.');

        LocalTime time = LocalTime.parse(timeStr,
                DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));

        return time.toNanoOfDay() / 1_000_000; // 转换为毫秒
    }

    @lombok.Data
    public static class SubtitleEntry {
        private int index;
        private long startTime; // 毫秒
        private long endTime;   // 毫秒
        private String text;
    }
}