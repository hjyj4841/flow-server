package com.master.flow.service;

import com.master.flow.model.dao.PostTagDAO;
import com.master.flow.model.vo.PostTag;
import com.master.flow.model.vo.Post;
import com.master.flow.model.vo.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostTagService {

    @Autowired
    private PostTagDAO postTagDAO;

    // 게시물 태그 추가
    public PostTag addTag(PostTag postTag){
        return postTagDAO.save(postTag);
    }

    // 게시물 삭제
    public void deleteAll(int postCode){
        postTagDAO.deleteById(postCode);
    }
    
    // 태그명으로 게시물 조회
    public List<Post> viewPostsByTagName(String tagName) {

        Tag tag = postTagDAO.findByTagName(tagName);
        if (tag != null) {
            return postTagDAO.findPostsByTagCode(tag.getTagCode());
        }
        return List.of();
    }
}
