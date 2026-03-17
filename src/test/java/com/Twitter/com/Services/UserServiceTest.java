package com.Twitter.com.Services;

import com.Twitter.com.Model.Post;
import com.Twitter.com.Model.User;
import com.Twitter.com.Model.Enum.PostType;
import com.Twitter.com.Model.dto.CreatePostRequest;
import com.Twitter.com.Model.dto.Credential;
import com.Twitter.com.Model.dto.FollowRequest;
import com.Twitter.com.Model.dto.LikeRequest;
import com.Twitter.com.Repositroy.AdminRepo;
import com.Twitter.com.Repositroy.PostRepo;
import com.Twitter.com.Repositroy.UserRepo;
import com.Twitter.com.Services.utility.PasswordEncrypter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepo userRepo;
    @Mock
    PostService postService;
    @Mock
    LikeService likeService;
    @Mock
    FollowService followService;
    @Mock
    CommentService commentService;
    @Mock
    EmailService emailService;
    @Mock
    PostRepo postRepo;
    @Mock
    AdminRepo adminRepo;
    @Mock
    OtpService otpService;

    @InjectMocks
    UserService userService;

    @Test
    void signUpReturnsAlreadyRegisterWhenEmailExists() throws Exception {
        User user = new User();
        user.setUserEmail("test@example.com");
        when(userRepo.existsByuserEmail("test@example.com")).thenReturn(true);

        String result = userService.SignUp(user);

        assertEquals("Already Register", result);
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void signUpHashesPasswordAndSavesUser() throws Exception {
        User user = new User();
        user.setUserEmail("test@example.com");
        user.setUserPassword("plain");
        when(userRepo.existsByuserEmail("test@example.com")).thenReturn(false);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = userService.SignUp(user);

        assertEquals("Register Successfully", result);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(PasswordEncrypter.hashPasswordWithStaticSecret("plain"), saved.getUserPassword());
    }

    @Test
    void signInSuccessSetsLoginStatus() throws Exception {
        Credential credential = new Credential("test@example.com", "plain");
        User user = new User();
        user.setUserEmail("test@example.com");
        user.setUserPassword(PasswordEncrypter.hashPasswordWithStaticSecret("plain"));
        when(userRepo.existsByuserEmail("test@example.com")).thenReturn(true);
        when(userRepo.findByUserEmail("test@example.com")).thenReturn(user);

        String result = userService.SignIn(credential);

        assertEquals("login", result);
        assertEquals("login", user.getStatus());
        verify(userRepo).save(user);
    }

    @Test
    void signInReturnsMismatchOnWrongPassword() throws Exception {
        Credential credential = new Credential("test@example.com", "plain");
        User user = new User();
        user.setUserEmail("test@example.com");
        user.setUserPassword(PasswordEncrypter.hashPasswordWithStaticSecret("other"));
        when(userRepo.existsByuserEmail("test@example.com")).thenReturn(true);
        when(userRepo.findByUserEmail("test@example.com")).thenReturn(user);

        String result = userService.SignIn(credential);

        assertEquals("Credential MisMatch", result);
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void resetPasswordStoresOtpInRedisAndEmails() {
        String email = "test@example.com";
        when(userRepo.existsByuserEmail(email)).thenReturn(true);

        String result = userService.resetPassWord(email);

        assertEquals("Otp sent Successfully", result);
        ArgumentCaptor<String> otpCaptorStore = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> otpCaptorEmail = ArgumentCaptor.forClass(String.class);
        verify(otpService).storeOtp(eq(email), otpCaptorStore.capture());
        verify(emailService).sendOtpEmail(eq(email), otpCaptorEmail.capture());
        assertEquals(otpCaptorStore.getValue(), otpCaptorEmail.getValue());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void verifyOtpUpdatesPasswordAndLogsOut() throws Exception {
        String email = "test@example.com";
        User user = new User();
        user.setUserEmail(email);
        when(userRepo.findByUserEmail(email)).thenReturn(user);
        when(otpService.validateOtp(email, "123456")).thenReturn(true);

        String result = userService.verifyOTP(email, "123456", "newpass");

        assertEquals("PassWord Successfully Save", result);
        assertEquals("logOut", user.getStatus());
        assertEquals(PasswordEncrypter.hashPasswordWithStaticSecret("newpass"), user.getUserPassword());
        verify(userRepo).save(user);
    }

    @Test
    void verifyOtpRejectsInvalidCode() throws Exception {
        String email = "test@example.com";
        User user = new User();
        user.setUserEmail(email);
        when(userRepo.findByUserEmail(email)).thenReturn(user);
        when(otpService.validateOtp(email, "bad")).thenReturn(false);

        String result = userService.verifyOTP(email, "bad", "newpass");

        assertEquals("Invalid or expired OTP", result);
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void createPostRejectsWhenUserNotLoggedIn() {
        String email = "test@example.com";
        User user = new User();
        user.setStatus("logout");
        when(userRepo.findByUserEmail(email)).thenReturn(user);

        CreatePostRequest req = new CreatePostRequest();
        req.setTitle("title");

        String result = userService.CreatePost(req, email);

        assertEquals("Please signIn first", result);
        verify(postService, never()).CreatePost(any(Post.class));
        assertEquals(0, user.getTotal());
    }

    @Test
    void createPostSetsOwnerAndIncrementsTotal() {
        String email = "test@example.com";
        User user = new User();
        user.setStatus("login");
        when(userRepo.findByUserEmail(email)).thenReturn(user);

        CreatePostRequest req = new CreatePostRequest();
        req.setTitle("title");
        req.setDescription("desc");
        req.setUrl("http://u");
        req.setPostType(PostType.IMAGE);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        when(postService.CreatePost(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = userService.CreatePost(req, email);

        assertEquals("Post Upload Successfully", result);
        verify(postService).CreatePost(postCaptor.capture());
        Post saved = postCaptor.getValue();
        assertEquals(user, saved.getPostOwner());
        assertEquals("title", saved.getTitle());
        assertEquals("desc", saved.getDescription());
        assertEquals("http://u", saved.getUrl());
        assertEquals(PostType.IMAGE, saved.getPostType());
        assertEquals(1, user.getTotal());
    }

    @Test
    void addLikeFailsWhenNotLoggedIn() {
        String email = "test@example.com";
        User user = new User();
        user.setStatus("logout");
        when(userRepo.findByUserEmail(email)).thenReturn(user);

        LikeRequest likeRequest = new LikeRequest();
        likeRequest.setPostId(1);

        String result = userService.addLike(likeRequest, email);

        assertEquals("Please signIn first", result);
        verify(likeService, never()).addLike(any());
    }

    @Test
    void addLikeSucceedsAndCreatesLike() {
        String email = "test@example.com";
        User user = new User();
        user.setStatus("login");
        when(userRepo.findByUserEmail(email)).thenReturn(user);

        Post post = new Post();
        post.setPostId(1);
        when(postService.getPostById(1)).thenReturn(post);
        when(postService.validatePost(post)).thenReturn(true);
        when(likeService.isLikeAllowedOnThisPost(post, user)).thenReturn(true);
        when(likeService.addLike(any())).thenReturn("Twitter post liked successfully!!!");

        LikeRequest likeRequest = new LikeRequest();
        likeRequest.setPostId(1);

        String result = userService.addLike(likeRequest, email);

        assertEquals("Twitter post liked successfully!!!", result);
        verify(likeService).addLike(any());
    }

    @Test
    void followUserFailsWhenNotLoggedIn() {
        String email = "follower@example.com";
        User follower = new User();
        follower.setStatus("logout");
        when(userRepo.findByUserEmail(email)).thenReturn(follower);

        FollowRequest req = new FollowRequest();
        req.setTargetUserId(2L);

        String result = userService.FollowUser(req, email);

        assertEquals("Please signIn first", result);
        verify(followService, never()).startFollowing(any(), any());
    }

    @Test
    void followUserFailsWhenTargetMissing() {
        String email = "follower@example.com";
        User follower = new User();
        follower.setStatus("login");
        when(userRepo.findByUserEmail(email)).thenReturn(follower);
        when(userRepo.findById(99L)).thenReturn(java.util.Optional.empty());

        FollowRequest req = new FollowRequest();
        req.setTargetUserId(99L);

        String result = userService.FollowUser(req, email);

        assertEquals("User to be followed is Invalid!!!", result);
        verify(followService, never()).startFollowing(any(), any());
    }

    @Test
    void followUserSucceeds() {
        String email = "follower@example.com";
        User follower = new User();
        follower.setStatus("login");
        follower.setGetUserHandle("@me");
        User target = new User();
        target.setUserid(2L);
        target.setGetUserHandle("@you");

        when(userRepo.findByUserEmail(email)).thenReturn(follower);
        when(userRepo.findById(2L)).thenReturn(java.util.Optional.of(target));
        when(followService.isFollowAllowed(target, follower)).thenReturn(true);

        FollowRequest req = new FollowRequest();
        req.setTargetUserId(2L);

        String result = userService.FollowUser(req, email);

        assertEquals("@me is now following @you", result);
        verify(followService).startFollowing(any(), eq(follower));
    }
}
