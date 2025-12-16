package com.neighbus.club;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neighbus.util.PagingDTO;

// (선택) Lombok을 사용한다면 @Slf4j와 @RequiredArgsConstructor 사용 가능
@Service
public class ClubServiceImpl implements ClubService {

	// (선택) @Slf4j 어노테이션이 없다면 Logger를 직접 선언해야 합니다.
	private static final Logger logger = LoggerFactory.getLogger(ClubServiceImpl.class);

	private final ClubMapper clubMapper; // Mybatis Mapper 주입
	private final com.neighbus.account.AccountMapper accountMapper;

	public ClubServiceImpl(ClubMapper clubMapper, com.neighbus.account.AccountMapper accountMapper) {
		this.clubMapper = clubMapper;
		this.accountMapper = accountMapper;
	}

	/**
	 * 동아리 생성 및 생성자 가입 (트랜잭션 처리)
	 */
	@Override
	@Transactional // 이 메서드가 하나의 트랜잭션으로 실행되도록 보장
	public boolean createClubAndAddCreator(ClubDTO clubDTO) {
		try {
			// 1. 동아리 생성 (clubs 테이블 INSERT)
			int clubResult = clubMapper.insertClub(clubDTO);

			// 2. 생성자 멤버 추가 (club_member 테이블 INSERT)
			// (insertClub 쿼리가 <selectKey>로 DTO에 ID를 다시 넣어준다고 가정)
			int memberResult = clubMapper.addCreatorAsMember(clubDTO);

			// 두 작업이 모두 성공했는지 확인 (1줄 이상 INSERT)
			return clubResult > 0 && memberResult > 0;

		} catch (Exception e) {
			logger.error("동아리 생성 또는 생성자 가입 중 오류 발생", e);
			// @Transactional에 의해 자동 롤백됩니다.
			return false;
		}

	}

	@Override
	@Transactional
	public void deleteClubMember(Long clubId, Long userId) {
		// 1. 매퍼에 전달할 Map 생성
		Map<String, Object> params = new HashMap<>();
		params.put("clubId", clubId);
		params.put("userId", userId);

		// 2. 매퍼 호출
		int deletedRows = clubMapper.deleteClubMember(params);

		// 3. (선택) 삭제가 되었는지 확인
		if (deletedRows == 0) {
			// 예: 해당 멤버가 존재하지 않았을 경우
			throw new RuntimeException("해당 클럽 멤버를 찾을 수 없습니다.");
		}

	}

	/**
	 * 동아리 가입 (중복 확인)
	 */
	@Override
	@Transactional
	public boolean joinClub(ClubMemberDTO clubMemberDTO) {
		// 1. 이미 가입했는지 먼저 확인 (Mapper 호출)
		int count = clubMapper.isMember(clubMemberDTO);

		// 2. 이미 가입했다면(count > 0), false 반환
		if (count > 0) {
			logger.warn("User {} is already a member of club {}", clubMemberDTO.getUserId(), clubMemberDTO.getClubId());
			return false;
		}

		// 3. 가입하지 않았을 때만 INSERT 시도
		try {
			int result = clubMapper.insertClubMember(clubMemberDTO);
			return (result > 0); // 1줄 이상 INSERT 성공 시 true 반환
		} catch (Exception e) {
			logger.error("동아리 가입(INSERT) 중 오류 발생", e);
			return false;
		}
	}

	/**
	 * 모든 동아리 조회
	 */
	@Override
	public List<ClubDTO> getAllClubs() {
		return clubMapper.findAllClubs();
	}

	/**
	 * 동아리 상세 조회
	 */
	@Override
	public ClubDTO getClubById(int id) {
		return clubMapper.getClubById(id);
	}

	/**
	 * 가입 여부 확인 (컨트롤러용)
	 */
	@Override
	public int isMember(ClubMemberDTO clubMemberDTO) {
		// (수정) 이전에 'return 0;'으로 되어있던 부분을 수정
		// Mapper를 호출하여 실제 DB를 확인해야 합니다.
		return clubMapper.isMember(clubMemberDTO);
	}

	@Override
	public PagingDTO<ClubDTO> getClubsWithPaging(ClubDTO clubDTO) {
		int searchCnt = clubMapper.searchCnt(clubDTO);
		Map<String, Integer> pagingMap = com.neighbus.Util.searchUtil(searchCnt, clubDTO.getSelectPageNo(), 9);

		clubDTO.setSearchCnt(searchCnt);
		clubDTO.setSelectPageNo(pagingMap.get("selectPageNo"));
		clubDTO.setRowCnt(pagingMap.get("rowCnt"));
		clubDTO.setBeginPageNo(pagingMap.get("beginPageNo"));
		clubDTO.setEndPageNo(pagingMap.get("endPageNo"));
		clubDTO.setBeginRowNo(pagingMap.get("beginRowNo"));
		clubDTO.setEndRowNo(pagingMap.get("endRowNo"));

		List<ClubDTO> clubs = clubMapper.getClubListWithPaging(clubDTO);
		return new PagingDTO<>(clubs, pagingMap);
	}

	@Override
	public List<Map<String, Object>> getProvince() {
		return accountMapper.getProvince();
	}

	@Override
	public List<Map<String, Object>> getCity() {
		return accountMapper.getCity();
	}

	// 동아리 필터
	@Override
	public List<ClubDTO> getFilteredClubs(ClubDTO clubDTO) {
		if (clubDTO.getCity() == 0) {
			return clubMapper.getOderProvince(clubDTO.getProvinceId());
		} else {
			Map<String, Object> params = new HashMap<>();
			params.put("provinceId", clubDTO.getProvinceId());
			params.put("city", clubDTO.getCity());
			return clubMapper.getOderCity(params);
		}
	}

	@Override
	public List<ClubDTO> getMyClubs(Integer userId) {
		return clubMapper.getMyClubs(userId);
	}

	@Override
	public ClubDetailDTO getClubDetail(int id, com.neighbus.account.AccountDTO accountDTO) {
		ClubDTO club = clubMapper.getClubById(id);
		if (club == null) {
			return null;
		}

		ClubDetailDTO clubDetail = new ClubDetailDTO(club, accountDTO);
		if (clubDetail.isLoggedIn()) {
			ClubMemberDTO memberCheck = new ClubMemberDTO();
			memberCheck.setClubId(id);
			memberCheck.setUserId(accountDTO.getId());
			if (clubMapper.isMember(memberCheck) > 0) {
				clubDetail.setMember(true);
			}
		}
		return clubDetail;
	}

	@Override
	public List<Map<String, Object>> getClubMembers(int clubId) {
		return clubMapper.getClubMembers(clubId);
	}

	@Override
	@Transactional
	public boolean removeClubMember(int clubId, int userId) {
		Map<String, Object> params = new HashMap<>();
		params.put("clubId", clubId);
		params.put("userId", userId);
		int affectedRows = clubMapper.removeClubMember(params);
		return affectedRows > 0;
	}

	@Override
	@Transactional
	public boolean deleteClubByCreator(int clubId, int creatorId) {
		Map<String, Object> params = new HashMap<>();
		params.put("clubId", clubId);
		params.put("creatorId", creatorId);
		int affectedRows = clubMapper.deleteClubByCreator(params);
		return affectedRows > 0;
	}

    @Override
    public boolean isClubNameDuplicate(String clubName) {
        return clubMapper.countByClubName(clubName) > 0;
    }
}
