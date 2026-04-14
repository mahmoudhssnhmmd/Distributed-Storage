package springbootproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RenameFileRequest {
    @NotBlank(message = "Filename is required")
    @Size(max = 255, message = "Filename must be 255 characters or less")
    private String filename;
}

