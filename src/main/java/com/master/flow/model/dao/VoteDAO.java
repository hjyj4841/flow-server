package com.master.flow.model.dao;

import com.master.flow.model.vo.Vote;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteDAO extends JpaRepository<Vote, Integer> {

    // 로그인한 사람의 투표체크 여부
    @Query(value = "SELECT * FROM vote WHERE user_code = :userCode AND vote_yn = 'y' OR vote_yn = 'n'" , nativeQuery = true)
    Vote check(@Param("userCode") int userCode);

    // 투표게시물 전제 투표 수
    @Query(value = "SELECT count(*) FROM vote WHERE post_code = :postCode" , nativeQuery = true)
    int count(@Param("postCode") int postCode);

    // 투표게시물 찬성 투표 수
    @Query(value = "SELECT count(*) FROM vote WHERE vote_yn = 'Y' and post_code = :postCode", nativeQuery = true)
    int countY(@Param("postCode") int postCode);

    // 투표게시물 반대 투표 수
    @Query(value = "SELECT count(*) FROM vote WHERE vote_yn = 'N' and post_code = :postCode", nativeQuery = true)
    int countN(@Param("postCode") int postCode);

    // 투표게시물 삭제
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM vote WHERE post_code = :postCode",nativeQuery = true)
    public void deleteVoteByPostCode(@Param("postCode") int postCode);
}
