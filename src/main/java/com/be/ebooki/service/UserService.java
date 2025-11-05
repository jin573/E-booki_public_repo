package com.be.ebooki.service;

import com.be.ebooki.config.jwt.JwtProperties;
import com.be.ebooki.config.jwt.JwtTokenProvider;
import com.be.ebooki.domain.User;
import com.be.ebooki.dto.UserRequest;
import com.be.ebooki.dto.UserResponse;
import com.be.ebooki.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final JwtTokenProvider jwtTokenProvider;

    //sign up
    public UserResponse.UserInfoDTO signupUser(UserRequest.UserSignupDTO signupDTO){
        //이메일 중복 검사하기
        if(userRepository.existsByEmail(signupDTO.getEmail())){
            throw new IllegalArgumentException("이미 존재하는 계정입니다.");
        }

        //비밀번호 암호화
        String encodePassword = passwordEncoder.encode(signupDTO.getPassword());

        //Entity로 변환 후 저장
        User user = signupDTO.toEntity();
        user.updatePassword(encodePassword);//비밀번호를 암호화하여 업데이트
        User registeredUser = userRepository.save(user);

        return UserResponse.UserInfoDTO.from(registeredUser);
    }

    public UserResponse.UserLoginDTO loginUser(UserRequest.UserLoginDTO userLoginDTO) {
        //내부에서 아이디로 정보 찾기 -> userInfo에 담기 ->토큰 생성해서 loginDTO에 담기
        //이메일 존재 유무 검사
        if(!(userRepository.existsByEmail(userLoginDTO.getEmail()))){
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");
        }
        //비밀번호 틀린 로직
        User user = userRepository.findByEmail(userLoginDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        //이메일과 비밀번호를 통해 유저 정보 가져오기
        UserResponse.UserInfoDTO userInfoDTO = UserResponse.UserInfoDTO.from(user);

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

    //get all users
    public List<UserResponse.UserInfoDTO> getAllUsers(){
        return userRepository.findAll().stream()
                .map(user -> UserResponse.UserInfoDTO.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .profileImage(user.getProfileImage())
                        .build())
                .collect(Collectors.toList());
    }

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

}
