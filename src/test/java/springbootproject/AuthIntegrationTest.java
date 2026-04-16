package springbootproject;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    private static final String DEFAULT_PASSWORD = "12345678";

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    void shouldReturnJwtTokenWhenCredentialsAreValid() throws Exception {
        String username = "auth_user";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload(username)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void shouldReturnBadRequestForInvalidRegisterPayload() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "email": "invalid-email",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isString());
    }
}


