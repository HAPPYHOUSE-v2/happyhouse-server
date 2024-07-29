package com.ormi.happyhouse.post.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "posts")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private Long viewCount;

    private Date createdAt;

    private Date updatedAt;

    private boolean deleteYn;

    private boolean noticeYn;
}
