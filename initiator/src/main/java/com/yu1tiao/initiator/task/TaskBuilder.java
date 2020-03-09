package com.yu1tiao.initiator.task;

import android.os.Process;

import com.yu1tiao.initiator.executor.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mathew
 * @date 2020/3/9
 */
public class TaskBuilder {

    private boolean mIsRunAsSoon = false;
    private boolean mIsNeedWait = false;
    private boolean mIsOnlyInMainProcess = true;
    private ThreadMode mThreadMode = ThreadMode.IO;
    private int mPriority = Process.THREAD_PRIORITY_BACKGROUND;
    private Runnable mTailRunnable;
    private TaskCallback mTaskCallback;
    private List<Class<? extends Task>> mDependsOn;

    public TaskCallback getTaskCallback() {
        return mTaskCallback;
    }

    public TaskBuilder setTaskCallback(TaskCallback mTaskCallback) {
        this.mTaskCallback = mTaskCallback;
        return this;
    }

    public boolean isIsRunAsSoon() {
        return mIsRunAsSoon;
    }

    public TaskBuilder setIsRunAsSoon(boolean mIsRunAsSoon) {
        this.mIsRunAsSoon = mIsRunAsSoon;
        return this;
    }

    public int getPriority() {
        return mPriority;
    }

    public TaskBuilder setPriority(int mPriority) {
        this.mPriority = mPriority;
        return this;
    }

    public boolean isIsNeedWait() {
        return mIsNeedWait;
    }

    public TaskBuilder setIsNeedWait(boolean mIsNeedWait) {
        this.mIsNeedWait = mIsNeedWait;
        return this;
    }

    public ThreadMode getThreadMode() {
        return mThreadMode;
    }

    public TaskBuilder setThreadMode(ThreadMode mThreadMode) {
        this.mThreadMode = mThreadMode;
        return this;
    }

    public Runnable getTailRunnable() {
        return mTailRunnable;
    }

    public TaskBuilder setTailRunnable(Runnable mTailRunnable) {
        this.mTailRunnable = mTailRunnable;
        return this;
    }

    public boolean isIsOnlyInMainProcess() {
        return mIsOnlyInMainProcess;
    }

    public TaskBuilder setIsOnlyInMainProcess(boolean mIsOnlyInMainProcess) {
        this.mIsOnlyInMainProcess = mIsOnlyInMainProcess;
        return this;
    }

    public List<Class<? extends Task>> getDependsOn() {
        return mDependsOn;
    }

    public TaskBuilder addDependsOn(Class<? extends Task> depends) {
        if (mDependsOn == null) {
            mDependsOn = new ArrayList<>();
        }
        mDependsOn.add(depends);
        return this;
    }

    public TaskBuilder setDependsOn(List<Class<? extends Task>> mDependsOn) {
        this.mDependsOn = mDependsOn;
        return this;
    }

    public Task create(final Runnable runnable) {
        return new Task(this) {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }
}
