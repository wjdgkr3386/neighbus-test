package com.neighbus.config;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.neighbus.Util;
// ★ 중요: 본인 프로젝트 경로에 있는 DTO와 Mapper를 임포트하세요
import com.neighbus.account.AccountDTO;
import com.neighbus.account.AccountMapper;
import com.neighbus.account.AccountService;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	
    @Autowired
    private AccountMapper accountMapper; // JPA Repository 대신 Mapper 사용
    @Autowired
    private AccountService accountService; // JPA Repository 대신 Mapper 사용

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 구글 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Util.print(attributes);
        // 2. 정보 추출
        String provider = "google";
        String providerId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String image = (String) attributes.get("picture");
        
        // 중복 방지용 아이디 (예: google_12345...)
        String username = provider + "_" + providerId; 

        // 3. DB 조회 (MyBatis Mapper 사용)
        // 주의: AccountMapper에 findByUsername 메서드가 있어야 합니다. 
        // 없으면 기존 로그인할 때 아이디로 회원 찾는 메서드 이름을 쓰세요. (예: readMember, loginCheck 등)
        AccountDTO accountDTO = accountMapper.getUser(username);

        if (accountDTO == null) {
            System.out.println("구글 신규 회원 -> 회원가입 진행");

            // 4. DTO 생성 및 필수값(NN) 채우기
            accountDTO = new AccountDTO();
            accountDTO.setUsername(username);
            accountDTO.setPassword("1234"); // 임시 비번
            accountDTO.setName(name);
            accountDTO.setNickname(name);
            accountDTO.setEmail(email);
            accountDTO.setProvider(provider);
            accountDTO.setProviderId(providerId);

            // -- 필수값 채우기 (DB 에러 방지용 가짜 데이터) --
            accountDTO.setPhone("01000000000"); //이 부분은 반드시 에러 난다. 테이블에 null 허용하고 나중에 유저에게 입력받자
            accountDTO.setBirth("000000");
            accountDTO.setSex("N");
            accountDTO.setProvince(1);
            accountDTO.setCity(1);
            accountDTO.setAddress("소셜로그인");
            accountDTO.setImage(image==null?"default-profile.png":image);
            accountDTO.setUserUuid(UUID.randomUUID().toString()); // UUID 생성
            accountDTO.setGrade(1);
            accountDTO.setRole("ROLE_USER");

            // 5. DB 저장 (MyBatis Mapper 사용)
            // 주의: Mapper에 insert 메서드 이름을 쓰세요. (예: insertSignup, register 등)
            accountService.insertSignup(accountDTO);
        } else {
            System.out.println("기존 회원 -> 로그인 진행");
        }
        
        
     // CustomOAuth2UserService.java 마지막 부분

        if (accountDTO != null) {
            // 여기서 0이 나오면 -> Mapper/DB 문제
            // 여기서 119가 나오면 -> 시큐리티 세션/Controller 전달 문제
            System.out.println("★디버깅★ Service 리턴 직전 ID 값: " + accountDTO.getId());
        }


        // 6. 구글 정보를 DTO에 담아서 리턴 (핵심)
        accountDTO.setAttributes(attributes);

        return accountDTO;
    }
}