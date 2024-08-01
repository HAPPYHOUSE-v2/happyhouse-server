package com.ormi.happyhouse.post.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    private String fileName;

    private String fileType;

    private String fileUrl;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
}
