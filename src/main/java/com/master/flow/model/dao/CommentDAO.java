package com.master.flow.model.dao;

import com.master.flow.model.vo.Comment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentDAO extends JpaRepository<Comment, Integer> {
    // POST_CODE로 조회
    @Query(value="SELECT * FROM comment WHERE POST_CODE = :postCode",nativeQuery = true)
    List<Comment> findByPostCode(@Param("postCode") int postCode);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM comment WHERE post_code =:postCode",nativeQuery = true)
    void deleteCommentByPostCode(@Param("postCode") int postCode);
}
