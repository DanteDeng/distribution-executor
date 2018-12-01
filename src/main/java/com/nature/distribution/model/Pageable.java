package com.nature.distribution.model;

/**
 * 可分页的
 * @author nature
 * @version 1.0.0
 * @since 2018/11/22 10:17
 */
public interface Pageable {


    /**
     * 是否分页
     * @return 是否分页
     */
    boolean getDoPage();

    /**
     * 设置是否分页
     * @param doPage 是否分页
     */
    void setDoPage(boolean doPage);

    /**
     * 获取分页页码
     * @return 分页页码
     */
    int getPageNum();

    /**
     * 设置分页页码
     * @param pageNum 分页页码
     */
    void setPageNum(int pageNum);

    /**
     * 设置分页size
     * @return 分页size
     */
    int getPageSize();

    /**
     * 设置分页size
     * @param pageSize 分页size
     */
    void setPageSize(int pageSize);
}
