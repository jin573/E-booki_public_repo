package com.be.ebooki.service;

import com.be.ebooki.config.KakaoProperties;
import com.be.ebooki.config.jwt.JwtProperties;
import com.be.ebooki.config.jwt.JwtTokenProvider;
import com.be.ebooki.domain.User;
import com.be.ebooki.dto.KakaoResponse;
import com.be.ebooki.dto.UserRequest;
import com.be.ebooki.dto.UserResponse;
import com.be.ebooki.repository.UserRepository;
import com.be.ebooki.enums.Nickname;
import com.be.ebooki.enums.UserType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final JwtTokenProvider jwtTokenProvider;

    private final KakaoProperties kakaoProperties;
    private final S3Service s3Service;


    //sign up
    public UserResponse.UserInfoDTO signupUser(UserRequest.UserSignupDTO signupDTO) {
        //이메일 중복 검사하기
        if (userRepository.existsByEmail(signupDTO.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 계정입니다.");
        }

        //닉네임 생성
        String randomNickname = generateNickname();
        //비밀번호 암호화
        String encodePassword = passwordEncoder.encode(signupDTO.getPassword());
        //프로필 이미지
        String profileImageKey = null;

        if(signupDTO.getProfileImage() != null && !signupDTO.getProfileImage().isEmpty()){
            profileImageKey = signupDTO.getProfileImage();
            if(!profileImageKey.startsWith("images/profile/")){
                throw new IllegalArgumentException("잘못된 프로필 이미지 경로입니다.");
            }
        }else{
            profileImageKey = getRandomProfileImage(); //랜덤 url
        }

        //Entity로 변환 후 저장
        User user = signupDTO.toEntity();
        user.updateNickname(randomNickname);
        user.updatePassword(encodePassword);//비밀번호를 암호화하여 업데이트
        user.updateUserType(UserType.LOCAL);//유저 타입 지정
        user.updateProfileImage(profileImageKey);
        User registeredUser = userRepository.save(user);

        return UserResponse.UserInfoDTO.from(registeredUser,
                s3Service.getFileUrl(registeredUser.getProfileImage())
        );
    }

    public UserResponse.UserLoginDTO loginUser(UserRequest.UserLoginDTO userLoginDTO) {
        //내부에서 아이디로 정보 찾기 -> userInfo에 담기 ->토큰 생성해서 loginDTO에 담기
        //이메일 존재 유무 검사
        if (!(userRepository.existsByEmail(userLoginDTO.getEmail()))) {
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");
        }
        //비밀번호 틀린 로직
        User user = userRepository.findByEmail(userLoginDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        //이메일과 비밀번호를 통해 유저 정보 가져오기
        UserResponse.UserInfoDTO userInfoDTO = UserResponse.UserInfoDTO.from(user, s3Service.getFileUrl(user.getProfileImage()));

        //토큰 생성
        String accessToken = jwtTokenProvider.generateToken(user.getEmail(), user.getId(), jwtProperties.getAccessTokenExpiration());
        String refreshToken = jwtTokenProvider.generateToken(user.getEmail(), user.getId(), jwtProperties.getRefreshTokenExpiration());

        //UserLoginDTO에 담기
        return UserResponse.UserLoginDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userInfoDTO(userInfoDTO)
                .build();
    }

    @Transactional
    public void updateProfileImage(Integer userId, String newImageKey){
        if(!newImageKey.startsWith("images/profile/")){
            throw new IllegalArgumentException("잘못된 이미지 경로입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        //프로필 변경 시 이전 이미지 삭제 (단, default image 제외)
        String oldImageKey = user.getProfileImage();

        if(oldImageKey != null
                && !oldImageKey.equals(newImageKey)
                && !oldImageKey.startsWith("images/default")) {
            s3Service.deleteFile(oldImageKey);
        }

        user.updateProfileImage(newImageKey);
    }
    public void logoutUser(String email) {

    }

    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        userRepository.delete(user);

    }


    //get all users
    public List<UserResponse.UserInfoDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponse.UserInfoDTO.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .profileImage(user.getProfileImage())
                        .build())
                .collect(Collectors.toList());
    }

    //reissue
    public UserResponse.TokenReissueDTO reissueToken(UserRequest.TokenReissueDTO tokenReissueDTO) {
        String refreshToken = tokenReissueDTO.getRefreshToken();

        //refresh Token 유효성 검증
        boolean isValid = jwtTokenProvider.validateToken(refreshToken);
        if (!isValid) {
            throw new RuntimeException("유효하지 않거나 만료된 Refresh Token 입니다. 다시 로그인해주세요.");
        }

        //유효하면, 내부에서 사용자 정보 꺼내기
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        //새로운 access Token 발급
        String newAccessToken = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getId(),
                jwtProperties.getAccessTokenExpiration()
        );

        //refresh 유지 (만료되면 재로그인)
        return UserResponse.TokenReissueDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public UserResponse.UserLoginDTO kakaoLogin(String accessCode, HttpServletResponse httpServletResponse) {
        //토큰 요청
        KakaoResponse.OAuthToken oAuthToken = kakaoProperties.requestToken(accessCode);
        //관련 정보 가져오기
        KakaoResponse.KakaoProfile kakaoProfile = kakaoProperties.requestProfile(oAuthToken);
        //이메일 가져오기
        String email = kakaoProfile.getKakaoAccount().getEmail();

        //이메일 존재하는지 확인 -> 없으면 계정 생성 후 로그인까지
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(kakaoProfile));

        //토큰 및 닉네임 설정
        String accessToken = jwtTokenProvider.generateToken(user.getEmail(), user.getId(), jwtProperties.getAccessTokenExpiration());
        String refreshToken = jwtTokenProvider.generateToken(user.getEmail(), user.getId(), jwtProperties.getRefreshTokenExpiration());
        if (user.getNickname() == null || user.getNickname().isEmpty()) {
            user.updateNickname(generateNickname());
        }

        //userInfoDTO 생성
        UserResponse.UserInfoDTO userInfoDTO = UserResponse.UserInfoDTO.from(user, s3Service.getFileUrl(user.getProfileImage()));

        return UserResponse.UserLoginDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userInfoDTO(userInfoDTO)
                .build();
    }

    private User createNewUser(KakaoResponse.KakaoProfile kakaoProfile) {
        User newUser = new User(
                kakaoProfile.getKakaoAccount().getEmail(),
                passwordEncoder.encode(UUID.randomUUID().toString()),
                generateNickname(),
                getRandomProfileImage(),//랜덤 이미지로 설정
                UserType.KAKAO//추후 default url로 변경
        );
        return userRepository.save(newUser);
    }

    private String getRandomProfileImage() {
        List<String> images = List.of(
                "images/default/profile1.png",
                "images/default/profile2.png",
                "images/default/profile3.png",
                "images/default/profile4.png",
                "images/default/profile5.png"
        );

        return images.get(new Random().nextInt(images.size()));
    }


    private String generateNickname() {
        return Nickname.Adjective.random() + " " + Nickname.Noun.random();
    }

    public static Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 객체가 없는 경우
        if (authentication == null) {
            throw new IllegalStateException("인증 정보가 존재하지 않습니다.");
        }

        Object principal = authentication.getPrincipal();

        //로그인 안 한 경우 or principal 타입이 Integer 아닐 경우
        if (!(principal instanceof Integer)) {
            throw new IllegalStateException("유효한 사용자 정보가 아닙니다.");
        }

        return (Integer) principal;
    }

}
