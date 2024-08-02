package com.ormi.happyhouse.post.service;

import com.ormi.happyhouse.member.domain.Users;
import com.ormi.happyhouse.member.jwt.JwtUtil;
import com.ormi.happyhouse.member.repository.UsersRepository;
import com.ormi.happyhouse.post.domain.File;
import com.ormi.happyhouse.post.domain.Post;
import com.ormi.happyhouse.post.dto.PostDto;
import com.ormi.happyhouse.post.repository.FileRepository;
import com.ormi.happyhouse.post.repository.PostRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UsersRepository usersRepository;
    private final FileRepository fileRepository;
    private final JwtUtil jwtUtil;
    private final S3UploadService s3UploadService;

    // Create: 게시글 생성 메서드
    public void savePost(PostDto postDto, MultipartFile file, String authHeader) throws IOException {
        postDto.setCreatedAt(new Date());
        postDto.setViewCount(0L);


        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);


        Users users = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        postDto.setUsers(users);

        if (file != null && !file.isEmpty()) {
            File newFile = new File().builder()
                    .fileUrl(s3UploadService.saveFile(file))
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .post(postRepository.save(postDto.toEntity()))
                    .build();
            fileRepository.save(newFile);
        } else {
            postRepository.save(postDto.toEntity());
        }
    }

    // Read: 게시글 목록 조회 메서드
    public Page<PostDto> showAllPost(String title, Pageable pageable) {
        Page<Post> postListPage = postRepository.findByDeleteYnFalseAndTitleContains(title, pageable);
        return postListPage.map(PostDto::fromEntity);
    }

    // Read:  게시글 상세 조회 메서드
    public PostDto showPostDetail(Long postId) {
        Optional<Post> postById = postRepository.findById(postId);

        // view_count 1 증가
        Post viewCountPost = viewCountPlusPost(postById);
        postRepository.save(viewCountPost);

        return PostDto.fromEntity(viewCountPost);
    }

    // Read_Method: view_count 1증가 시키는 메서드
    public Post viewCountPlusPost(Optional<Post> postById) {
        Post post = postById.orElseThrow(() -> new IllegalArgumentException("post not found"));

        return new Post().builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount() + 1)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .noticeYn(post.isNoticeYn())
                .deleteYn(post.isDeleteYn())
                .comments(post.getComments())
                .files(post.getFiles())
                .users(post.getUsers())
                .build();
    }

    // Update: 게시글 수정 메서드
    public void updatePost(Long postId, PostDto postDto, MultipartFile file) throws IOException {

        Optional<Post> postById = postRepository.findById(postId);
        Post post = postById.orElseThrow(() -> new IllegalArgumentException("post not found"));

        if (file != null && !file.isEmpty()) {
            File newFile = new File().builder()
                    .fileUrl(s3UploadService.saveFile(file))
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .post(post)
                    .build();
            fileRepository.save(newFile);
        }

        Post updatedPost = new Post().builder()
                .postId(post.getPostId())
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(new Date())
                .noticeYn(postDto.isNoticeYn())
                .deleteYn(postDto.isDeleteYn())
                .comments(post.getComments())
                .files(post.getFiles())
                .users(post.getUsers())
                .build();
        postRepository.save(updatedPost);
    }

    // Delete: 게시글 삭제 메서드
    public void deletePost(Long postId, String authHeader) {
        Optional<Post> postById = postRepository.findById(postId);
        Post post = postById.orElseThrow(() -> new IllegalArgumentException("post not found"));

        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);

        if (post.getUsers().getEmail().equals(email)) {
            Post deletedPost = new Post().builder()
                    .postId(post.getPostId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .viewCount(post.getViewCount())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .noticeYn(post.isNoticeYn())
                    .deleteYn(true)
                    .comments(post.getComments())
                    .files(post.getFiles())
                    .users(post.getUsers())
                    .build();
            postRepository.save(deletedPost);
        } throw new IllegalArgumentException("본인이 작성한 게시글만 삭제할 수 있습니다.");
    }


    // 토큰의 user 값과 해당 포스트의 user값이 같은지 확인
    public boolean isYourPost(Long postId, String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // "Bearer " 이후의 토큰 추출
            try {
                if (jwtUtil.validateToken(token)) {
                    String email = jwtUtil.getEmailFromToken(token);
                    Optional<Users> userByPost = usersRepository.findById(postId);
                    Users user = userByPost.orElseThrow(() -> new RuntimeException("User Not Found"));

                    return email.equals(user.getEmail());
                }
            } catch (ExpiredJwtException e) {
                log.info("토큰 만료: {}", e.getMessage());

            } catch (JwtException e) {
                log.info("잘못된 토큰: {}", e.getMessage());

            }

        }
        return false;
    }
}