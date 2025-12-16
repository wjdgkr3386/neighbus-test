package com.neighbus.chatbot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import java.util.List;

@RestController
@RequestMapping("/api/mobile/chatbot")
public class ChatbotMobileRestController {
    private final ChatService chatService;
    private final ZeroShotFewShotService shotService;

    @Autowired
    public ChatbotMobileRestController(ChatService chatService, ZeroShotFewShotService shotService) {
        this.chatService = chatService;
        this.shotService = shotService;
    }

    // 메시지 한꺼번에 출력
    @GetMapping(value = "/sync") // Changed from "/api/chat/sync"
    public String syncChat(@RequestParam("message") String message) {
        return chatService.syncChat(message);
    }
    //메시지 스트림 방식으로 한글자씩 출력
    @GetMapping(value = "/stream") // Changed from "/api/chat/stream"
    public Flux<String> streamChat(@RequestParam("message") String message) {
        return chatService.streamChat(message);
    }
    
    @GetMapping("/compare") // Changed from "/compare"
    public List<Result> comparePrompts() {
        String testInput = "5명이서 피자 8조각을 나누어 먹는데, 2명이 추가되면 피자 1인당 몇 조각이 될까?";

        String zeroShotResult = shotService.runZeroShot(testInput);
        String fewShotResult = shotService.runFewShot(testInput);
        List<Result> list = List.of(
            new Result("제로샷의 결과", zeroShotResult), new Result("퓨삿의 결과", fewShotResult)
        );
        return list; 
    }
}