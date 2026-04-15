package springbootproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springbootproject.entity.FileMetadata;
import springbootproject.entity.User;
import springbootproject.repository.FileMetadataRepository;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;

    @Value("${app.storage.dir:uploads}")
    private String storageDir;

    private Path getStorageRoot() {
        return Paths.get(storageDir).toAbsolutePath().normalize();
    }

    private FileMetadata findOwnedFileOrThrow(Long fileId, User owner) {
        return fileMetadataRepository.findByIdAndUser(fileId, owner)
                .orElseThrow(() -> new NoSuchElementException("File not found with ID: " + fileId));
    }

    private String sanitizeFilename(String originalFilename) {
        String fallbackName = originalFilename == null ? "file" : originalFilename;
        return Paths.get(fallbackName).getFileName().toString();
    }

    @CacheEvict(cacheNames = "userFiles", allEntries = true)
    public FileMetadata uploadFile(MultipartFile file, User owner) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        Path storageRoot = getStorageRoot();
        Files.createDirectories(storageRoot);

        String safeFilename = sanitizeFilename(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID() + "_" + safeFilename;
        Path filePath = storageRoot.resolve(uniqueFilename);
        Files.write(filePath, file.getBytes());

        FileMetadata metadata = new FileMetadata();
        metadata.setFilename(safeFilename);
        metadata.setContentType(file.getContentType());
        metadata.setSize(file.getSize());
        metadata.setStoragePath(filePath.toString());
        metadata.setUser(owner);

        return fileMetadataRepository.save(metadata);
    }

    public List<FileMetadata> getUserFiles(User owner) {
        return fileMetadataRepository.findByUser(owner);
    }

    @Cacheable(
            cacheNames = "userFiles",
            key = "#owner.id + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()",
            condition = "#owner != null && #owner.id != null"
    )
    public Page<FileMetadata> getUserFiles(User owner, Pageable pageable) {
        return fileMetadataRepository.findByUser(owner, pageable);
    }

    @CacheEvict(cacheNames = "userFiles", allEntries = true)
    public FileMetadata renameFile(Long id, String newFilename, User owner) {
        if (newFilename == null || newFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be blank");
        }

        FileMetadata metadata = findOwnedFileOrThrow(id, owner);

        metadata.setFilename(newFilename.trim());
        return fileMetadataRepository.save(metadata);
    }

    @CacheEvict(cacheNames = "userFiles", allEntries = true)
    public void deleteFile(Long id, User owner) {
        FileMetadata metadata = findOwnedFileOrThrow(id, owner);

        try {
            Files.deleteIfExists(Paths.get(metadata.getStoragePath()));
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete file from storage", ex);
        }

        fileMetadataRepository.delete(metadata);
    }


    public Resource downloadFile(Long id, User owner) {
        FileMetadata metadata = findOwnedFileOrThrow(id, owner);
        Path path = Paths.get(metadata.getStoragePath());

        try {
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists()) {
                return resource;
            }
            throw new NoSuchElementException("File does not exist on the server");
        } catch (IOException e) {
            throw new RuntimeException("Error reading file", e);
        }
    }
}