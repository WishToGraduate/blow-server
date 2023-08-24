package com.blow.server.api.service.Post;


import com.blow.server.api.common.message.ExceptionMessage;
import com.blow.server.api.dto.Post.request.PostCreateRequestDTO;
import com.blow.server.api.dto.Post.request.PostDeleteRequestDTO;
import com.blow.server.api.dto.Post.request.PostEditRequestDTO;
import com.blow.server.api.dto.Post.response.PostResponseDTO;
import com.blow.server.api.entity.Post;
import com.blow.server.api.repository.PostRepository;
import com.blow.server.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService{
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public PostResponseDTO getPosts(){
        val postList = postRepository.findAll();
        return PostResponseDTO.of(postList);
    }

    @Override
    public PostResponseDTO getPostsByCategory(String category){
        val postList = postRepository.findPostByCategory(category);
        return PostResponseDTO.of(postList);
    }

    @Override
    @Transactional
    public void createPost(Long userId, PostCreateRequestDTO request){
        val user = userRepository.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ExceptionMessage.NOT_FOUND_USER.getMessage()));

        postRepository.save(Post.builder()
                .title(request.title())
                .content(request.content())
                .category(request.Category())
                .users(user)
                .duedate(request.duedate())
                .build());
    }

    @Override
    @Transactional
    public void deletePost(Long userId, PostDeleteRequestDTO request){
        val postId = request.postId();
        val user = userRepository.getUserById(userId)
                .orElseThrow(()-> new EntityNotFoundException(ExceptionMessage.NOT_FOUND_USER.getMessage()));

        val post = postRepository.getPostById(postId)
                .orElseThrow(()-> new EntityNotFoundException(ExceptionMessage.NOT_FOUND_POST.getMessage()));

        if(!isOwner(post,user.getId())){
            throw new EntityNotFoundException(ExceptionMessage.NOT_POST_OWNER.getMessage());
        }

        postRepository.deleteById(postId);
    }

    @Override
    @Transactional
    public void updatePost(Long userId, PostEditRequestDTO request){
        val postId = request.postId();
        System.out.println(postId);
        val post = postRepository.getPostById(postId)
                .orElseThrow(()-> new EntityNotFoundException(ExceptionMessage.NOT_FOUND_POST.getMessage()));
        val user = userRepository.getUserById(userId)
                .orElseThrow(()-> new EntityNotFoundException(ExceptionMessage.NOT_FOUND_USER.getMessage()));

        if(!isOwner(post,user.getId())){
            throw new EntityNotFoundException(ExceptionMessage.NOT_POST_OWNER.getMessage());
        }
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setCategory(request.Category());
        post.setDuedate(request.duedate());
    }

    private boolean isOwner(Post post, Long userId){
        if (!post.isOwner(userId)){
            return false;
        }
        return true;
    }
}
