package com.neighbus.chat;


import java.util.List;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.neighbus.account.AccountMapper; // Added
import com.neighbus.alarm.NotificationService;
import com.neighbus.recruitment.RecruitmentService;

@RequestMapping("/api/mobile/chat")
@Controller
public class ChatMobileRestController {

    private final SimpMessageSendingOperations template;
    private final ChatMapper chatMapper;
    private final NotificationService notificationService;
    private final RecruitmentService recruitmentService;
    private final AccountMapper accountMapper; // Added

    public ChatMobileRestController(SimpMessageSendingOperations template, ChatMapper chatMapper,
                          NotificationService notificationService, RecruitmentService recruitmentService,
                          AccountMapper accountMapper) { // Added
        this.template = template;
        this.chatMapper = chatMapper;
        this.notificationService = notificationService;
        this.recruitmentService = recruitmentService;
        this.accountMapper = accountMapper; // Added
    }

    @MessageMapping("/message")
    public void message(ChatMessageDTO message) {

        // 1. 공통 로직: 메시지 타입에 따라 내용 변경 및 DB 저장, STOMP로 브로드캐스트
        if ("ENTER".equals(message.getMessageType())) {
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
        }
        chatMapper.insertMessage(message);
        template.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);

        // 2. 알림 로직 분기
        String recipientUsername = message.getRecipientUsername();

        if (recipientUsername != null && !recipientUsername.isEmpty()) {
            // Case 1: 친구 1:1 채팅 알림
            Integer recipientId = accountMapper.findIdByUsername(recipientUsername);
            
            // 수신자 ID가 존재할 경우에만 알림 전송
            if (recipientId != null) {
                String notificationContent = message.getSender() + "님으로부터 새로운 메시지가 도착했습니다.";
                String notificationUrl = "/friend/list"; // 클릭 시 친구 목록 페이지로 이동
                notificationService.send(recipientId, "FRIEND_CHAT", notificationContent, notificationUrl);
            }
        } else {
            // Case 2: 기존 모집글 그룹 채팅 알림
            try {
                int recruitmentId = Integer.parseInt(message.getRoomId());
                int senderId = accountMapper.findIdByUsername(message.getSender());
                List<Integer> memberIds = recruitmentService.getMemberIdsByRecruitmentId(recruitmentId);

                if (memberIds != null && !memberIds.isEmpty()) {
                    String notificationContent = "모임 채팅에 새로운 메시지: " + message.getMessage();
                    String notificationUrl = "/recruitment/" + recruitmentId; // 해당 모집글 상세 페이지로 이동

                    for (Integer memberId : memberIds) {
                        if (memberId != senderId) {
                            notificationService.send(memberId, "CHAT", notificationContent, notificationUrl);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                // roomId가 숫자로 변환되지 않는 경우 (예: 예전 친구 채팅방)는 무시
                System.out.println("Info: Not a recruitment chat room ID, skipping notification. RoomId: " + message.getRoomId());
            }
        }
    }
}