package com.Twitter.com.Services;

import com.Twitter.com.Model.Like;
import com.Twitter.com.Model.Post;
import com.Twitter.com.Model.User;
import com.Twitter.com.Repositroy.LikeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikeService {
    @Autowired
    LikeRepo likeRepo;
    public boolean isLikeAllowedOnThisPost(Post twitterPost, User liker) {
        List<Like> likeList = likeRepo.findByTwitterPostAndLiker(twitterPost,liker);
        return likeList != null && likeList.isEmpty();
    }

    public String addLike(Like like) {
        likeRepo.save(like);
        return "Twitter post liked successfully!!!";
    }
    public Integer getLikeCountForPost(Post validPost) {
        return likeRepo.findByTwitterPost(validPost).size();
    }

    public Like findLike(Integer likeId) {
        return likeRepo.findById(likeId).orElse(null);
    }

    public void removeLike(Like like) {
        likeRepo.delete(like);
    }


}
