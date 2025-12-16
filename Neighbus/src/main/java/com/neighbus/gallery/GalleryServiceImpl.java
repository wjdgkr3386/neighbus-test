package com.neighbus.gallery;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GalleryServiceImpl implements GalleryService {

	@Autowired
	GalleryMapper galleryMapper;
	
	public void insertGallery(GalleryDTO galleryDTO) {
		System.out.println("GalleryServiceImpl - insertGallery");
		if(galleryMapper.insertGallery(galleryDTO)>0) {
			int galleryId = galleryMapper.getGalleryMaxId(galleryDTO);
			galleryDTO.setGalleryId(galleryId);
			galleryMapper.insertGalleryImage(galleryDTO);
		}
	}

	public void updateGallery(GalleryDTO galleryDTO) {
		System.out.println("GalleryServiceImpl - updateGallery");
		System.out.println(1);
		galleryMapper.updateGallery(galleryDTO);
		System.out.println(2);
		galleryMapper.deleteGalleryImage(galleryDTO);
		System.out.println(3);
		galleryMapper.insertGalleryImage(galleryDTO);
		System.out.println(4);
	}
	
	//갤러리 정보와 갤러리 이미지 정보 가져오기
	public List<Map<String ,Object>> getGalleryList(GalleryDTO galleryDTO){
		System.out.println("GalleryServiceImpl - getGalleryList");
		System.out.println(galleryDTO);
		return galleryMapper.getGalleryList(galleryDTO);
	}
	
	public void insertComment(Map<String ,Object> map) {
		System.out.println("GalleryServiceImpl - insertComment");
		galleryMapper.insertComment(map);
	}
	public void updateViewCount(int id) {
		galleryMapper.updateViewCount(id);
	}
	
	public void deleteGalleryById(int galleryId) {
		galleryMapper.deleteGalleryById(galleryId);
	}
    @Override
	public Map<String, Object> insertReaction(Map<String, Object> request) {
    	System.out.println("GalleryServiceImpl - insertReaction");
    	galleryMapper.insertReaction(request);
		return galleryMapper.selectReaction(request);
	}
	
	@Override
	public Map<String, Object> deleteReaction(Map<String, Object> request) {
    	System.out.println("GalleryServiceImpl - deleteReaction");
    	galleryMapper.deleteReaction(request);
		return galleryMapper.selectReaction(request);
	}
	
	@Override
	public Map<String, Object> updateReaction(Map<String, Object> request) {
    	System.out.println("GalleryServiceImpl - updateReaction");
    	galleryMapper.updateReaction(request);
		return galleryMapper.selectReaction(request);
	}
}
