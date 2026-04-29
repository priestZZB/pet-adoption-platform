package com.pet.common.util;

import java.util.List;

/**
 * 分页工具类
 * 配合 PageHelper 使用
 */
public class PageUtils {

    private int pageNum;
    private int pageSize;
    private long total;
    private List<?> list;

    public PageUtils() {}

    public PageUtils(List<?> list, long total, int pageNum, int pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public int getPageNum() { return pageNum; }
    public void setPageNum(int pageNum) { this.pageNum = pageNum; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public List<?> getList() { return list; }
    public void setList(List<?> list) { this.list = list; }
}