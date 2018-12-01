package com.nature.distribution.definition;

import com.nature.distribution.model.KeyAndPage;

import java.util.List;

/**
 * 任务
 * @author nature
 * @version 1.0.0
 * @since 2018/11/30 22:03
 */
public interface Work<P extends KeyAndPage, D> {

    /**
     * 查询获取数据总数
     * @param param 查询参数
     * @return 数据总数
     */
    int selectDataTotal(P param);

    /**
     * 查询批次需要处理的全部数据
     * @param param 执行参数
     * @return 数据
     */
    List<D> selectDataList(P param);

    /**
     * 处理数据
     * @param param 执行参数
     * @param datum 数据
     */
    void handleDatum(P param, D datum);
}
