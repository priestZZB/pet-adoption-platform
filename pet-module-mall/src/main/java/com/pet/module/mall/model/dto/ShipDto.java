package com.pet.module.mall.model.dto;

/**
 * 发货请求
 */
public class ShipDto {

    private String logisticsNo;
    private String logisticsStatus;

    public String getLogisticsNo() { return logisticsNo; }
    public void setLogisticsNo(String logisticsNo) { this.logisticsNo = logisticsNo; }
    public String getLogisticsStatus() { return logisticsStatus; }
    public void setLogisticsStatus(String logisticsStatus) { this.logisticsStatus = logisticsStatus; }
}