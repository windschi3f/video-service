package com.windschief.videoservice.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

	private final Path rootLocation;

	public FileSystemStorageService(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
	}

	@Override
	public void init() {
		try {
			Files.createDirectory(rootLocation);

			for (FileType fileLocation : FileType.values()) {
				Files.createDirectory(rootLocation.resolve(fileLocation.subDirectory));
			}
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}

	@Override
	public String store(MultipartFile file, FileType fileType) {
		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file.");
			}

			String filename = FilenameUtils.getName(file.getOriginalFilename()); //Pfad entfernen

			Path destinationFile = load(filename, fileType);

			if (Files.exists(destinationFile)) {
				String basename = FilenameUtils.getBaseName(destinationFile.toString());
				String extension = FilenameUtils.getExtension(destinationFile.toString());
				int i = 1;
				do {
					filename = basename + "-" + i + "." + extension;
					destinationFile = load(filename, fileType);
					i++;
				} while (Files.exists(destinationFile));
			}			

			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
			}

			return filename;
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file.", e);
		}
	}

	@Override
	public Stream<Path> loadAll() {
		try {
			return Files.walk(this.rootLocation, 1)
				.filter(path -> !path.equals(this.rootLocation))
				.map(this.rootLocation::relativize);
		}
		catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}
	}

	@Override
	public Stream<Path> loadAll(FileType fileType) {
		try {
			return Files.walk(this.rootLocation.resolve(fileType.subDirectory), 1)
				.filter(path -> !path.equals(this.rootLocation.resolve(fileType.subDirectory)))
				.map(this.rootLocation::relativize);
		}
		catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}
	}

	@Override
	public Path load(String filename, FileType fileType) {
		Path path = rootLocation.resolve(fileType.subDirectory).resolve(FilenameUtils.getName(filename));
		if (!path.getParent().equals(rootLocation.resolve(fileType.subDirectory).toAbsolutePath())) {
            throw new StorageException("Cannot delete file outside current directory.");
        }
		
		return path;
	}

	@Override
	public Resource loadAsResource(String filename, FileType fileType) {
		try {
			Path path = load(filename, fileType);
			Resource resource = new UrlResource(path.toUri());
			
			if (!resource.exists() || !resource.isReadable()) {
				throw new StorageFileNotFoundException("Could not read file: " + filename);
			}

			return resource;
		}
		catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

    @Override
    public boolean delete(String filename, FileType fileType) {
		try {
			File file = loadAsResource(filename, fileType).getFile();
			return file.delete();
		} catch (IOException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
    }

	@Override
	public void deleteAll() {
		try {
			FileUtils.cleanDirectory(rootLocation.toFile());
		} catch (IOException e) {
			throw new StorageFileNotFoundException("Rootlocation not found: " + rootLocation.toString(), e);
		}
	}
}