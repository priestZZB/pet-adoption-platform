package com.pet.module.mall.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单表 mall_order
 */
public class MallOrder {

    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
    private String logisticsNo;
    private String logisticsStatus;
    private String logisticsTimeline;
    private String courierCompany;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLogisticsNo() { return logisticsNo; }
    public void setLogisticsNo(String logisticsNo) { this.logisticsNo = logisticsNo; }
    public String getLogisticsStatus() { return logisticsStatus; }
    public void setLogisticsStatus(String logisticsStatus) { this.logisticsStatus = logisticsStatus; }
    public String getLogisticsTimeline() { return logisticsTimeline; }
    public void setLogisticsTimeline(String logisticsTimeline) { this.logisticsTimeline = logisticsTimeline; }
    public String getCourierCompany() { return courierCompany; }
    public void setCourierCompany(String courierCompany) { this.courierCompany = courierCompany; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}