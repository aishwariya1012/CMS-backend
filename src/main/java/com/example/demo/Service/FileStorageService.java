package com.example.demo.Service;


import com.example.demo.Exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.profiles-dir}")
    private String profilesDir;

    public String storeFile(MultipartFile file, String prefix) {
        validateImageFile(file);
        try {
            Files.createDirectories(Paths.get(profilesDir));
            String ext      = getExtension(file.getOriginalFilename());
            String fileName = prefix + "-" + UUID.randomUUID() + ext;
            Path   target   = Paths.get(profilesDir).resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored at: {}", target);
            return target.toString();
        } catch (IOException e) {
            log.error("File storage failed: {}", e.getMessage());
            throw new RuntimeException("Could not store file: " + e.getMessage());
        }
    }


    public Resource loadFileAsResource(String filePath) {
        try {
            Path path = Paths.get(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new BadRequestException("File not found or not readable: " + filePath);
        } catch (MalformedURLException e) {
            throw new BadRequestException("Invalid file path: " + filePath);
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new BadRequestException("Uploaded file is empty");
        String name = file.getOriginalFilename();
        if (name == null) throw new BadRequestException("Invalid file name");
        String ext = getExtension(name).toLowerCase();
        if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png"))
            throw new BadRequestException("Only JPG, JPEG, PNG files are allowed");
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return ".jpg";
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
