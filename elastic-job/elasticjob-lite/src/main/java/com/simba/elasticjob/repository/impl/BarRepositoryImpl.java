package com.simba.elasticjob.repository.impl;

import com.simba.elasticjob.repository.BarRepository;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/22 14:26
 * @Version V1.0
 **/
public class BarRepositoryImpl implements BarRepository {
    private static final String[] DATA = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};

    @Override
    public String getById(final int id) {
        return id >= 0 && id < DATA.length ? DATA[id] : null;
    }
}
