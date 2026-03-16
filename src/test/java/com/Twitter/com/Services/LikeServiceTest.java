package com.Twitter.com.Services;

import com.Twitter.com.Model.Like;
import com.Twitter.com.Model.Post;
import com.Twitter.com.Model.User;
import com.Twitter.com.Repositroy.LikeRepo;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LikeServiceTest {

    @Test
    void isLikeAllowedWhenNoExistingLike() {
        LikeRepo repo = mock(LikeRepo.class);
        LikeService service = new LikeService(repo);
        Post post = new Post();
        User user = new User();
        when(repo.findByTwitterPostAndLiker(post, user)).thenReturn(Collections.emptyList());

        assertTrue(service.isLikeAllowedOnThisPost(post, user));
    }

    @Test
    void isLikeAllowedFalseWhenExisting() {
        LikeRepo repo = mock(LikeRepo.class);
        LikeService service = new LikeService(repo);
        Post post = new Post();
        User user = new User();
        when(repo.findByTwitterPostAndLiker(post, user)).thenReturn(List.of(new Like()));

        assertFalse(service.isLikeAllowedOnThisPost(post, user));
    }

    @Test
    void addLikeSavesAndReturnsMessage() {
        LikeRepo repo = mock(LikeRepo.class);
        LikeService service = new LikeService(repo);
        Like like = new Like();

        String result = service.addLike(like);

        assertEquals("Twitter post liked successfully!!!", result);
        verify(repo).save(like);
    }

    @Test
    void getLikeCountUsesRepoSize() {
        LikeRepo repo = mock(LikeRepo.class);
        LikeService service = new LikeService(repo);
        Post post = new Post();
        when(repo.findByTwitterPost(post)).thenReturn(List.of(new Like(), new Like()));

        assertEquals(2, service.getLikeCountForPost(post));
    }

    @Test
    void findLikeReturnsValueOrNull() {
        LikeRepo repo = mock(LikeRepo.class);
        LikeService service = new LikeService(repo);
        Like like = new Like();
        when(repo.findById(1)).thenReturn(Optional.of(like));

        assertSame(like, service.findLike(1));
    }

    @Test
    void removeLikeDeletes() {
        LikeRepo repo = mock(LikeRepo.class);
        LikeService service = new LikeService(repo);
        Like like = new Like();

        service.removeLike(like);

        verify(repo).delete(like);
    }
}
