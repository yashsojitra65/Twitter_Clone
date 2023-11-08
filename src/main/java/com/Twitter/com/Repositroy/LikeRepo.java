package com.Twitter.com.Repositroy;

import com.Twitter.com.Model.Like;
import com.Twitter.com.Model.Post;
import com.Twitter.com.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface LikeRepo extends JpaRepository<Like,Integer> {
    List<Like> findByTwitterPostAndLiker(Post twitterPost, User liker);
    Collection<Like> findByTwitterPost(Post validPost);
}
