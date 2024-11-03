package com.master.flow.service;

import com.master.flow.model.dao.FollowDAO;
import com.master.flow.model.dao.PostDAO;
import com.master.flow.model.dao.PostImgDAO;
import com.master.flow.model.dao.UserDAO;
import com.master.flow.model.dto.FollowDTO;
import com.master.flow.model.dto.PostInfoDTO;
import com.master.flow.model.dto.UserDTO;
import com.master.flow.model.vo.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Service
public class FollowService {
    @Autowired
    private FollowDAO followDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PostDAO postDAO;

    @Autowired
    private PostImgDAO postImgDAO;

    @Autowired
    private JPAQueryFactory queryFactory;

    // 전체 팔로우 테이블 가져오기
    public HashSet<Follow> findAllFollowSet() {
        return followDAO.findAllFollowSet();
    }

    // 로그인된 유저의 프라이머리키와 팔로우할 유저의 프라이머리키를 받아서 객체 생성
    public Follow existFollow(int followingUserCode, int followerUserCode) {
        User followingUser = userDAO.findById(followingUserCode).orElse(null);
        User followerUser = userDAO.findById(followerUserCode).orElse(null);
        Follow follow = Follow.builder()
                .followingUser(followingUser)
                .followerUser(followerUser)
                .build();
        return follow;
    }
    //전체 팔로우 해쉬셋에 existFollow로 새로 생성한 객체의 데이터가 포함되어있는지 확인
    public boolean checkLogic(int followingUserCode, int followerUserCode) {
        return findAllFollowSet().contains(existFollow(followingUserCode, followerUserCode));
    }

    //새로운 팔로우 관계 생성
    public boolean addFollowRelative(int followingUserCode, int followerUserCode) {
        if(checkLogic(followingUserCode, followerUserCode)) {
            return false; // 이미 존재한다면 false 후 컨트롤러로
        } else {
            followDAO.save(existFollow(followingUserCode, followerUserCode));
            return true; // 존재하지 않는다면 새로운 팔로우 관계 생성하고 컨트롤러로 ㄱㄱ
        }
    }
    // 언팔로우
    public boolean unFollow(int followingUserCode, int followerUserCode) {
        if(checkLogic(followingUserCode, followerUserCode)) {
            followDAO.delete(existFollow(followingUserCode, followerUserCode));
            return true; // 객체 존재여부 메서드로 참일시 관계 삭제 후 컨트롤러로
        } else {
            return false; // 아니면 false하고 컨트롤러 ㄱㄱ
        }
    }


    public BooleanBuilder followBuilder(String key, List<User> list) {
        QUser qUser = QUser.user;  // QueryDSL로 생성된 QUser 객체

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (key != null && !key.trim().isEmpty()) {
            if (isKoreanConsonant(key) == 0) {
                booleanBuilder.and(qUser.userEmail.contains(key).or(qUser.userNickname.contains(key)));
            }
            return booleanBuilder;
        }
        return booleanBuilder;
    }

    public List<User> followingUserList(BooleanBuilder booleanBuilder, int code) {
        QUser qUser = QUser.user;  // QueryDSL로 생성된 QUser 객체
        QFollow qFollow = QFollow.follow;
        List<User> users;
        if (booleanBuilder.hasValue()) {  // booleanBuilder에 조건이 있을 때만 포함
            users = queryFactory
                    .select(qUser)
                    .from(qFollow)
                    .join(qUser).on(qFollow.followerUser.userCode.eq(qUser.userCode))
                    .where(qFollow.followingUser.userCode.eq(code)
                            .and(booleanBuilder))
                    .fetch();
        } else {  // key 조건이 없을 때는 기본 조건으로만 조회
            users = queryFactory
                    .select(qUser)
                    .from(qFollow)
                    .join(qUser).on(qFollow.followerUser.userCode.eq(qUser.userCode))
                    .where(qFollow.followingUser.userCode.eq(code))  // 기본 조건만 적용
                    .fetch();
        }
        return users;
    }

    public List<User> followerUserList(BooleanBuilder booleanBuilder, int code) {
        QUser qUser = QUser.user;  // QueryDSL로 생성된 QUser 객체
        QFollow qFollow = QFollow.follow;
        List<User> users;
        if (booleanBuilder.hasValue()) {  // booleanBuilder에 조건이 있을 때만 포함
            users = queryFactory
                    .select(qUser)
                    .from(qFollow)
                    .join(qUser).on(qFollow.followingUser.userCode.eq(qUser.userCode))
                    .where(qFollow.followerUser.userCode.eq(code)
                            .and(booleanBuilder))
                    .fetch();
        } else {  // key 조건이 없을 때는 기본 조건으로만 조회
            users = queryFactory
                    .select(qUser)
                    .from(qFollow)
                    .join(qUser).on(qFollow.followingUser.userCode.eq(qUser.userCode))
                    .where(qFollow.followerUser.userCode.eq(code))  // 기본 조건만 적용
                    .fetch();
        }
        return users;
    }
    public List<String> nickNameList (List<User> users) {
        return users.stream()
                .map(User::getUserNickname)
                .toList();
    }
    private static final String[] INITIALS = {
            "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ",
            "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    };
    private static final String[] FINAL_CONSONANTS = {
            "",   "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ",
            "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    };

    private static final Map<String, String> COMPLEX_CONSONANTS = Map.ofEntries(
            Map.entry(FINAL_CONSONANTS[3], "ㄱㅅ"),
            Map.entry(FINAL_CONSONANTS[5], "ㄴㅈ"),
            Map.entry(FINAL_CONSONANTS[6], "ㄴㅎ"),
            Map.entry(FINAL_CONSONANTS[9], "ㄹㄱ"),
            Map.entry(FINAL_CONSONANTS[10], "ㄹㅁ"),
            Map.entry(FINAL_CONSONANTS[11], "ㄹㅂ"),
            Map.entry(FINAL_CONSONANTS[12], "ㄹㅅ"),
            Map.entry(FINAL_CONSONANTS[13], "ㄹㅌ"),
            Map.entry(FINAL_CONSONANTS[14], "ㄹㅍ"),
            Map.entry(FINAL_CONSONANTS[15], "ㄹㅎ"),
            Map.entry(FINAL_CONSONANTS[18], "ㅂㅅ")
    );


    public List<String> convertToInitialsFromName(List<User> users, int num, String key) {
        List<String> userNickNameList = new ArrayList<>();
        for(User user : users) {
            StringBuilder initials = new StringBuilder();
            int keyIndex = 0;
            for (char ch : user.getUserNickname().toCharArray()) {
                int unicode = ch - 0xAC00;
                int initialIndex = unicode / (21 * 28);
                int medialIndex = (unicode % (21 * 28)) / 28; // 중성 인덱스 추출
                int finalIndex = unicode % 28;


                char combinedChar = (char) (0xAC00 + (initialIndex * 21 * 28) + (medialIndex * 28));
                if (ch >= 0xAC00 && ch <= 0xD7A3 && num == 1) { // 한글이냐 여부
                    initials.append(INITIALS[initialIndex]);
                } else if(ch >= 0xAC00 && ch <= 0xD7A3 && num == 2) {
                    initials.append(combinedChar);
                } else if(ch >= 0xAC00 && ch <= 0xD7A3 && num == 3) {
                    String initialChar = INITIALS[initialIndex];
                    if (key != null && !key.trim().isEmpty()) {
                        if (keyIndex < key.length()) {
                            char keyCh = key.charAt(keyIndex);
                            int keyUnicode = keyCh - 0xAC00;
                            int keyInitialIndex = keyUnicode / (21*28);
                            int keyMedialIndex = (keyUnicode % (21*28)) /28;
                            int keyFinalIndex = keyUnicode % 28;

                            // 키가 온전한 한글 음절인 경우
                            if (keyCh >= 0xAC00 && keyCh <= 0xD7A3 && keyCh == ch) {
                                initials.append(ch);
                                keyIndex++;
                            }
                            // 키가 초성인 경우, 유저 닉네임의 초성과 비교
                            else if ((keyCh >= 0x3131 && keyCh <= 0x314E) && initialChar.charAt(0) == keyCh) {
                                initials.append(initialChar);
                                keyIndex++;
                            }
                            // 키와 매칭되지 않는 경우, 유저의 초성만 추가 // 초중성이 같다면 초중성만 남기기
                            else {
                                if(keyCh == (char) (0xAC00 + (initialIndex * 21 * 28) + (medialIndex * 28))) {
                                    if(keyFinalIndex == 0 || finalIndex == 0) initials.append(combinedChar);
                                    if(FINAL_CONSONANTS[finalIndex].equals(FINAL_CONSONANTS[keyFinalIndex])) initials.append(ch);
                                    else if(sliceKorean(String.valueOf(keyCh)).charAt(0) == ch) initials.append(ch);
                                    else initials.append(initialChar);
                                }
                                else initials.append(initialChar);
                            }
                        } else {
                            // 키의 길이를 넘어섰을 때, 남은 닉네임의 글자는 초성만 추가
                            initials.append(initialChar);
                        }
                    } else {
                        initials.append(ch);  // 한글이 아닌 경우 그대로 추가
                    }
                }
            }
            userNickNameList.add(initials.toString());
        }
        System.out.println(userNickNameList);
        return userNickNameList; // 한글 닉네임의 초성 문자열이 나옴 홍길동-> ㅎㄱㄷ || 홍ㄱㄷ
    }

    private int isKoreanConsonant(String key) {
        if (key == null || key.trim().isEmpty()) {
            return 0;
        }
        for(int i=0; i<key.length(); i++) {
            int unicode = key.charAt(i) - 0xAC00;
            int finalIndex = unicode % 28;
            if (key.length() > 1) {
                if(key.charAt(i)< 0x3131) return 0;

                if(key.charAt(key.length()-1) >= 0x3131 && key.charAt(key.length()-1) <= 0x314E) return 3;
                else if(finalIndex == 0) return 3;
                else return 5;
            }
        }
        if(key.length() == 1 && (key.charAt(0) >= 0x3131 && key.charAt(0) <= 0x314E)) {
            return 1;
        }
        if (key.length() == 1 && key.charAt(0) >= 0xAC00 && key.charAt(0) <= 0xD7A3) {
            int unicode = key.charAt(0) - 0xAC00;
            int finalIndex = unicode % 28;
                if (finalIndex == 0) {
                    return 2;
                } else return 4;
        }
        return 0;
    }

    public String sliceKorean(String key) {
        if(!key.matches(".*\\s.*")) {
            if (isKoreanConsonant(key) == 4) {
                int unicode = key.charAt(0) - 0xAC00;
                int initialIndex = unicode / (21 * 28); // 초성 인덱스 추출
                int medialIndex = (unicode % (21 * 28)) / 28; // 중성 인덱스 추출
                int finalIndex = unicode % 28;

                StringBuilder sliceTextBuilder = new StringBuilder();
                String firstChar = String.valueOf((char) (0xAC00 + (initialIndex * 21 * 28) + (medialIndex * 28)));
                String secondChar = FINAL_CONSONANTS[finalIndex];
                if (COMPLEX_CONSONANTS.containsKey(secondChar)) {
                    secondChar = COMPLEX_CONSONANTS.get(secondChar);
                    // secondChar 을 잘라서 배열의 무언가와 비교를 하고 그 배열에 맞는 인덱스를 저기 더해줘야함
                    String secondCharBefore = secondChar.substring(0, 1);
                    int secondCharIndex = 0;
                    for (int i = 0; i < FINAL_CONSONANTS.length; i++) {
                        if (secondCharBefore.equals(FINAL_CONSONANTS[i])) {
                            secondCharIndex = i;
                            break;
                        }
                    }
                    firstChar = String.valueOf((char) (firstChar.charAt(0) + secondCharIndex));
                    secondChar = secondChar.substring(1, 2);
                }
                sliceTextBuilder.append(firstChar);
                sliceTextBuilder.append(secondChar);

                String sliceText = sliceTextBuilder.toString();
                return sliceText;
            } else if (isKoreanConsonant(key) == 5) {
                StringBuilder sliceTextBuilder = new StringBuilder();
                for (char ch : key.toCharArray()) {
                    if(!(ch < 0xAC00)) {
                        int unicode = ch - 0xAC00;
                        int initialIndex = unicode / (21 * 28); // 초성 인덱스 추출
                        int medialIndex = (unicode % (21 * 28)) / 28; // 중성 인덱스 추출
                        int finalIndex = unicode % 28;

                        if (finalIndex == 0) {
                            sliceTextBuilder.append(ch);
                        } else {
                            String firstChar = String.valueOf((char) (0xAC00 + (initialIndex * 21 * 28) + (medialIndex * 28)));
                            String secondChar = FINAL_CONSONANTS[finalIndex];
                            sliceTextBuilder.append(firstChar);
                            sliceTextBuilder.append(secondChar);
                        }
                    }
                }
                String sliceText = sliceTextBuilder.toString();
                return sliceText;
            }
        }
        return key;
    }
    private List<User> filteredUsers(String key, int code) {
        QUser qUser = QUser.user;  // QueryDSL로 생성된 QUser 객체
        // 먼저 유저 리스트를 가져옴 (필터링에 사용)
        List<User> allUsers = queryFactory
                .selectFrom(qUser)
                .where(qUser.userHeight.isNotNull())
                .fetch(); // 모든 유저 목록을 가져옴
        BooleanBuilder followFilter = followBuilder(key, allUsers);

        return followingUserList(followFilter, code);
    }
    private List<User> filteredUsers2 (String key, int code) {
        QUser qUser = QUser.user;  // QueryDSL로 생성된 QUser 객체
        // 먼저 유저 리스트를 가져옴 (필터링에 사용)
        List<User> allUsers = queryFactory
                .selectFrom(qUser)
                .where(qUser.userHeight.isNotNull())
                .fetch(); // 모든 유저 목록을 가져옴
        BooleanBuilder followFilter = followBuilder(key, allUsers);
        return followerUserList(followFilter,code);
    }
    private char extractInitial(char ch) {
        int unicodeValue = ch - 0xAC00;
        int initialIndex = unicodeValue / (21 * 28);
        return (char) (0x1100 + initialIndex); // 초성
    }

    private static char extractFinal(char ch) {
        int unicodeValue = ch - 0xAC00;
        int finalIndex = unicodeValue % 28;
        return finalIndex == 0 ? '\0' : (char) (0x11A7 + finalIndex); // 종성 (없으면 '\0' 반환)
    }
    private String createSearchResources(String key, List<User> users) {
        List<String> nickNameList = nickNameList(users);
        Map<String, List<Character>> nickNameCharMap = new LinkedHashMap<>();

        Set<String> nickNameSet = new HashSet<>(nickNameList);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(key);

        if(key != null && key.length() >= 2) {
            stringBuilder.setLength(stringBuilder.length()-1);
            for(User user : users) {
                List<Character> list = new ArrayList<>();
                if (user.getUserNickname().length() > key.length()) {
                    list.add(user.getUserNickname().charAt(key.length()-2));
                    list.add(user.getUserNickname().charAt(key.length()-1));
                    list.add(user.getUserNickname().charAt(key.length()));
                    nickNameCharMap.put(user.getUserNickname(), list);
                } else if(user.getUserNickname().length() == key.length()){
                    list.add(user.getUserNickname().charAt(key.length()-3));
                    list.add(user.getUserNickname().charAt(key.length()-2));
                    list.add(user.getUserNickname().charAt(key.length()-1));
                    nickNameCharMap.put(user.getUserNickname(), list);
                } else {
                    nickNameCharMap.put(user.getUserNickname(), list);
                    break;
                }
            }
            String lastLetter = String.valueOf(key.charAt(key.length() - 1));
            int unicode =  lastLetter.charAt(0) - 0xAC00;
            int finalIndex = unicode % 28;

            String target = stringBuilder.toString() + lastLetter;
            String target2 = sliceKorean(lastLetter);
            String target3 = stringBuilder.toString() + sliceKorean(lastLetter).charAt(0);
            if(lastLetter.charAt(0) < 0xAC00 ) stringBuilder.append(lastLetter);
            else if(finalIndex == 0) stringBuilder.append(lastLetter);
            else if(nickNameSet.stream().anyMatch(s -> s.contains(target))) stringBuilder.append(key.charAt(key.length()-1));
            else if(
                    nickNameCharMap.values().stream()
                            .anyMatch(charArray -> {
                                for (char c : charArray) {
                                    if (c == sliceKorean(lastLetter).charAt(0)) {
                                        return true;
                                    }
                                }
                                return false;
                            })
            ) stringBuilder.append(target2);
            else if (nickNameCharMap.values().stream()
                    .anyMatch(charArray -> {
                        if (charArray.isEmpty()) return false; // 빈 배열 처리
                        // 리스트의 마지막 글자 가져오기
                        char lastChar = charArray.getLast();
                        // 입력 문자의 종성과 리스트 마지막 글자의 초성을 비교
                        return extractFinal(lastLetter.charAt(0)) == extractInitial(lastChar);
                    })) stringBuilder.append(lastLetter);
            else if(nickNameSet.stream().anyMatch(s -> s.contains(target3))) stringBuilder.append(target2);
        }
        return stringBuilder.toString();
    }
    //내가 팔로우한 인간들의 수와 인간들 전체 목록 dto발사
    public FollowDTO viewMyFollower(int followingUserCode, String key) {
        if(key != null) {
            key=key.trim();
        }
        List<User> filteredUsers = filteredUsers(key, followingUserCode);
        List<String> nickNameList = nickNameList(filteredUsers);
        List<User> initialSearchUser = new ArrayList<>();

        String keyword = createSearchResources(key, filteredUsers);
        System.out.println(keyword);
        switch (isKoreanConsonant(keyword)) {
            case 1: {
                List<String> userNickNameList = convertToInitialsFromName(filteredUsers, 1, null);
                for (int i = 0; i < userNickNameList.size(); i++) {
                    if (userNickNameList.get(i).contains(keyword)) {
                        String matchingName = nickNameList.get(i);
                        filteredUsers.stream()
                                .filter(user -> user.getUserNickname().contains(matchingName)) // 닉네임 일치 여부 확인
                                .findFirst()
                                .ifPresent(initialSearchUser::add);
                    }
                }
                List<UserDTO> initialUserDTOList = initialSearchUser.stream()
                        .map(user -> {
                            boolean logic = checkLogic(followingUserCode, user.getUserCode());
                            return new UserDTO(user, logic);
                        })
                        .toList();
                return new FollowDTO(initialUserDTOList.size(), initialUserDTOList);
            }
            case 2: {
                List<String> userNickNameList = convertToInitialsFromName(filteredUsers, 2, null);
                for (int i = 0; i < userNickNameList.size(); i++) {
                    if (userNickNameList.get(i).contains(keyword)) {
                        String matchingName = nickNameList.get(i);
                        filteredUsers.stream()
                                .filter(user -> user.getUserNickname().contains(matchingName)) // 닉네임 일치 여부 확인
                                .findFirst()
                                .ifPresent(initialSearchUser::add);
                    }
                }
                List<UserDTO> initialUserDTOList = initialSearchUser.stream()
                        .map(user -> {
                            boolean logic = checkLogic(followingUserCode, user.getUserCode());
                            return new UserDTO(user, logic);
                        })
                        .toList();
                return new FollowDTO(initialUserDTOList.size(), initialUserDTOList);
            }
            case 3: {
                List<String> userNickNameList = convertToInitialsFromName(filteredUsers, 3, keyword);
                for (int i = 0; i < userNickNameList.size(); i++) {
                    if (userNickNameList.get(i).contains(keyword)) {
                        String matchingName = nickNameList.get(i);
                        filteredUsers.stream()
                                .filter(user -> user.getUserNickname().contains(matchingName)) // 닉네임 일치 여부 확인
                                .findFirst()
                                .ifPresent(initialSearchUser::add);
                    }
                }
                List<UserDTO> initialUserDTOList = initialSearchUser.stream()
                        .map(user -> {
                            boolean logic = checkLogic(followingUserCode, user.getUserCode());
                            return new UserDTO(user, logic);
                        })
                        .toList();
                return new FollowDTO(initialUserDTOList.size(), initialUserDTOList);
            }
            case 4: {
                List<String> userNickNameList = convertToInitialsFromName(filteredUsers, 3,sliceKorean(keyword));
                for (int i = 0; i < filteredUsers.size(); i++) {
                    if (filteredUsers.get(i).getUserNickname().contains(keyword)) {
                        String matchingName = nickNameList.get(i);
                        filteredUsers.stream()
                                .filter(user -> user.getUserNickname().contains(matchingName))
                                .findFirst()
                                .ifPresent(initialSearchUser::add);
                    } else if (userNickNameList.get(i).contains(sliceKorean(keyword))) {
                        String matchingName = nickNameList.get(i);
                        filteredUsers.stream()
                                .filter(user -> user.getUserNickname().contains(matchingName)) // 닉네임 일치 여부 확인
                                .findFirst()
                                .ifPresent(initialSearchUser::add);
                    }
                }
                List<UserDTO> initialUserDTOList = initialSearchUser.stream()
                        .map(user -> {
                            boolean logic = checkLogic(followingUserCode, user.getUserCode());
                            return new UserDTO(user, logic);
                        })
                        .toList();
                return new FollowDTO(initialUserDTOList.size(), initialUserDTOList);
            }
            case 5 : {
                List<String> userNickNameList = convertToInitialsFromName(filteredUsers, 3, keyword);

                for (int i = 0; i < filteredUsers.size(); i++) {
                    if (filteredUsers.get(i).getUserNickname().contains(keyword)) {
                        String matchingName = nickNameList.get(i);
                        filteredUsers.stream()
                                .filter(user -> user.getUserNickname().contains(matchingName))
                                .findFirst()
                                .ifPresent(initialSearchUser::add);
                    } else if (userNickNameList.get(i).contains(keyword)) {
                        String matchingName = nickNameList.get(i);
                        filteredUsers.stream()
                                .filter(user -> user.getUserNickname().contains(matchingName)) // 닉네임 일치 여부 확인
                                .findFirst()
                                .ifPresent(initialSearchUser::add);
                    }
                }
                List<UserDTO> initialUserDTOList = initialSearchUser.stream()
                        .map(user -> {
                            boolean logic = checkLogic(followingUserCode, user.getUserCode());
                            return new UserDTO(user, logic);
                        })
                        .toList();
                return new FollowDTO(initialUserDTOList.size(), initialUserDTOList);
            }
            default: {
//                 User 리스트를 UserDTO 리스트로 변환
                List<UserDTO> userDTOList = filteredUsers.stream()
                        .map(user -> {
                            boolean logic = checkLogic(followingUserCode, user.getUserCode());
                            return new UserDTO(user, logic);
                        })
                        .collect(Collectors.toList());

                // FollowDTO로 변환하여 반환
                return new FollowDTO(userDTOList.size(), userDTOList);
            }
        }
    }
    //위랑 반대
    public FollowDTO followMeUsers (int followerUserCode, String key) {
        if(key != null) {
            key=key.trim();
        }
        List<User> filteredUsers = filteredUsers2(key, followerUserCode);
        List<String> nickNameList = nickNameList(filteredUsers);
        List<User> initialSearchUser = new ArrayList<>();

        String keyword = createSearchResources(key, filteredUsers);
        switch (isKoreanConsonant(keyword)) {
            case 1: {
                List<String> userNickNameList = convertToInitialsFromName(filteredUsers, 1, null);
                for (int i = 0; i < userNickNameList.size(); i++) {
                    if (userNickNameList.get(i).contains(keyword)) {
                        String matchingName = nickNameList.get(i);
                        filteredUsers.stream()
                                .filter(user -> user.getUserNickname().contains(matchingName)) // 닉네임 일치 여부 확인
                                .findFirst()
                                .ifPresent(initialSearchUser::add);
                    }
                }
                List<UserDTO> initialUserDTOList = initialSearchUser.stream()
                        .map(user -> {
                            boolean logic = checkLogic(followerUserCode, user.getUserCode());
                            return new UserDTO(user, logic);
                        })
                        .toList();
                return new FollowDTO(initialUserDTOList.size(), initialUserDTOList);
            }
            case 2: {
                List<String> userNickNameList = convertToInitialsFromName(filteredUsers, 2, null);
                for (int i = 0; i < userNickNameList.size(); i++) {
                    if (userNickNameList.get(i).contains(keyword)) {
                        String matchingName = nickNameList.get(i);
                        filteredUsers.stream()
                                .filter(user -> user.getUserNickname().contains(matchingName)) // 닉네임 일치 여부 확인
                                .findFirst()
                                .ifPresent(initialSearchUser::add);
                    }
                }
                List<UserDTO> initialUserDTOList = initialSearchUser.stream()
                        .map(user -> {
                            boolean logic = checkLogic(followerUserCode, user.getUserCode());
                            return new UserDTO(user, logic);
                        })
                        .toList();
                return new FollowDTO(initialUserDTOList.size(), initialUserDTOList);
            }
            case 3: {
                List<String> userNickNameList = convertToInitialsFromName(filteredUsers, 3, keyword);
                for (int i = 0; i < userNickNameList.size(); i++) {
                    if (userNickNameList.get(i).contains(keyword)) {
                        String matchingName = nickNameList.get(i);
                        filteredUsers.stream()
                                .filter(user -> user.getUserNickname().contains(matchingName)) // 닉네임 일치 여부 확인
                                .findFirst()
                                .ifPresent(initialSearchUser::add);
                    }
                }
                List<UserDTO> initialUserDTOList = initialSearchUser.stream()
                        .map(user -> {
                            boolean logic = checkLogic(followerUserCode, user.getUserCode());
                            return new UserDTO(user, logic);
                        })
                        .toList();
                return new FollowDTO(initialUserDTOList.size(), initialUserDTOList);
            }
            case 4: {
                List<String> userNickNameList = convertToInitialsFromName(filteredUsers, 3, sliceKorean(keyword));
                for (int i = 0; i < filteredUsers.size(); i++) {
                    if (filteredUsers.get(i).getUserNickname().contains(keyword)) {
                        String matchingName = nickNameList.get(i);
                        filteredUsers.stream()
                                .filter(user -> user.getUserNickname().contains(matchingName))
                                .findFirst()
                                .ifPresent(initialSearchUser::add);
                    } else if (userNickNameList.get(i).contains(sliceKorean(keyword))) {
                        String matchingName = nickNameList.get(i);
                        filteredUsers.stream()
                                .filter(user -> user.getUserNickname().contains(matchingName)) // 닉네임 일치 여부 확인
                                .findFirst()
                                .ifPresent(initialSearchUser::add);
                    }
                }
                List<UserDTO> initialUserDTOList = initialSearchUser.stream()
                        .map(user -> {
                            boolean logic = checkLogic(followerUserCode, user.getUserCode());
                            return new UserDTO(user, logic);
                        })
                        .toList();
                return new FollowDTO(initialUserDTOList.size(), initialUserDTOList);
            }
            case 5 : {
                List<String> userNickNameList = convertToInitialsFromName(filteredUsers, 3, keyword);
                for (int i = 0; i < filteredUsers.size(); i++) {
                    if (filteredUsers.get(i).getUserNickname().contains(keyword)) {
                        String matchingName = nickNameList.get(i);
                        filteredUsers.stream()
                                .filter(user -> user.getUserNickname().contains(matchingName))
                                .findFirst()
                                .ifPresent(initialSearchUser::add);
                    } else if (userNickNameList.get(i).contains(keyword)) {
                        String matchingName = nickNameList.get(i);
                        filteredUsers.stream()
                                .filter(user -> user.getUserNickname().contains(matchingName)) // 닉네임 일치 여부 확인
                                .findFirst()
                                .ifPresent(initialSearchUser::add);
                    }
                }
                List<UserDTO> initialUserDTOList = initialSearchUser.stream()
                        .map(user -> {
                            boolean logic = checkLogic(followerUserCode, user.getUserCode());
                            return new UserDTO(user, logic);
                        })
                        .toList();
                return new FollowDTO(initialUserDTOList.size(), initialUserDTOList);
            }
            default: {
//                 User 리스트를 UserDTO 리스트로 변환
                List<UserDTO> userDTOList = filteredUsers.stream()
                        .map(user -> {
                            boolean logic = checkLogic(followerUserCode, user.getUserCode());
                            return new UserDTO(user, logic);
                        })
                        .collect(Collectors.toList());

                // FollowDTO로 변환하여 반환
                return new FollowDTO(userDTOList.size(), userDTOList);
            }
        }
    }

    // 내가 팔로우하는 유저의 게시글 조회
    public Page<PostInfoDTO> getPostsFromFollowingUsers(int userCode, Pageable pageable) {
        List<Follow> followers = followDAO.findAllByFollowingUser_UserCode(userCode);
        List<Integer> followerUserCodes = followers.stream()
                .map(follow -> follow.getFollowerUser().getUserCode())
                .collect(Collectors.toList());

        Page<Post> posts = postDAO.findByUser_UserCodeIn(followerUserCodes, pageable);

        return posts.map(post -> {
            List<PostImg> postImgs = postImgDAO.findByPost_PostCode(post.getPostCode());
            return new PostInfoDTO(post, 0, 0, postImgs);
        });
    }
    // 추천 팔로워를 해봅시다...
//    public
}
