package fu.se.myplatform;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType; // Quan trọng
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationAPITest {

    @Autowired
    private MockMvc mockMvc;

    // Đăng ký tài khoản mới thành công (username chưa tồn tại)
    @Test
    void testRegister_Success() throws Exception {
        String requestBody = """
            {
                "userName": "string12",
                "email": "string12@example.com",
                "password": "string"
            }
        """;
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    // Đăng ký trùng username (đã có sẵn trong DB)
    @Test
    void testRegister_DuplicateUsername() throws Exception {
        String requestBody = """
            {
                "userName": "string01",
                "email": "another01@example.com",
                "password": "string"
            }
        """;
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Tên đăng nhập đã được sử dụng"));
    }

    // Đăng ký trùng email (email đã có trong DB)
    @Test
    void testRegister_DuplicateEmail() throws Exception {
        String requestBody = """
            {
                "userName": "newusername1",
                "email": "string09@gmail.com", 
                "password": "string"
            }
        """;
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email đã được sử dụng"));
    }

    // Đăng nhập thành công với user đã tồn tại
    @Test
    void testLogin_Success() throws Exception {
        String requestBody = """
            {
                "userName": "string02",
                "password": "string"
            }
        """;
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("string02"));
    }

    // Đăng nhập sai password
    @Test
    void testLogin_WrongPassword() throws Exception {
        String requestBody = """
            {
                "userName": "string02",
                "password": "wrongpass"
            }
        """;
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid username or password"));
    }

    // Đăng nhập username không tồn tại
    @Test
    void testLogin_UsernameNotExist() throws Exception {
        String requestBody = """
            {
                "userName": "userNotFound",
                "password": "string"
            }
        """;
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid username or password"));
    }
}
