package com.layerten.service;

import com.layerten.dto.MediaAssetDTO;
import com.layerten.entity.MediaAsset;
import com.layerten.repository.MediaAssetRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MediaService {

    private final MediaAssetRepository mediaAssetRepository;
    private final Path mediaRoot;

    public MediaService(
            MediaAssetRepository mediaAssetRepository,
            @Value("${layerten.media.root:./local-media}") String mediaRootPath
    ) {
        this.mediaAssetRepository = mediaAssetRepository;
        this.mediaRoot = Paths.get(mediaRootPath).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.mediaRoot);
        } catch (IOException e) {
            throw new RuntimeException("Could not create media storage directory", e);
        }
    }

    @Transactional
    public MediaAssetDTO uploadMedia(MultipartFile file, String altText) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("File has no name");
        }

        // Generate unique filename
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Save file to disk
        Path targetPath = mediaRoot.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Create database record
        MediaAsset mediaAsset = new MediaAsset(
            uniqueFilename,
            file.getContentType(),
            file.getSize(),
            altText,
            targetPath.toString()
        );

        MediaAsset saved = mediaAssetRepository.save(mediaAsset);
        
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public MediaAssetDTO getMediaById(Long id) {
        MediaAsset mediaAsset = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Media asset not found with id: " + id));
        return toDTO(mediaAsset);
    }

    @Transactional(readOnly = true)
    public java.util.List<MediaAssetDTO> getAllMedia() {
        return mediaAssetRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Path getMediaFile(Long id) {
        MediaAsset mediaAsset = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Media asset not found with id: " + id));
        
        Path filePath = mediaRoot.resolve(mediaAsset.getFilename());
        
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found on disk: " + mediaAsset.getFilename());
        }
        
        return filePath;
    }

    @Transactional
    public void deleteMedia(Long id) throws IOException {
        MediaAsset mediaAsset = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Media asset not found with id: " + id));
        
        // Delete file from disk
        Path filePath = mediaRoot.resolve(mediaAsset.getFilename());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        // Delete database record
        mediaAssetRepository.delete(mediaAsset);
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    private MediaAssetDTO toDTO(MediaAsset mediaAsset) {
        return new MediaAssetDTO(
            mediaAsset.getId(),
            mediaAsset.getFilename(),
            mediaAsset.getContentType(),
            mediaAsset.getFileSize(),
            mediaAsset.getAltText(),
            "/api/media/" + mediaAsset.getId()
        );
    }
}
