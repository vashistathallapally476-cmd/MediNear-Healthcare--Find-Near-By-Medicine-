package com.mednear.service;

import com.mednear.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadRoot;

    public FileStorageService(@Value("${mednear.upload.dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir, "prescriptions").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload directory: " + this.uploadRoot, e);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("Cannot upload empty file");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        } else {
            extension = ".jpg";
        }

        // Validate image extension
        if (!extension.matches("^\\.(jpg|jpeg|png|webp|bmp)$")) {
            throw new BusinessException("Unsupported file type. Please upload a JPG, PNG, or WEBP image.");
        }

        String fileName = UUID.randomUUID().toString() + extension;
        Path targetLocation = this.uploadRoot.resolve(fileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/prescriptions/" + fileName;
        } catch (IOException e) {
            throw new BusinessException("Failed to save prescription image: " + e.getMessage());
        }
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }
}
