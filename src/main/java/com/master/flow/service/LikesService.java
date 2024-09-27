package com.master.flow.service;

import com.master.flow.model.dao.LikesDAO;
import com.master.flow.model.dao.PostDAO;
import com.master.flow.model.dao.UserDAO;
import com.master.flow.model.vo.Likes;
import com.master.flow.model.vo.Post;
import com.master.flow.model.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LikesService {

    @Autowired
    private LikesDAO dao;

    @Autowired
    private PostDAO postDAO;

    @Autowired
    private UserDAO userDAO;

    public boolean toggleLikeWithoutUser(User user, Post post) {
        // Post가 데이터베이스에 저장되지 않았다면 저장
        if (!postDAO.existsById(post.getPostCode())) {
            throw new IllegalArgumentException("Post가 존재하지 않습니다.");}
        Optional<Likes> existingLike = dao.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {
            // 이미 좋아요가 눌러져 있는 경우 삭제
            dao.delete(existingLike.get());
            return false;
        } else {
            Likes like = Likes.builder()
                    .user(user)
                    .post(post)
                    .build();
            dao.save(like);
            return true;
        }
    }
    public int countLikesByPost(Post post) {
        return dao.countByPost(post);
    }
}
