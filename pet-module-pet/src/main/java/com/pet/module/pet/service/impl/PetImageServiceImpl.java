package com.pet.module.pet.service.impl;

import com.pet.module.pet.mapper.PetImageMapper;
import com.pet.module.pet.model.entity.PetImage;
import com.pet.module.pet.service.PetImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PetImageServiceImpl implements PetImageService {

    @Autowired
    private PetImageMapper petImageMapper;

    @Override
    public void saveImages(Long petId, List<String> imageUrls) {
        List<PetImage> list = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            PetImage image = new PetImage();
            image.setPetId(petId);
            image.setImageUrl(imageUrls.get(i));
            image.setSortOrder(i);
            list.add(image);
        }
        if (!list.isEmpty()) {
            petImageMapper.insertBatch(list);
        }
    }

    @Override
    public void deleteByPetId(Long petId) {
        petImageMapper.deleteByPetId(petId);
    }
}