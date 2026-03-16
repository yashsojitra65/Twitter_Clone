package com.Twitter.com.Services;

import com.Twitter.com.Model.Comment;
import com.Twitter.com.Model.Post;
import com.Twitter.com.Repositroy.CommentRepo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    @Test
    void getCommentCountReturnsSize() {
        CommentRepo repo = mock(CommentRepo.class);
        CommentService service = new CommentService(repo);
        Post post = new Post();
        when(repo.findByTwitterPost(post)).thenReturn(List.of(new Comment(), new Comment(), new Comment()));

        assertEquals(3, service.getCommentCountForPost(post));
    }

    @Test
    void addCommentSavesAndReturnsMessage() {
        CommentRepo repo = mock(CommentRepo.class);
        CommentService service = new CommentService(repo);
        Comment comment = new Comment();

        String result = service.addComment(comment);

        assertEquals("Comment has been added!!!", result);
        verify(repo).save(comment);
    }

    @Test
    void findCommentReturnsOptionalOrNull() {
        CommentRepo repo = mock(CommentRepo.class);
        CommentService service = new CommentService(repo);
        Comment comment = new Comment();
        when(repo.findById(1)).thenReturn(Optional.of(comment));

        assertSame(comment, service.findComment(1));
    }

    @Test
    void removeCommentDeletes() {
        CommentRepo repo = mock(CommentRepo.class);
        CommentService service = new CommentService(repo);
        Comment comment = new Comment();

        service.removeComment(comment);

        verify(repo).delete(comment);
    }
}
