package com.simba.elasticjob.concurrent;


import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/17 14:45
 * @Version V1.0
 **/
public interface Service {
    Service startAsync();

    boolean isRunning();

    Service.State state();

    Service stopAsync();

    void awaitRunning();

    default void awaitRunning(Duration timeout) throws TimeoutException {
        this.awaitRunning(Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
    }

    void awaitRunning(long var1, TimeUnit var3) throws TimeoutException;

    void awaitTerminated();

    default void awaitTerminated(Duration timeout) throws TimeoutException {
        this.awaitTerminated(Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
    }

    void awaitTerminated(long var1, TimeUnit var3) throws TimeoutException;

    Throwable failureCause();

    void addListener(Listener var1, Executor var2);

    public abstract static class Listener {
        public Listener() {
        }

        public void starting() {
        }

        public void running() {
        }

        public void stopping(com.google.common.util.concurrent.Service.State from) {
        }

        public void terminated(com.google.common.util.concurrent.Service.State from) {
        }

        public void failed(com.google.common.util.concurrent.Service.State from, Throwable failure) {
        }
    }

    public static enum State {
        NEW {
            boolean isTerminal() {
                return false;
            }
        },
        STARTING {
            boolean isTerminal() {
                return false;
            }
        },
        RUNNING {
            boolean isTerminal() {
                return false;
            }
        },
        STOPPING {
            boolean isTerminal() {
                return false;
            }
        },
        TERMINATED {
            boolean isTerminal() {
                return true;
            }
        },
        FAILED {
            boolean isTerminal() {
                return true;
            }
        };

        private State() {
        }

        abstract boolean isTerminal();
    }
}
