package com.layerten.controller;

import com.layerten.dto.MediaAssetDTO;
import com.layerten.service.MediaService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Public API controller for serving media files.
 * Provides endpoints for retrieving uploaded media assets.
 */
@RestController
@RequestMapping("/api/media")
public class MediaController {
    
    private final MediaService mediaService;
    
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }
    
    /**
     * Serve a media file by ID.
     * Includes caching headers and appropriate content type.
     * 
     * @param id the media asset ID
     * @return the file as a resource with caching headers
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getMedia(@PathVariable Long id) throws IOException {
        // Get media metadata
        MediaAssetDTO mediaAsset = mediaService.getMediaById(id);
        
        // Get file from disk
        Path filePath = mediaService.getMediaFile(id);
        Resource resource = new UrlResource(filePath.toUri());
        
        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("File not found or not readable: " + mediaAsset.filename());
        }
        
        // Determine content type
        String contentType = mediaAsset.contentType();
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        
        // Build response with caching headers
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
            .eTag(String.valueOf(id))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + mediaAsset.filename() + "\"")
            .body(resource);
    }
}
