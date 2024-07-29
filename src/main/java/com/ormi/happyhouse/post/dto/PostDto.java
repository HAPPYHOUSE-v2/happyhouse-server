package com.ormi.happyhouse.post.dto;

import com.ormi.happyhouse.post.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

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
