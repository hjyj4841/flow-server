package com.master.flow.model.dao;

import com.master.flow.model.vo.Post;
import com.master.flow.model.vo.PostTag;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostTagDAO extends JpaRepository<PostTag, Integer> {

    // 태그 코드로 게시물 조회
    @Query("SELECT pt.post FROM PostTag pt WHERE pt.tag.tagCode = :tagCode")
    List<Post> findPostsByTagCode(@Param("tagCode") int tagCode);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM post_tag WHERE post_code = :postCode",nativeQuery = true)
    public void deletePostTagByPostCode(@Param("postCode") int postCode);
}
