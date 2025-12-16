package com.neighbus.club;

import com.neighbus.account.AccountDTO; // Assuming AccountDTO is needed if using @AuthenticationPrincipal
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/mobile/club")
public class ClubMobileRestController {

    private static final Logger logger = LoggerFactory.getLogger(ClubMobileRestController.class);

    @Autowired
    private ClubService clubService;

    // From ClubRestController.java
    @GetMapping("/checkName")
    public ResponseEntity<Map<String, Boolean>> checkClubName(@RequestParam("clubName") String clubName) {
        boolean isDuplicate = clubService.isClubNameDuplicate(clubName);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return ResponseEntity.ok(response);
    }

    // From ClubController.java
    @PostMapping("/members/remove")
    public Map<String, Object> removeMember(@RequestParam("clubId") int clubId, @RequestParam("userId") int userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = clubService.removeClubMember(clubId, userId);
            if (success) {
                response.put("status", "success");
                response.put("message", "회원이 성공적으로 탈퇴 처리되었습니다.");
            } else {
                response.put("status", "fail");
                response.put("message", "회원 탈퇴 처리에 실패했습니다. 해당 회원을 찾을 수 없거나 이미 탈퇴했습니다.");
            }
        } catch (Exception e) {
            logger.error("Error removing club member: clubId={}, userId={}", clubId, userId, e);
            response.put("status", "error");
            response.put("message", "서버 오류로 회원 탈퇴 처리에 실패했습니다.");
        }
        return response;
    }
}
