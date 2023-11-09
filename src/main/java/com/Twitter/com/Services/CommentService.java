package com.Twitter.com.Services;

import com.Twitter.com.Model.Comment;
import com.Twitter.com.Model.Post;
import com.Twitter.com.Repositroy.CommentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    @Autowired
    CommentRepo commentRepo;

    public Integer getCommentCountForPost(Post validPost) {
        return commentRepo.findByTwitterPost(validPost).size();
    }

    public String addComment(Comment comment) {
        comment.setTime(comment.getTime());
        commentRepo.save(comment);
        return "Comment has been added!!!";
    }

    public Comment findComment(Integer commentId) {
        return  commentRepo.findById(commentId).orElse(null);
    }

    public void removeComment(Comment comment) {
        commentRepo.delete(comment);
    }

}
