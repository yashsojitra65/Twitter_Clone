package com.Twitter.com.Services;

import com.Twitter.com.Model.Post;
import com.Twitter.com.Model.User;
import com.Twitter.com.Repositroy.PostRepo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class PostServiceTest {

    @Test
    void createPostSaves() {
        PostRepo postRepo = mock(PostRepo.class);
        PostService service = new PostService(postRepo);
        Post post = new Post();
        when(postRepo.save(any(Post.class))).thenReturn(post);

        Post result = service.CreatePost(post);

        assertSame(post, result);
        verify(postRepo).save(post);
    }

    @Test
    void deletePostRemovesWhenOwnerMatches() {
        PostRepo postRepo = mock(PostRepo.class);
        PostService service = new PostService(postRepo);
        Post post = new Post();
        User owner = new User();
        post.setPostOwner(owner);
        when(postRepo.findById(1)).thenReturn(Optional.of(post));

        String result = service.deletePost(1, owner);

        assertEquals("Removed successfully", result);
        verify(postRepo).deleteById(1);
    }

    @Test
    void deletePostRejectsWhenOwnerMismatchOrMissing() {
        PostRepo postRepo = mock(PostRepo.class);
        PostService service = new PostService(postRepo);
        when(postRepo.findById(1)).thenReturn(Optional.empty());

        String result = service.deletePost(1, new User());

        assertEquals("Post does not exist", result);
        verify(postRepo, never()).deleteById(anyInt());
    }

    @Test
    void validatePostDelegatesExistsById() {
        PostRepo postRepo = mock(PostRepo.class);
        PostService service = new PostService(postRepo);
        Post post = new Post();
        post.setPostId(5);
        when(postRepo.existsById(5)).thenReturn(true);

        assertTrue(service.validatePost(post));
        verify(postRepo).existsById(5);
    }

    @Test
    void getPostByIdReturnsOptionalValue() {
        PostRepo postRepo = mock(PostRepo.class);
        PostService service = new PostService(postRepo);
        Post post = new Post();
        when(postRepo.findById(7)).thenReturn(Optional.of(post));

        assertSame(post, service.getPostById(7));
    }
}
