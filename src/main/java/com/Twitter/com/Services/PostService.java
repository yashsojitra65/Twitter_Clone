package com.Twitter.com.Services;

import com.Twitter.com.Model.Post;
import com.Twitter.com.Model.User;
import com.Twitter.com.Model.Enum.PostType;
import com.Twitter.com.Repositroy.PostRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepo postRepo;

    public Post CreatePost(Post post) {
        return postRepo.save(post);
    }

    public String deletePost(Integer postId, User user) {
        Post post = postRepo.findById(postId).orElse(null);
        if (post != null && post.getPostOwner().equals(user)) {
            postRepo.deleteById(postId);
            return "Removed successfully";
        }
        return "Post does not exist";
    }

    public boolean validatePost(Post twitterPost) {
        return (twitterPost != null && postRepo.existsById(twitterPost.getPostId()));
    }
    public Post getPostById(Integer postId) {
        return postRepo.findById(postId).orElse(null);
    }

    public Page<Post> getPostsByOwnerEmail(String email, Pageable pageable) {
        return postRepo.findByPostOwnerUserEmail(email, pageable);
    }

    public Page<Post> getAllPosts(PostType type, Pageable pageable) {
        if (type != null) {
            return postRepo.findByPostType(type, pageable);
        }
        return postRepo.findAll(pageable);
    }

}
