package com.Twitter.com.Repositroy;

import com.Twitter.com.Model.Follow;
import com.Twitter.com.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepo extends JpaRepository<Follow,Integer> {
    List<Follow> findByCurrentUserAndUserFollower(User followTargetUser, User follower);
}
