package com.taoziyoyo.files.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "media")
@Data
public class MediaProperties {
    @NotNull(message = "Root path must be specified")
    private String rootPath;

    @NotEmpty(message = "Supported video types must not be empty")
    private List<String> supportedVideoTypes;

    @NotEmpty(message = "Supported audio types must not be empty")
    private List<String> supportedAudioTypes;

    @NotEmpty(message = "Supported subtitle types must not be empty")
    private List<String> supportedSubtitleTypes;

    @Min(value = 1, message = "Max depth must be at least 1")
    @Max(value = 10, message = "Max depth cannot exceed 10")
    private Integer maxDepth;

    @Positive(message = "Max file size must be positive")
    private Long maxFileSize;

    private String tempPath;

    @PostConstruct
    public void init() {
        System.out.println("MediaProperties initialized:");
        System.out.println("rootPath: " + rootPath);
        System.out.println("maxDepth: " + maxDepth);
        System.out.println("supportedVideoTypes: " + supportedVideoTypes);
    }
}
