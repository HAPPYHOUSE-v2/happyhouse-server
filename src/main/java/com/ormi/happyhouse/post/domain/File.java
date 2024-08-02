package com.ormi.happyhouse.post.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "files")
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