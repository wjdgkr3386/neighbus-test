package com.neighbus.freeboard;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neighbus.account.AccountDTO;

@RestController
@RequestMapping("/api/mobile/freeboard")
public class FreeboardMobileRestController {

    @Autowired
    private FreeboardService freeboardService;

    // From FreeboardController.java
    @PostMapping("/write") // Path changed from /freeboard/write
    public int write(
        @AuthenticationPrincipal AccountDTO accountDTO, 
    	FreeboardDTO freeboardDTO
    ) {
    	System.out.println("FreeboardMobileRestController - write");
    	int cnt=0;
    	try {
	    	freeboardDTO.setWriter(accountDTO.getId());
	    	freeboardService.postInsert(freeboardDTO);
	    	cnt=1;
    	}catch(Exception e) {
    		System.out.println(e);
    	}
    	return cnt;
    }

    // From FreeboardController.java
    @PostMapping("/comment") // Path changed from /freeboard/comment
    public ResponseEntity<String> registerComment(
        @RequestBody CommentDTO commentDTO,
        @AuthenticationPrincipal AccountDTO accountDTO 
    ) {
        if (accountDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        commentDTO.setWriter(accountDTO.getId()); 
        if (commentDTO.getParent() == null) {
        	commentDTO.setParent(0);
        }
        
        boolean success = freeboardService.registerComment(commentDTO);
        
        if (success) {
            return ResponseEntity.ok("댓글이 등록되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 등록에 실패했습니다.");
        }
    }

    // From FreeboardController.java
    @DeleteMapping("/comment/{id}") // Path changed from /freeboard/comment/{id}
    public ResponseEntity<String> removeComment(
        @PathVariable("id") int id, 
        @AuthenticationPrincipal AccountDTO accountDTO
    ) {
        if (accountDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        boolean success = freeboardService.removeComment(id, accountDTO.getId());
        
        if (success) {
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("댓글 삭제에 실패했거나 권한이 없습니다.");
        }
    }

    // From FreeboardController.java
    @PostMapping("/edit/{id}") // Path changed from /freeboard/edit/{id}
    public int updatePost(
        @PathVariable("id") int id, 
        FreeboardDTO freeboardDTO, 
        @AuthenticationPrincipal AccountDTO accountDTO
    ) {
    	System.out.println("FreeboardMobileRestController - updatePost");
    	int cnt = 0;
    	try {
	        freeboardDTO.setId(id);
	        freeboardService.updatePost(freeboardDTO, accountDTO.getId());
	        cnt = 1;
    	}catch(Exception e) {
    		System.out.println(e);
    	}
        return cnt;
    }

    // From FreeboardRestController.java
    @DeleteMapping("/deleteReaction") // Path changed from /freeboard/api/deleteReaction
    public Map<String, Object> deleteReaction(
		@RequestBody Map<String, Object> request,
		@AuthenticationPrincipal AccountDTO user
	) {
		System.out.println("FreeboardMobileRestController - deleteReaction");
		request.put("userId", user.getId());
		return freeboardService.deleteReaction(request);
	}

    // From FreeboardRestController.java
	@PutMapping("/updateReaction") // Path changed from /freeboard/api/updateReaction
	public Map<String, Object> updateReaction(
		@RequestBody Map<String, Object> request,
		@AuthenticationPrincipal AccountDTO user
	) {
		System.out.println("FreeboardMobileRestController - updateReaction");
		request.put("userId", user.getId());
		return freeboardService.updateReaction(request);
	}
	
    // From FreeboardRestController.java
	@PostMapping("/insertReaction") // Path changed from /freeboard/api/insertReaction
	public Map<String, Object> insertReaction(
		@RequestBody Map<String, Object> request,
		@AuthenticationPrincipal AccountDTO user
	) {
		System.out.println("FreeboardMobileRestController - insertReaction");
		request.put("userId", user.getId());
		return freeboardService.insertReaction(request);
	}
}
