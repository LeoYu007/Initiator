package com.yu1tiao.initiator.executor;

/**
 * @author mathew
 * @date 2020/3/9
 */
public enum ThreadMode {
    /**
     * 主线程
     */
    MAIN,
    /**
     * 执行cpu密集型任务，线程池大小为cpu核心数
     */
    CPU,
    /**
     * io线程，执行io密集型任务，线程池为无边界线程池
     */
    IO
}
