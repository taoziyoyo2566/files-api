package com.taoziyoyo.files.controller;

import com.taoziyoyo.files.model.ApiResponse;
import com.taoziyoyo.files.model.MediaFile;
import com.taoziyoyo.files.model.Subtitle;
import com.taoziyoyo.files.model.SubtitleEntry;
import com.taoziyoyo.files.service.MediaService;
import com.taoziyoyo.files.service.SubtitleService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController implements ErrorController {
    private final MediaService mediaService;
    private final SubtitleService subtitleService;
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

    @GetMapping("/stream2/{filename}")
    public ResponseEntity<ResourceRegion> streamMedia2(
            @PathVariable String filename,
            @RequestHeader HttpHeaders headers) throws IOException {

        HttpRange range = headers.getRange().isEmpty() ? null : headers.getRange().getFirst();
        logger.info("range: {}",range);
        ResourceRegion region = mediaService.getMediaRegion(filename, range);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory.getMediaType("file:" + filename)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(region);
    }


    /**
     * json subtitle
     * @param filename String
     * @return List
     */
    @GetMapping("/subtitle2/{filename}")
    public ResponseEntity<List<Subtitle>> getSubtitles(@PathVariable String filename) {
        return ResponseEntity.ok(subtitleService.getSubtitles(filename));
    }

    /**
     * srt subtitle
     * @param filename String
     * @return List
     */
    @GetMapping("/subtitle/{filename}")
    public ResponseEntity<List<SubtitleEntry>> getSubtitleSrt(@PathVariable String filename) throws IOException {
        logger.info("request[getSubtitleSrt] start");
        return ResponseEntity.ok(subtitleService.getSubtitleSrt(filename));
    }

    @GetMapping("/stream/{filename}")
    public ResponseEntity<ResourceRegion> streamMedia(
            @PathVariable String filename,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            HttpServletRequest request) {

        Resource mediaResource = mediaService.getMediaResource(filename);
        try {
            long contentLength = mediaResource.contentLength();

            // 3. 设置适当的Content-Type
            MediaType mediaType = MediaTypeFactory
                    .getMediaType(mediaResource)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

            // 4. 如果存在Range头，处理部分内容请求
            if (StringUtils.hasText(rangeHeader)) {
                // 解析Range头
                String[] ranges = rangeHeader.replace("bytes=", "").split("-");
                long start = Long.parseLong(ranges[0]);
                long end = ranges.length > 1 ?
                        Long.parseLong(ranges[1]) :
                        contentLength - 1;

                // 创建ResourceRegion
                ResourceRegion region = new ResourceRegion(
                        mediaResource, start,
                        Math.min(1024 * 1024, end - start + 1)
                );

                // 返回206 Partial Content状态
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .contentType(mediaType)
                        .header("Accept-Ranges", "bytes")
                        .body(region);
            }

            // 5. 如果没有Range头，返回完整内容
            ResourceRegion region = new ResourceRegion(
                    mediaResource, 0,
                    Math.min(1024 * 1024, contentLength)
            );

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header("Accept-Ranges", "bytes")
                    .body(region);

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error streaming media file"
            );
        }
    }

    @RequestMapping("/error")
    public ApiResponse<Void> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object method = request.getMethod();

        if (status != null && Integer.parseInt(status.toString()) == HttpStatus.NOT_FOUND.value()) {
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