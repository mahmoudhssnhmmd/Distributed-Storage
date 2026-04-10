package springbootproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springbootproject.entity.FileMetadata;
import springbootproject.entity.User;
import springbootproject.service.FileService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileMetadata> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User owner) throws IOException {
        return ResponseEntity.ok(fileService.uploadFile(file, owner));
    }

    @GetMapping
    public ResponseEntity<List<FileMetadata>> getUserFiles(
            @AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(fileService.getUserFiles(owner));
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal User owner) throws IOException {
        byte[] fileBytes = fileService.downloadFile(fileId, owner);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileBytes);
    }
}