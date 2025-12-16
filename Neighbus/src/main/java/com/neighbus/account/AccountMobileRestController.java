package com.neighbus.account;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/mobile/account")
@RestController
public class AccountMobileRestController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private final AuthenticationManager authenticationManager;

    public AccountMobileRestController(AccountService accountService,
            AuthenticationManager authenticationManager,
            AccountMapper accountMapper) {
			this.accountService = accountService;
			this.accountMapper = accountMapper;
			this.authenticationManager = authenticationManager;
		}
				
	//마이페이지에서 내 정보 수정
	@PostMapping("/updateSocialInfo")
	public String updateSocialInfo(@AuthenticationPrincipal AccountDTO accountDTO,
	                               @RequestParam("phone") String phone,
	                               @RequestParam("birth") String birth,
	                               @RequestParam("sex") String sex) {

	    // 1. DTO 정보 수정
	    accountDTO.setPhone(phone);
	    accountDTO.setBirth(birth);
	    accountDTO.setSex(sex);

	    // 2. DB 정보 수정 (영구 저장)
	    accountMapper.updateSocialInfo(accountDTO);

	    // ★ 3. DB에서 최신 정보 다시 조회 (핵심 해결책) ★
	    // 업데이트된 내용 + 기존 이미지(img) 등 모든 정보를 갱신된 상태로 가져옴
	    // (Mapper에 ID나 Email로 조회하는 메서드를 사용하세요)
	    AccountDTO newAccountDTO = accountMapper.getUser(accountDTO.getUsername()); 

	    // 4. 세션 강제 업데이트
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

	    Authentication newAuth = new UsernamePasswordAuthenticationToken(
	            newAccountDTO,         // ★ DB에서 다시 조회한 완전한 객체 사용
	            auth.getCredentials(),
	            auth.getAuthorities()
	    );

	    SecurityContextHolder.getContext().setAuthentication(newAuth);

	    return "OK";
	}
	
	@PostMapping("/phoneVerification")
	@ResponseBody
	public Map<String, Object> phoneVerification(
		@AuthenticationPrincipal AccountDTO accountDTO
	) {
	    AccountFindDTO findDTO = new AccountFindDTO();
	    findDTO.setPhone(accountDTO.getPhone());
	    findDTO.setUsername(accountDTO.getUsername());
	    return accountService.findAccountByPhone(findDTO);
	}
	
	
	@PostMapping("/updateGrade")
	public String updateGrade(
		AccountDTO accountDTO
	) {
		accountService.updateGrade(accountDTO);
		return "OK";
	}
	
	@PostMapping(value="/insertSignup")
	public Map<String, Object> insertSignup(
			@RequestBody AccountDTO accountDTO
			){
		System.out.println("AccountRestController - insertSignup");
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			int status = accountService.insertSignup(accountDTO);
			map.put("status", status);
		}catch(Exception e) {
			System.out.println(e);
			map.put("status", -1);
		}
		return map;
	}
	

    @PostMapping("/findAccount")
    public Map<String, Object> findAccount(
    	@RequestBody Map<String, String> request
    ) {
		System.out.println("AccountRestController - findAccount");
        String email = request.get("email");

        String username = accountService.findUsernameByEmail(email);

        Map<String, Object> result = new HashMap<>();
        if (username == null) {
            result.put("status", 0);
        } else {
            result.put("status", 1);
            result.put("username", username);
        }
        return result;
    }
    
	@PostMapping("/findAccountByEmail")
	public Map<String,Object> findAccountByEmail(
		@ModelAttribute AccountFindDTO accountFindDTO
	) {
		System.out.println("AccountRestController - findAccountByEmail");
		return accountService.findAccountByEmail(accountFindDTO);
	}
	
	@PostMapping("/sendTempPassword")
	public Map<String,Object> sendTempPassword(
		@RequestBody Map<String, String> request
	) {
		System.out.println("AccountRestController - sendTempPassword");
	    Map<String, Object> response = new HashMap<>();
	    try {
	        accountService.sendTempPassword(request.get("email"));
	        response.put("success", true);
	        response.put("message", "임시 비밀번호가 발송되었습니다.");
	    } catch (Exception e) {
	        response.put("success", false);
	        response.put("message", "처리 중 오류가 발생했습니다.");
	    }
	    return response;
	}

	@PostMapping("/findAccountByPhone")
	public Map<String,Object> findAccountByPhone(
		@ModelAttribute AccountFindDTO accountFindDTO
	) {
		System.out.println("AccountRestController - findAccountByPhone");
		return accountService.findAccountByPhone(accountFindDTO);
	}
	
	@PostMapping("/updatePasswordByPhone")
	public Map<String,Object> updatePasswordByPhone(
		@RequestBody Map<String, String> request
	) {
	    System.out.println("AccountRestController - updatePasswordByPhone");
	    String phone = request.get("phone");
	    Map<String, Object> response = new HashMap<>();
	    try {
	        accountService.updatePasswordByPhone(phone);
	        response.put("success", true);
	        response.put("message", "임시 비밀번호가 발송되었습니다.");
	    } catch (Exception e) {
	        response.put("success", false);
	        response.put("message", "처리 중 오류가 발생했습니다.");
	    }
	    return response;
	}
	
	
}
