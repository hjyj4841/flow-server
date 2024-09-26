package com.master.flow.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Post {
    @Id
    @Column(name="POST_CODE")
    private int postCode;

//    게시물 구분 (착장룩, 투표)
    @Column(name="POST_TYPE")
    private String postType;

//    게시물 내용
    @Column(name="POST_DESC")
    private String postDesc;

//    작성 날짜 => 데이터타입 수정해야할지도
    @Column(name="POST_DATE")
    private LocalDateTime postDate;

//    공개 여부
    @Column(name="POST_PUBLIC_YN")
    private String postPublicYn;

//    유저 코드
    @Column(name="USER_CODE")
    private int userCode;
}
