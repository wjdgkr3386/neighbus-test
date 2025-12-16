package com.neighbus.alarm;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.neighbus.account.AccountDTO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;


@RequestMapping("/api/mobile/notifications")
@RestController
public class NotificationMobileRestController {

    private final NotificationService notificationService; 


   public NotificationMobileRestController(NotificationService notificationService) {
		super();
		this.notificationService = notificationService;
	}
    
    @DeleteMapping("/{id}")
    public String deleteNotification(@PathVariable("id") int id) {
        notificationService.deleteNotification(id);
        return "deleted";
    }
    
    // AJAX 요청을 받아서 JSON 리스트 반환
    @GetMapping("") // Changed from "/api/notifications"
    public List<NotificationDTO> getMyNotifications(@AuthenticationPrincipal AccountDTO accountDTO) {
        if (accountDTO == null) return null;
        return notificationService.getMyNotifications(accountDTO.getId());
    }

    // 특정 사용자의 읽지 않은 알림 개수 반환 (AJAX)
    @GetMapping("/count") // Changed from "/api/notifications/count"
    public int countUnreadNotifications(@AuthenticationPrincipal AccountDTO accountDTO) {
        if (accountDTO == null) return 0;
        return notificationService.countUnread(accountDTO.getId());
    }
}
