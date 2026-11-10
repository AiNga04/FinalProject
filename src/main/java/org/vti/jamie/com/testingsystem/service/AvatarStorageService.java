package org.vti.jamie.com.testingsystem.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface AvatarStorageService {
    String store(MultipartFile file) throws IOException;
    Path resolve(String storageKey);
    void delete(String storageKey) throws IOException;
}
