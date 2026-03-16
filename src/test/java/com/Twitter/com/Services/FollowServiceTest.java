package com.Twitter.com.Services;

import com.Twitter.com.Model.Follow;
import com.Twitter.com.Model.User;
import com.Twitter.com.Repositroy.FollowRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FollowServiceTest {

    private FollowRepo followRepo;
    private EntityManager entityManager;
    private FollowService service;

    @BeforeEach
    void setup() throws Exception {
        followRepo = mock(FollowRepo.class);
        entityManager = mock(EntityManager.class);
        service = new FollowService(followRepo);
        Field emField = FollowService.class.getDeclaredField("entityManager");
        emField.setAccessible(true);
        emField.set(service, entityManager);
    }

    @Test
    void isFollowAllowedWhenNoExistingAndNotSameUser() {
        User target = new User();
        target.setUserid(1L);
        User follower = new User();
        follower.setUserid(2L);
        when(followRepo.findByCurrentUserAndUserFollower(target, follower)).thenReturn(Collections.emptyList());

        assertTrue(service.isFollowAllowed(target, follower));
    }

    @Test
    void isFollowNotAllowedWhenAlreadyFollowing() {
        User target = new User();
        User follower = new User();
        when(followRepo.findByCurrentUserAndUserFollower(target, follower))
                .thenReturn(List.of(new Follow()));

        assertFalse(service.isFollowAllowed(target, follower));
    }

    @Test
    void isFollowNotAllowedWhenSameUser() {
        User same = new User();
        when(followRepo.findByCurrentUserAndUserFollower(same, same)).thenReturn(Collections.emptyList());

        assertFalse(service.isFollowAllowed(same, same));
    }

    @Test
    void startFollowingSetsFollowerAndSaves() {
        Follow follow = new Follow();
        User follower = new User();

        service.startFollowing(follow, follower);

        assertSame(follower, follow.getUserFollower());
        verify(followRepo).save(follow);
    }

    @Test
    void findFollowReturnsOptional() {
        Follow follow = new Follow();
        when(followRepo.findById(1)).thenReturn(Optional.of(follow));

        assertSame(follow, service.findFollow(1));
    }

    @Test
    void unfollowDeletes() {
        Follow follow = new Follow();
        service.unfollow(follow);
        verify(followRepo).delete(follow);
    }

    @Test
    void getTotalFollowUsesQueryResult() {
        User user = new User();
        @SuppressWarnings("unchecked")
        TypedQuery<Long> query = mock(TypedQuery.class);
        when(entityManager.createQuery(any(String.class), eq(Long.class))).thenReturn(query);
        when(query.setParameter("user", user)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(3L);

        assertEquals(3, service.getTotalFollow(user));
    }
}
