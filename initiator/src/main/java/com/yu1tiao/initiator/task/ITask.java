package com.yu1tiao.initiator.task;

import android.os.Process;

import androidx.annotation.IntRange;

import com.yu1tiao.initiator.executor.ThreadMode;

import java.util.List;

public interface ITask {

    void run();

    /**
     * 优先级的范围，可根据Task重要程度及工作量指定；之后根据实际情况决定是否有必要放更大
     */
    @IntRange(from = Process.THREAD_PRIORITY_FOREGROUND, to = Process.THREAD_PRIORITY_LOWEST)
    int priority();

    /**
     * 依赖关系
     *
     * @return
     */
    List<Class<? extends Task>> dependsOn();

    /**
     * 异步线程执行的Task是否需要在被调用await的时候等待，默认不需要
     *
     * @return
     */
    boolean needWait();

    /**
     * 执行任务的线程类型，分为主线程、cpu线程和io线程
     *
     * @return
     */
    ThreadMode threadMode();

    /**
     * 只是在主进程执行
     *
     * @return
     */
    boolean onlyInMainProcess();

    /**
     * 是否需要尽快执行，解决特殊场景的问题：一个Task耗时非常多但是优先级却一般，很有可能开始的时间较晚，
     * 导致最后只是在等它，这种可以早开始。
     *
     * @return
     */
    boolean needRunAsSoon();

    /**
     * Task主任务执行完成之后需要执行的任务
     *
     * @return
     */
    Runnable getTailRunnable();

    void setTaskCallback(TaskCallback callBack);

}
