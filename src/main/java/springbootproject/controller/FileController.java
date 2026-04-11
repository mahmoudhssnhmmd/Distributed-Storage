package springbootproject.controller;

import jakarta.servlet.ServletContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springbootproject.entity.FileMetadata;
import springbootproject.entity.User;
import springbootproject.service.FileService;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    @Autowired
    private ServletContext servletContext;

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



    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id ,
                                                 @AuthenticationPrincipal User owner) {
        Resource resource = fileService.downloadFile(id,owner);
        String contentType = "application/octet-stream";

        try {
            String mimeType = servletContext.getMimeType(resource.getFile().getAbsolutePath());
            if (mimeType != null) {
                contentType = mimeType;
            }
        } catch (IOException ex) {

        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}