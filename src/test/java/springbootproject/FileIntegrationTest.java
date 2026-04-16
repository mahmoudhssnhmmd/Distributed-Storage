package springbootproject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.Comparator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileIntegrationTest {

    private static final String DEFAULT_PASSWORD = "12345678";
    private static final Path TEST_UPLOAD_DIR = Paths.get("target/test-uploads");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String registerPayload(String username) {
        return """
                {
                  "username": "%s",
                  "email": "%s@example.com",
                  "password": "%s"
                }
                """.formatted(username, username, DEFAULT_PASSWORD);
    }

    private String loginPayload(String username) {
        return """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, DEFAULT_PASSWORD);
    }

    @AfterEach
    void cleanupUploadedFiles() throws Exception {
        if (Files.exists(TEST_UPLOAD_DIR)) {
            try (Stream<Path> walk = Files.walk(TEST_UPLOAD_DIR)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (Exception ignored) {
                            }
                        });
            }
        }
    }

    @Test
    void shouldHandleFileCrudFlowWhenRequestIsAuthorized() throws Exception {
        String username = "file_user_" + System.currentTimeMillis();
        String token = registerAndLogin(username);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hello from test".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/files/upload")
                        .file(multipartFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("hello.txt"))
                .andReturn();

        JsonNode uploadJson = objectMapper.readTree(uploadResult.getResponse().getContentAsString());
        long fileId = uploadJson.get("id").asLong();

        mockMvc.perform(get("/api/files")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(fileId));

        mockMvc.perform(patch("/api/files/{id}/rename", fileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "filename": "renamed.txt"
                                }
                                """)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("renamed.txt"));

        mockMvc.perform(delete("/api/files/{id}", fileId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectFileAccessWithoutAuthorizationToken() throws Exception {
        mockMvc.perform(get("/api/files"))
                .andExpect(status().isForbidden());
    }

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload(username)))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(username)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return loginJson.get("token").asText();
    }
}

