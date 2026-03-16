package com.Twitter.com.Controller;

import com.Twitter.com.Model.Post;
import com.Twitter.com.Model.dto.Credential;
import com.Twitter.com.Model.dto.PostDto;
import com.Twitter.com.Services.UserService;
import com.Twitter.com.Services.FollowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private FollowService followService;

    @Test
    void signUpDelegatesToService() throws Exception {
        Mockito.when(userService.SignUp(any())).thenReturn("Register Successfully");

        mockMvc.perform(post("/User/SignUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userEmail\":\"test@example.com\",\"userPassword\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Register Successfully")));
    }

    @Test
    void signInReturnsServiceResult() throws Exception {
        Mockito.when(userService.SignIn(any(Credential.class))).thenReturn("login");

        Credential credential = new Credential("test@example.com", "pass");
        mockMvc.perform(post("/User/SignIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credential)))
                .andExpect(status().isOk())
                .andExpect(content().string("login"));
    }

    @Test
    void resetPasswordEndpoint() throws Exception {
        Mockito.when(userService.resetPassWord("test@example.com")).thenReturn("Otp sent Successfully");

        mockMvc.perform(post("/User/resetPass")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Otp sent Successfully"));
    }

    @Test
    void verifyOtpEndpoint() throws Exception {
        Mockito.when(userService.verifyOTP("test@example.com", "123456", "newpass"))
                .thenReturn("PassWord Successfully Save");

        mockMvc.perform(put("/User/verifyOTP")
                        .param("email", "test@example.com")
                        .param("otp", "123456")
                        .param("newPassword", "newpass"))
                .andExpect(status().isOk())
                .andExpect(content().string("PassWord Successfully Save"));
    }

    @Test
    void showPostReturnsList() throws Exception {
        List<PostDto> posts = Arrays.asList(
                new PostDto("t1", "d1", "u1", "now", "alice"),
                new PostDto("t2", "d2", "u2", "later", "bob")
        );
        Mockito.when(userService.showPost("test@example.com")).thenReturn(posts);

        mockMvc.perform(get("/User/showPost/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("t1"))
                .andExpect(jsonPath("$[1].userName").value("bob"));
    }

    @Test
    void createPostDelegates() throws Exception {
        Mockito.when(userService.CreatePost(any(Post.class), eq("test@example.com")))
                .thenReturn("Post Upload Successfully");

        mockMvc.perform(post("/User/Post")
                        .param("email", "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Post Upload Successfully"));
    }
}
