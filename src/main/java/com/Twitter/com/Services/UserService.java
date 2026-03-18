package com.Twitter.com.Services;

import com.Twitter.com.Model.*;
import com.Twitter.com.Model.Enum.PostType;
import com.Twitter.com.Model.dto.CommentRequest;
import com.Twitter.com.Model.dto.CreatePostRequest;
import com.Twitter.com.Model.dto.Credential;
import com.Twitter.com.Model.dto.FollowRequest;
import com.Twitter.com.Model.dto.LikeRequest;
import com.Twitter.com.Model.dto.PostDto;
import com.Twitter.com.Repositroy.AdminRepo;
import com.Twitter.com.Repositroy.PostRepo;
import com.Twitter.com.Repositroy.UserRepo;
import com.Twitter.com.Services.utility.OTPGenerator;
import com.Twitter.com.Services.utility.PasswordEncrypter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final PostService postService;
    private final LikeService likeService;
    private final FollowService followService;
    private final CommentService commentService;
    private final EmailService emailService;
    private final PostRepo postRepo;
    private final AdminRepo adminRepo;
    private final OtpService otpService;
    private final LoginStatusService loginStatusService;

    public String SignUp(User user) throws NoSuchAlgorithmException {

        if (userRepo.existsByuserEmail(user.getUserEmail())) {
            return "Already Register";
        }

        String hashPass = PasswordEncrypter.hashPasswordWithStaticSecret(user.getUserPassword());
        user.setUserPassword(hashPass);
        userRepo.save(user);
        return "Register Successfully";

    }

    public String SignIn(Credential credential) throws NoSuchAlgorithmException {
        if (!userRepo.existsByuserEmail(credential.getEmail())) {
            return "Please Create a Account";
        }
        String hashPass = PasswordEncrypter.hashPasswordWithStaticSecret(credential.getPassword());
        User user = userRepo.findByUserEmail(credential.getEmail());

        if (hashPass.equals(user.getUserPassword())) {
            loginStatusService.markLogin(user.getUserEmail());
            return "login";
        }
        return "Credential MisMatch";
    }

    public String SignOut(String email) {
        User user = userRepo.findByUserEmail(email);
        if (isLoggedIn(user, email)) {
            loginStatusService.markLogout(email);
        } else {
            return "Please signIn first";
        }
        return "User Signed out successfully";
    }

    public String CreatePost(CreatePostRequest createRequest, String email) {
        User user = userRepo.findByUserEmail(email);
        if (user != null && user.getTotalPost() == null) {
            user.setTotalPost(0);
        }
        if (!isLoggedIn(user, email)) {
            return "Please signIn first";
        }

        Post post = new Post();
        post.setTitle(createRequest.getTitle());
        post.setDescription(createRequest.getDescription());
        post.setUrl(createRequest.getUrl());
        post.setPostType(createRequest.getPostType());
        post.setPostOwner(user);

        user.setTotalPost(user.getTotalPost() + 1);
        postService.CreatePost(post);
        return "Post Upload Successfully";
    }

    public String deletePost(Integer postId, String email) {
        User user = userRepo.findByUserEmail(email);
        if (!isLoggedIn(user, email)) {
            return "Please signIn first";
        }
        if (user.getTotalPost() == null || user.getTotalPost() <= 0) {
            return "No posts to delete";
        }
        user.setTotalPost(user.getTotalPost() - 1);
        postService.deletePost(postId, user);
        return "Post Deleted Successfully";
    }


    public String addLike(LikeRequest likeRequest, String likeEmail) {
        User liker = userRepo.findByUserEmail(likeEmail);
        if (!isLoggedIn(liker, likeEmail)) {
            return "Please signIn first";
        }

        Post twitterPost = postService.getPostById(likeRequest.getPostId());
        if (twitterPost == null || !postService.validatePost(twitterPost)) {
            return "Cannot like on Invalid Post!!";
        }

        if (likeService.isLikeAllowedOnThisPost(twitterPost, liker)) {
            Like like = new Like();
            like.setTwitterPost(twitterPost);
            like.setLiker(liker);
            return likeService.addLike(like);
        } else {
            return "Already Liked!!";
        }
    }

    public String totalLike(Integer postId) {
        Post validPost = postService.getPostById(postId);

        if (validPost != null) {
            Integer likeCountForPost = likeService.getLikeCountForPost(validPost);
            return String.valueOf(likeCountForPost);
        } else {
            return "Cannot like on Invalid Post!!";
        }
    }

    public String totalComment(Integer postId) {
        Post validPost = postService.getPostById(postId);

        if (validPost != null) {
            Integer CommentCountForPost = commentService.getCommentCountForPost(validPost);
            return String.valueOf(CommentCountForPost);
        } else {
            return "Cannot Comment on Invalid Post!!";
        }
    }

    public String deleteLike(Integer likeId, String likerEmail) {
        Like like = likeService.findLike(likeId);
        if (like != null) {
            if (authorizeLikeRemover(likerEmail, like)) {
                likeService.removeLike(like);
                return "like deleted successfully";
            } else {
                return "Like is already detected...Not allowed!!!!";
            }
        }
        return "Invalid like";
    }

    private boolean authorizeLikeRemover(String likerEmail, Like like) {
        String likeOwnerEmail = like.getLiker().getUserEmail();
        return likerEmail.equals(likeOwnerEmail);
    }

    public String FollowUser(FollowRequest followRequest, String followerEmail) {
        User follower = userRepo.findByUserEmail(followerEmail);
        if (!isLoggedIn(follower, followerEmail)) {
            return "Please signIn first";
        }

        User followTargetUser = userRepo.findById(followRequest.getTargetUserId()).orElse(null);
        if (followTargetUser == null) {
            return "User to be followed is Invalid!!!";
        }

        if (followService.isFollowAllowed(followTargetUser, follower)) {
            Follow follow = new Follow();
            follow.setCurrentUser(followTargetUser);
            followService.startFollowing(follow, follower);
            return follower.getGetUserHandle() + " is now following " + followTargetUser.getGetUserHandle();
        } else {
            return follower.getUserHandle + " already follows " + followTargetUser.getUserHandle;
        }
    }

    public String unFollowUser(Integer followId, String followerEmail) {
        Follow follow = followService.findFollow(followId);
        if (follow != null) {
            if (authorizeUnfollow(followerEmail, follow)) {
                followService.unfollow(follow);
                return follow.getCurrentUser().getGetUserHandle() + " Unfollowing " + follow.getUserFollower().getGetUserHandle();
            } else {
                return "Unauthorized unfollow detected...Not allowed!!!!";
            }
        } else {
            return "Invalid follow mapping";
        }
    }

    private boolean authorizeUnfollow(String email, Follow follow) {
        String targetEmail = follow.getCurrentUser().getUserEmail();
        String followerEmail = follow.getUserFollower().getUserEmail();

        return targetEmail.equals(email) || followerEmail.equals(email);
    }

    public String addComment(CommentRequest commentRequest, String commenterEmail) {
        User commenter = userRepo.findByUserEmail(commenterEmail);
        if (!isLoggedIn(commenter, commenterEmail)) {
            return "Please signIn first";
        }

        Post post = postService.getPostById(commentRequest.getPostId());
        if (post == null || !postService.validatePost(post)) {
            return "Cannot comment on Invalid Post!!";
        }

        Comment comment = new Comment();
        comment.setCommenter(commenter);
        comment.setTwitterPost(post);
        comment.setCommentBody(commentRequest.getText());

        return commentService.addComment(comment);
    }


    public String removeComment(Integer commentId, String email) {
        Comment comment = commentService.findComment(commentId);
        if (comment != null) {
            if (authorizeCommentRemover(email, comment)) {
                commentService.removeComment(comment);
                return "comment deleted successfully";
            } else {
                return "Unauthorized delete detected...Not allowed!!!!";
            }

        } else {
            return "Invalid Comment";
        }
    }

    private boolean authorizeCommentRemover(String email, Comment comment) {
        String commentOwnerEmail = comment.getCommenter().getUserEmail();
        String postOwnerEmail = comment.getTwitterPost().getPostOwner().getUserEmail();

        return postOwnerEmail.equals(email) || commentOwnerEmail.equals(email);
    }

    private boolean isLoggedIn(User user, String email) {
        if (user == null) {
            return false;
        }
        String emailToCheck = user.getUserEmail() != null ? user.getUserEmail() : email;
        if (emailToCheck == null) {
            return false;
        }
        return loginStatusService.isLoggedIn(emailToCheck);
    }

    public String resetPassWord(String email) {
        if (!userRepo.existsByuserEmail(email)) {
            return "Register First";
        }
//        User user = userRepo.findByUserEmail(email);
        String otp = OTPGenerator.generateOTP();
        otpService.storeOtp(email, otp);
        emailService.sendOtpEmail(email, otp);
        return "Otp sent Successfully";
    }

    public String verifyOTP(String email, String otp, String newPassword) throws NoSuchAlgorithmException {
        User user = userRepo.findByUserEmail(email);
        if (otpService.validateOtp(email, otp)) {
            String newHashPassWord = PasswordEncrypter.hashPasswordWithStaticSecret(newPassword);
            user.setUserPassword(newHashPassWord);
            userRepo.save(user);
            loginStatusService.markLogout(email);
            return "Password Successfully Save";
        }
        return "Invalid or expired OTP";
    }

    public Page<PostDto> showPost(String email, Pageable pageable) {
        Page<Post> posts = postService.getPostsByOwnerEmail(email, pageable);
        return posts.map(post -> new PostDto(post.getTitle(), post.getDescription(), post.getUrl(), post.getTime(), post.getPostOwner().getUserName(), post.getPostType()));
    }

    public Page<PostDto> showAllPosts(PostType type, Pageable pageable) {
        Page<Post> posts = postService.getAllPosts(type, pageable);
        return posts.map(post -> new PostDto(post.getTitle(), post.getDescription(), post.getUrl(), post.getTime(), post.getPostOwner().getUserName(), post.getPostType()));
    }

    public User getUserById(Long userId) {
        return userRepo.findById(userId).orElse(null);
    }

    public String toggleBlueTick(Long id, boolean blueTick) {
        User user = userRepo.findById(id).orElse(null);
        Admin admin = new Admin();

        if (user != null && blueTick == true) {
            user.setIsBlueTicked("✅");
            admin.setEmail(user.getUserEmail());
            admin.setUserName(user.getUserName());
            adminRepo.save(admin);
            userRepo.save(user);
            return "Blue tick was set..";
        } else if (user != null && blueTick == false) {
            user.setIsBlueTicked("Not Verified");
            userRepo.save(user);
            return "Blue tick was not set..";
        }
        return "user doesn't exist";
    }
}
