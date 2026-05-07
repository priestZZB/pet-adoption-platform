package com.pet.module.system.model.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户列表返回（管理员用）
 */
public class UserListVo implements Serializable {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private Integer isRealName;
    private Integer status;
    private String volunteerStatus;
    private List<String> roles;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getIsRealName() { return isRealName; }
    public void setIsRealName(Integer isRealName) { this.isRealName = isRealName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getVolunteerStatus() { return volunteerStatus; }
    public void setVolunteerStatus(String volunteerStatus) { this.volunteerStatus = volunteerStatus; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}