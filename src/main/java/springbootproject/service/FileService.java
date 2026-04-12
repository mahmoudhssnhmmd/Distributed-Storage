package springbootproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springbootproject.entity.FileMetadata;
import springbootproject.entity.User;
import springbootproject.repository.FileMetadataRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;

    private final String STORAGE_DIR = "uploads/";

    public FileMetadata uploadFile(MultipartFile file, User owner) throws IOException {
        Files.createDirectories(Paths.get(STORAGE_DIR));

        String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(STORAGE_DIR + uniqueFilename);
        Files.write(filePath, file.getBytes());

        FileMetadata metadata = new FileMetadata();
        metadata.setFilename(file.getOriginalFilename());
        metadata.setContentType(file.getContentType());
        metadata.setSize(file.getSize());
        metadata.setStoragePath(filePath.toString());
        metadata.setUser(owner);

        return fileMetadataRepository.save(metadata);
    }

    public List<FileMetadata> getUserFiles(User owner) {
        return fileMetadataRepository.findByUser(owner);
    }


    public Resource downloadFile(Long id, User owner) {
        FileMetadata metadata = fileMetadataRepository.findByIdAndUser(id, owner)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + id));

        try {
            Path path = Paths.get(metadata.getStoragePath());
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("File does not exist on the server");
            }
            return resource;
        } catch (Exception e) {
            throw new RuntimeException("Error reading file: " + e.getMessage());
        }
    }
}