package com.windschief.videoservice.storage;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void init();
    String store(MultipartFile file, FileType fileType);
    Stream<Path> loadAll();
    Stream<Path> loadAll(FileType fileType);
    Path load(String filename, FileType fileType);
    Resource loadAsResource(String filename, FileType fileType);
    boolean delete(String filename, FileType fileType);
    void deleteAll();
}
