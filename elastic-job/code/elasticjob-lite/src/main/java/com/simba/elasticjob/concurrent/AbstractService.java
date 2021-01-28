package com.simba.elasticjob.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/17 15:14
 * @Version V1.0
 **/
public class AbstractService implements Service {
    @Override
    public Service startAsync() {
        return null;
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public State state() {
        return null;
    }

    @Override
    public Service stopAsync() {
        return null;
    }

    @Override
    public void awaitRunning() {

    }

    @Override
    public void awaitRunning(long var1, TimeUnit var3) throws TimeoutException {

    }

    @Override
    public void awaitTerminated() {

    }

    @Override
    public void awaitTerminated(long var1, TimeUnit var3) throws TimeoutException {

    }

    @Override
    public Throwable failureCause() {
        return null;
    }

    @Override
    public void addListener(Listener var1, Executor var2) {

    }
}
