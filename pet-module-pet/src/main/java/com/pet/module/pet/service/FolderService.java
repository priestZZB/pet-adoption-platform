package com.pet.module.pet.service;

import com.pet.module.pet.model.entity.PetFavoriteFolder;
import java.util.List;

public interface FolderService {

    Long create(Long userId, String name);

    void rename(Long userId, Long folderId, String name);

    void delete(Long userId, Long folderId);

    List<PetFavoriteFolder> getMyFolders(Long userId);
}
