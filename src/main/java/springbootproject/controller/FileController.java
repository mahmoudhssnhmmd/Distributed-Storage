package springbootproject.controller;

import jakarta.servlet.ServletContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springbootproject.dto.RenameFileRequest;
import springbootproject.entity.FileMetadata;
import springbootproject.entity.User;
import springbootproject.service.FileService;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final ServletContext servletContext;
    private final FileService fileService;

    private void validatePagination(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination values");
        }
    }

    private Sort resolveSort(String sortBy, String direction) {
        if (!"asc".equalsIgnoreCase(direction) && !"desc".equalsIgnoreCase(direction)) {
            throw new IllegalArgumentException("direction must be 'asc' or 'desc'");
        }
        return "asc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
    }

    @PostMapping("/upload")
    public ResponseEntity<FileMetadata> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User owner) throws IOException {
        return ResponseEntity.ok(fileService.uploadFile(file, owner));
    }

    @GetMapping
    public ResponseEntity<Page<FileMetadata>> getUserFiles(
            @AuthenticationPrincipal User owner,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        validatePagination(page, size);
        Sort sort = resolveSort(sortBy, direction);
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(fileService.getUserFiles(owner, pageable));
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<FileMetadata> renameFile(@PathVariable Long id,
                                                   @Valid @RequestBody RenameFileRequest request,
                                                   @AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(fileService.renameFile(id, request.getFilename(), owner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id,
                                           @AuthenticationPrincipal User owner) {
        fileService.deleteFile(id, owner);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id,
                                                 @AuthenticationPrincipal User owner) {
        Resource resource = fileService.downloadFile(id, owner);
        String contentType = DEFAULT_CONTENT_TYPE;

        String mimeType = servletContext.getMimeType(resource.getFilename());
        if (mimeType != null) {
            contentType = mimeType;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}