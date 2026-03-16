package com.Twitter.com.Repositroy;

import com.Twitter.com.Model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepo extends JpaRepository<Post, Integer> {
    List<Post> findByPostOwnerUserEmail(String email);
    Page<Post> findByPostOwnerUserEmail(String email, Pageable pageable);
}
