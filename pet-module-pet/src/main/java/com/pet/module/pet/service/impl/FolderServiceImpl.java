package com.pet.module.pet.service.impl;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.pet.mapper.PetFavoriteFolderMapper;
import com.pet.module.pet.model.entity.PetFavoriteFolder;
import com.pet.module.pet.service.FolderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FolderServiceImpl implements FolderService {

    private final PetFavoriteFolderMapper folderMapper;

    public FolderServiceImpl(PetFavoriteFolderMapper folderMapper) {
        this.folderMapper = folderMapper;
    }

    @Override
    @Transactional
    public Long create(Long userId, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "收藏夹名称不能为空");
        }
        if (name.length() > 20) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "收藏夹名称不能超过20个字符");
        }
        if (folderMapper.countByNameAndUser(userId, name.trim()) > 0) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "收藏夹名称已存在");
        }
        PetFavoriteFolder folder = new PetFavoriteFolder();
        folder.setUserId(userId);
        folder.setName(name.trim());
        folder.setSortOrder(0);
        folderMapper.insert(folder);
        return folder.getId();
    }

    @Override
    @Transactional
    public void rename(Long userId, Long folderId, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISSING, "收藏夹名称不能为空");
        }
        PetFavoriteFolder folder = folderMapper.selectById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "收藏夹不存在");
        }
        if (!folder.getName().equals(name.trim()) && folderMapper.countByNameAndUser(userId, name.trim()) > 0) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "收藏夹名称已存在");
        }
        folder.setName(name.trim());
        folderMapper.updateById(folder);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long folderId) {
        PetFavoriteFolder folder = folderMapper.selectById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "收藏夹不存在");
        }
        // 删除收藏夹时，该收藏夹下的宠物收藏的 folder_id 置为 null（由数据库 ON DELETE SET NULL 处理）
        folderMapper.deleteById(folderId);
    }

    @Override
    public List<PetFavoriteFolder> getMyFolders(Long userId) {
        return folderMapper.selectByUserId(userId);
    }
}
