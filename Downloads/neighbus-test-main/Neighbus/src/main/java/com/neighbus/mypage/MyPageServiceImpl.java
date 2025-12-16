package com.neighbus.mypage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neighbus.account.AccountDTO;

@Transactional
@Service
public class MyPageServiceImpl implements MyPageService {

	private final PasswordEncoder passwordEncoder = null;
	// ⭐ @Autowired 필드 주입은 구현체 클래스에서만 가능합니다.
	@Autowired
	private MyPageMapper myPageMapper;

	// ⭐ 인터페이스의 메서드를 @Override하여 실제로 구현합니다.
	@Override
	public Map<String, Object> getMyPageInfo(String username) {
		System.out.println("MyPageServiceimpl - getMyPageInfo");
		return myPageMapper.getMyInfo(username);
	}

	@Override
	public List<Map<String, Object>> getMyPosts(String username) {
		System.out.println("MyPageServiceimpl - getMyPosts");
		return myPageMapper.getMyPosts(username);
	}

	@Override
	public List<Map<String, Object>> getMyComments(String username) {
		System.out.println("MyPageServiceimpl - getMyComments");
		return myPageMapper.getMyComments(username);
	}

	@Override
	public int addFriend(int id, String friendCode) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("friendCode", friendCode);
		if (myPageMapper.checkUser(map) < 1 || myPageMapper.checkFriend(map) > 0) {
			return -1;
		}
		myPageMapper.addFriend(map);
		return 1;
	}

	@Override
	public void friendAccept(Map<String, Object> map) {
		myPageMapper.insertFriend(map);
		myPageMapper.updateFriendStateAccept(map);
	}

	@Override
	public void friendReject(Map<String, Object> map) {
		myPageMapper.updateFriendStateReject(map);
	}

	@Override
	public void updateProfile(Map<String, Object> updateData) {
		System.out.println("MyPageServiceImpl - updateProfile");
		myPageMapper.updateProfile(updateData);
	}

	public MyPageServiceImpl(MyPageMapper myPageMapper) {
		super();
		this.myPageMapper = myPageMapper;
	}

	@Override
	public void updateProfileImage(Map<String, Object> updateData) {
		System.out.println("MyPageServiceImpl - updateProfileImage");
		myPageMapper.updateProfileImage(updateData);
	}

	@Override
	public void delMyUser(AccountDTO accountDTO) {
		System.out.println("MyPageServiceImpl - delMyUser for " + accountDTO.getUsername());

		// 1. Get user ID from username
		Map<String, Object> myInfo = myPageMapper.getMyInfo(accountDTO.getUsername());
		if (myInfo == null || myInfo.get("id") == null) {
			System.out.println("User not found: " + accountDTO.getUsername());
			return;
		}
		Integer userId = (Integer) myInfo.get("id");

		// 2. Delete dependent data in order
		
		// Inquiries
		myPageMapper.delMyUserInquiryComments(userId); // Deletes comments on user's inquiries
		myPageMapper.delMyUserInquiryCommentsByWriter(userId); // Deletes comments written by user
		myPageMapper.delMyUserInquiries(userId);

		// Notices
		myPageMapper.delMyUserNotices(userId);
		
		// Clubs (this will cascade delete club_members, freeboards, galleries, etc.)
		myPageMapper.delMyUserClubs(userId);

		// The following are now redundant because of ON DELETE CASCADE in the schema,
		// but we keep them for safety in case the schema changes.
		myPageMapper.delMyUserGalleryComments(userId);
		myPageMapper.delMyUserFreeboardComments(userId);
		myPageMapper.delMyUserGalleries(userId);
		myPageMapper.delMyUserFreeboards(userId);

		// Friend relationships are also ON DELETE CASCADE
		myPageMapper.delMyUserFriendState(userId);
		myPageMapper.delMyUserFriends(userId);

		// 3. Finally, delete the user
		myPageMapper.delMyUser(accountDTO);

		System.out.println("Successfully deleted user and all related data for userId: " + userId);
	}
}