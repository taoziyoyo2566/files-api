package com.taoziyoyo.files.controller;

import com.taoziyoyo.files.model.MediaFile;
import com.taoziyoyo.files.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/api")
@RestController
public class MediaController {

    private final MediaService mediaService;
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/media/list")
    public Map<String, Object> listMediaFiles() {
        List<MediaFile> mediaFiles = mediaService.getMediaFiles();
        System.out.println("=================");
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("count", mediaFiles.size());
        response.put("data", mediaFiles);
        return response;
    }
}