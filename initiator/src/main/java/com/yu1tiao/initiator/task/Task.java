package com.yu1tiao.initiator.task;

import android.content.Context;
import android.os.Process;

import androidx.annotation.IntRange;

import com.yu1tiao.initiator.Initiator;
import com.yu1tiao.initiator.executor.ThreadMode;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public abstract class Task implements ITask {

    protected Context mContext = Initiator.getContext();

    private volatile boolean mIsWaiting;// 是否正在等待
    private volatile boolean mIsRunning;// 是否正在执行
    private volatile boolean mIsFinished;// Task是否执行完成
    private volatile boolean mIsSent;// Task是否已经被分发

    private TaskCallback mTaskCallback;
    private boolean mIsRunAsSoon;
    private int mPriority;
    private boolean mIsNeedWait;
    private ThreadMode mThreadMode;
    private Runnable mTailRunnable;
    private boolean mIsOnlyInMainProcess;

    private List<Class<? extends Task>> mDependsOn;
    // 当前Task依赖的Task数量（需要等待被依赖的Task执行完毕才能执行自己），默认没有依赖
    private CountDownLatch mDependsCountDownLatch;

    public Task() {
        this(new TaskBuilder());
    }

    public Task(TaskBuilder builder) {
        this.mDependsOn = builder.getDependsOn();
        this.mDependsCountDownLatch = new CountDownLatch(mDependsOn == null ? 0 : mDependsOn.size());

        this.mIsRunAsSoon = builder.isIsRunAsSoon();
        this.mIsOnlyInMainProcess = builder.isIsOnlyInMainProcess();
        this.mIsNeedWait = builder.isIsNeedWait();
        this.mPriority = builder.getPriority();
        this.mThreadMode = builder.getThreadMode();
        this.mTailRunnable = builder.getTailRunnable();
        this.mTaskCallback = builder.getTaskCallback();
    }

    /**
     * 当前Task等待，让依赖的Task先执行
     */
    public void waitToSatisfy() {
        try {
            mDependsCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 依赖的Task执行完一个
     */
    public void satisfy() {
        mDependsCountDownLatch.countDown();
    }

    /**
     * 是否需要尽快执行，解决特殊场景的问题：一个Task耗时非常多但是优先级却一般，很有可能开始的时间较晚，
     * 导致最后只是在等它，这种可以早开始。
     *
     * @return
     */
    @Override
    public boolean needRunAsSoon() {
        return mIsRunAsSoon;
    }

    public void setNeedRunAsSoon(boolean runAsSoon) {
        this.mIsRunAsSoon = runAsSoon;
    }

    /**
     * Task的优先级，运行在主线程则不要去改优先级
     *
     * @return
     */
    @Override
    public int priority() {
        return mPriority;
    }

    public void setPriority(@IntRange(from = Process.THREAD_PRIORITY_FOREGROUND,
            to = Process.THREAD_PRIORITY_LOWEST) int priority) {
        this.mPriority = priority;
    }

    /**
     * 异步线程执行的Task是否需要在被调用await的时候等待，默认不需要
     *
     * @return
     */
    @Override
    public boolean needWait() {
        return mIsNeedWait;
    }

    public void setNeedWait(boolean needWait) {
        this.mIsNeedWait = needWait;
    }

    /**
     * 当前Task依赖的Task集合（需要等待被依赖的Task执行完毕才能执行自己），默认没有依赖
     *
     * @return
     */
    @Override
    public List<Class<? extends Task>> dependsOn() {
        return mDependsOn;
    }

    public void setDependsOn(List<Class<? extends Task>> dependsOn) {
        this.mDependsOn = dependsOn;
        this.mDependsCountDownLatch = new CountDownLatch(mDependsOn == null ? 0 : mDependsOn.size());
    }

    @Override
    public ThreadMode threadMode() {
        return mThreadMode;
    }

    public void setThreadMode(ThreadMode mThreadMode) {
        this.mThreadMode = mThreadMode;
    }

    @Override
    public Runnable getTailRunnable() {
        return mTailRunnable;
    }

    public void setTailRunnable(Runnable mTailRunnable) {
        this.mTailRunnable = mTailRunnable;
    }

    @Override
    public boolean onlyInMainProcess() {
        return mIsOnlyInMainProcess;
    }

    public void setOnlyInMainProcess(boolean mIsOnlyInMainProcess) {
        this.mIsOnlyInMainProcess = mIsOnlyInMainProcess;
    }

    public void setTaskCallback(TaskCallback callBack) {
        this.mTaskCallback = callBack;
    }

    public TaskCallback getTaskCallback() {
        return mTaskCallback;
    }


    public boolean isRunning() {
        return mIsRunning;
    }

    public void setRunning(boolean mIsRunning) {
        this.mIsRunning = mIsRunning;
    }

    public boolean isFinished() {
        return mIsFinished;
    }

    public void setFinished(boolean finished) {
        mIsFinished = finished;
    }

    public boolean isSent() {
        return mIsSent;
    }

    public void setSent(boolean sent) {
        mIsSent = sent;
    }

    public boolean isWaiting() {
        return mIsWaiting;
    }

    public void setWaiting(boolean mIsWaiting) {
        this.mIsWaiting = mIsWaiting;
    }

    @SafeVarargs
    protected final List<Class<? extends Task>> listOf(Class<? extends Task>... clz){
        return Arrays.asList(clz);
    }
}
