package com.Twitter.com.Controller;

import com.Twitter.com.Model.Enum.PostType;
import com.Twitter.com.Model.dto.FollowRequest;
import com.Twitter.com.Model.dto.CreatePostRequest;
import com.Twitter.com.Model.dto.Credential;
import com.Twitter.com.Model.dto.PostDto;
import com.Twitter.com.Services.FollowService;
import com.Twitter.com.Services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private FollowService followService;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService, followService)).build();
    }

    @Test
    void signUpDelegatesToService() throws Exception {
        Mockito.when(userService.SignUp(any())).thenReturn("Register Successfully");

        mockMvc.perform(post("/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userEmail\":\"test@example.com\",\"userPassword\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Register Successfully")));
    }

    @Test
    void signInReturnsServiceResult() throws Exception {
        Mockito.when(userService.SignIn(any(Credential.class))).thenReturn("login");

        Credential credential = new Credential("test@example.com", "pass");
        mockMvc.perform(post("/user/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credential)))
                .andExpect(status().isOk())
                .andExpect(content().string("login"));
    }

    @Test
    void resetPasswordEndpoint() throws Exception {
        Mockito.when(userService.resetPassWord("test@example.com")).thenReturn("Otp sent Successfully");

        mockMvc.perform(post("/user/resetpass")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Otp sent Successfully"));
    }

    @Test
    void verifyOtpEndpoint() throws Exception {
        Mockito.when(userService.verifyOTP("test@example.com", "123456", "newpass"))
                .thenReturn("PassWord Successfully Save");

        mockMvc.perform(put("/user/verifyOTP")
                        .param("email", "test@example.com")
                        .param("otp", "123456")
                        .param("newPassword", "newpass"))
                .andExpect(status().isOk())
                .andExpect(content().string("PassWord Successfully Save"));
    }

    @Test
    void showPostReturnsPagedList() throws Exception {
        List<PostDto> posts = Arrays.asList(
                new PostDto("t1", "d1", "u1", "now", "alice", PostType.TEXT),
                new PostDto("t2", "d2", "u2", "later", "bob", PostType.IMAGE)
        );
        Mockito.when(userService.showPost(Mockito.eq("test@example.com"), Mockito.any()))
                .thenReturn(new PageImpl<>(posts, PageRequest.of(0, 10), posts.size()));

        mockMvc.perform(get("/user/showpost/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("t1"))
                .andExpect(jsonPath("$.content[1].userName").value("bob"));
    }

    @Test
    void createPostDelegates() throws Exception {
        Mockito.when(userService.CreatePost(any(CreatePostRequest.class), eq("test@example.com")))
                .thenReturn("Post Upload Successfully");

        mockMvc.perform(post("/user/post")
                        .param("email", "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Post Upload Successfully"));
    }

    @Test
    void followDelegates() throws Exception {
        Mockito.when(userService.FollowUser(any(FollowRequest.class), eq("test@example.com")))
                .thenReturn("ok");

        mockMvc.perform(post("/user/follow")
                        .param("followerEmail", "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetUserId\":2}"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }
}
