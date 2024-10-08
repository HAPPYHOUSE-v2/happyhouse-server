package com.ormi.happyhouse.post.repository;

import com.ormi.happyhouse.post.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
