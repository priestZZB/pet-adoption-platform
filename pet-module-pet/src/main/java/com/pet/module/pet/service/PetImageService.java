package com.pet.module.pet.service;

import java.util.List;

public interface PetImageService {

    void saveImages(Long petId, List<String> imageUrls);

    void deleteByPetId(Long petId);
}