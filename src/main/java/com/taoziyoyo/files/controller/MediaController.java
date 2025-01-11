package com.taoziyoyo.files.controller;

import com.taoziyoyo.files.model.ApiResponse;
import com.taoziyoyo.files.model.MediaFile;
import com.taoziyoyo.files.service.MediaService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController implements ErrorController {
    private final MediaService mediaService;
    private static final Logger logger = LoggerFactory.getLogger(MediaController.class);
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<MediaFile>>> getMediaFiles() {
        logger.info("request[list] start");
        return ResponseEntity.ok(ApiResponse.success(mediaService.getMediaFiles()));
    }

    @RequestMapping("/")
    public String home(){
        return "home";
    }
    @GetMapping("/stream/{filename}")
    public ResponseEntity<ResourceRegion> streamMedia(
            @PathVariable String filename,
            @RequestHeader HttpHeaders headers) throws IOException {

        HttpRange range = headers.getRange().isEmpty() ? null : headers.getRange().get(0);
        ResourceRegion region = mediaService.getMediaRegion(filename, range);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory.getMediaType("file:" + filename)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(region);
    }

    @RequestMapping("/error")
    public ApiResponse<Void> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object method = request.getMethod();

        if (status != null && Integer.valueOf(status.toString()) == HttpStatus.NOT_FOUND.value()) {
            return ApiResponse.error(
                    HttpStatus.NOT_FOUND.value(),
                    "ENDPOINT_NOT_FOUND",
                    String.format("Could not find the %s method for URL %s", method, path)
            );
        }

        return ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred"
        );
    }
}