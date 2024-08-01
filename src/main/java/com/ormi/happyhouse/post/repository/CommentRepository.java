package com.ormi.happyhouse.post.repository;

import com.ormi.happyhouse.post.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
