package com.ormi.happyhouse.post.domain;

import com.ormi.happyhouse.member.domain.Users;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "comments")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = false)
    private boolean deleteYn;
}