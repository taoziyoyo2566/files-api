package com.taoziyoyo.files.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import java.util.stream.StreamSupport;

public class MediaTypeUtils {

    private static final Logger log = LoggerFactory.getLogger(MediaTypeUtils.class);
    // 媒体类型映射
    private static final Map<String, String> MEDIA_TYPES = new HashMap<>();

    static {
        // 视频格式
        MEDIA_TYPES.put("mp4", "video/mp4");
        MEDIA_TYPES.put("webm", "video/webm");
        MEDIA_TYPES.put("avi", "video/x-msvideo");
        MEDIA_TYPES.put("wmv", "video/x-ms-wmv");
        MEDIA_TYPES.put("flv", "video/x-flv");
        MEDIA_TYPES.put("mkv", "video/x-matroska");
        MEDIA_TYPES.put("mov", "video/quicktime");

        // 音频格式
        MEDIA_TYPES.put("mp3", "audio/mpeg");
        MEDIA_TYPES.put("wav", "audio/wav");
        MEDIA_TYPES.put("ogg", "audio/ogg");
        MEDIA_TYPES.put("m4a", "audio/mp4");
        MEDIA_TYPES.put("flac", "audio/flac");
        MEDIA_TYPES.put("aac", "audio/aac");

        // 字幕格式
        MEDIA_TYPES.put("srt", "application/x-subrip");
        MEDIA_TYPES.put("vtt", "text/vtt");
        MEDIA_TYPES.put("ass", "text/x-ssa");
    }

    /**
     * 获取文件的媒体类型
     */
    public static String getMediaType(String filename) {
        String extension = getFileExtension(filename);
        return MEDIA_TYPES.getOrDefault(extension, "application/octet-stream");
    }

    /**
     * 检查是否为媒体文件
     */
    public static boolean isMediaFile(Path path) {
        String mediaType = getMediaType(path.getFileName().toString());
        return mediaType.startsWith("video/") || mediaType.startsWith("audio/");
    }

    /**
     * 检查是否为视频文件
     */
    public static boolean isVideoFile(String filename) {
        String mediaType = getMediaType(filename);
        return mediaType.startsWith("video/");
    }

    /**
     * 检查是否为音频文件
     */
    public static boolean isAudioFile(String filename) {
        String mediaType = getMediaType(filename);
        return mediaType.startsWith("audio/");
    }

    /**
     * 检查是否为字幕文件
     */
    public static boolean isSubtitleFile(String filename) {
        String extension = getFileExtension(filename);
        return MEDIA_TYPES.containsKey(extension) &&
                (extension.equals("srt") || extension.equals("vtt") || extension.equals("ass"));
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 获取Spring MediaType对象
     */
    public static MediaType getSpringMediaType(String filename) {
        String mimeType = getMediaType(filename);
        try {
            return MediaType.parseMediaType(mimeType);
        } catch (Exception e) {
            log.warn("Failed to parse media type for file: {}", filename, e);
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    /**
     * 获取文件基本名（不含扩展名）
     */
    public static String getBaseName(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(0, lastDotIndex);
        }
        return filename;
    }

    /**
     * 判断是否为支持的媒体格式
     */
    public static boolean isSupportedMediaType(String filename) {
        String extension = getFileExtension(filename);
        return MEDIA_TYPES.containsKey(extension);
    }


    /**
     * 提取媒体元数据
     */
    public static MediaMetadata extractMetadata(Path path) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "quiet",
                    "-print_format", "json",
                    "-show_format",
                    "-show_streams",
                    path.toString()
            );

            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            JsonNode json = new ObjectMapper().readTree(output);

            JsonNode format = json.get("format");
            JsonNode videoStream = StreamSupport.stream(json.get("streams").spliterator(), false)
                    .filter(stream -> "video".equals(stream.get("codec_type").asText()))
                    .findFirst()
                    .orElse(null);

            return MediaMetadata.builder()
                    .duration(format.has("duration") ?
                            (long)(format.get("duration").asDouble() * 1000) : null)
                    .format(format.get("format_name").asText())
                    .codec(videoStream != null ?
                            videoStream.get("codec_name").asText() : null)
                    .width(videoStream != null ?
                            videoStream.get("width").asInt() : null)
                    .height(videoStream != null ?
                            videoStream.get("height").asInt() : null)
                    .bitrate(format.has("bit_rate") ?
                            format.get("bit_rate").asInt() : null)
                    .resolution(videoStream != null ?
                            videoStream.get("width").asText() + "x" +
                                    videoStream.get("height").asText() : null)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to extract metadata for: {}", path, e);
            return MediaMetadata.builder().build();
        }
    }
}