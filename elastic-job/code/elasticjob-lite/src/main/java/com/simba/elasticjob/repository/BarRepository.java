package com.simba.elasticjob.repository;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/22 14:25
 * @Version V1.0
 **/
public interface BarRepository {
    /**
     * Get data by ID.
     *
     * @param id ID
     * @return data
     */
    String getById(int id);
}
