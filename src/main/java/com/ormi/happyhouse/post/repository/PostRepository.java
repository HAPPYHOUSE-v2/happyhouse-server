package com.ormi.happyhouse.post.repository;

import com.ormi.happyhouse.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByDeleteYn(boolean deleteYn, Pageable pageable);
}
