package com.layerten.controller;

import com.layerten.dto.MediaAssetDTO;
import com.layerten.service.MediaService;
import com.layerten.validation.ValidFileSize;
import com.layerten.validation.ValidImageFile;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Admin API controller for managing media assets.
 * Requires authentication.
 */
@RestController
@RequestMapping("/api/admin/media")
@Validated
public class AdminMediaController {
    
    private final MediaService mediaService;
    
    public AdminMediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }
    
    /**
     * Upload a new media file.
     * 
     * @param file the file to upload
     * @param altText optional alt text for the image
     * @return the created media asset
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MediaAssetDTO uploadMedia(
        @RequestParam("file") 
        @NotNull(message = "File must not be null")
        @ValidImageFile
        @ValidFileSize(maxSizeInBytes = 10 * 1024 * 1024, message = "File size must not exceed 10MB")
        MultipartFile file,
        @RequestParam(required = false) String altText
    ) throws IOException {
        return mediaService.uploadMedia(file, altText);
    }
    
    /**
     * Delete a media asset.
     * 
     * @param id the media asset ID
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedia(@PathVariable Long id) throws IOException {
        mediaService.deleteMedia(id);
    }
}
