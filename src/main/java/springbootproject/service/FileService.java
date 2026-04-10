package springbootproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springbootproject.entity.FileMetadata;
import springbootproject.entity.User;
import springbootproject.repository.FileMetadataRepository;

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
        metadata.setOwner(owner);

        return fileMetadataRepository.save(metadata);
    }

    public List<FileMetadata> getUserFiles(User owner) {
        return fileMetadataRepository.findByOwner(owner);
    }

    public byte[] downloadFile(Long fileId, User owner) throws IOException {
        FileMetadata metadata = fileMetadataRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new RuntimeException("File not found"));

        return Files.readAllBytes(Paths.get(metadata.getStoragePath()));
    }
}