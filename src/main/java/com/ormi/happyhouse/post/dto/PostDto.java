package com.ormi.happyhouse.post.dto;

import java.time.LocalDate;
import java.util.Date;

public class PostDto {

    private Long id;

    private Long userId;

    private String title;

    private String content;

    private Long viewCount;

    private LocalDate createdAt;

    private Date updatedAt;

    private boolean deleteYN;

    private boolean noticeYN;
}
