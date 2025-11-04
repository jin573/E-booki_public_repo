package com.be.ebooki.service;

import com.be.ebooki.domain.User;
import com.be.ebooki.dto.UserRequest;
import com.be.ebooki.dto.UserResponse;
import com.be.ebooki.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

}
