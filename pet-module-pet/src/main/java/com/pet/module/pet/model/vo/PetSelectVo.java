package com.pet.module.pet.model.vo;

import java.io.Serializable;

/**
 * 宠物选择下拉列表返回（仅id+名称+状态）
 */
public class PetSelectVo implements Serializable {

    private Long id;
    private String name;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
