package com.yu1tiao.initiator.executor;

import android.os.Handler;
import android.os.Looper;

import com.yu1tiao.initiator.Initiator;
import com.yu1tiao.initiator.task.InitiatorTask;
import com.yu1tiao.initiator.task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * 通过线程模式提交任务到不同的线程池执行
 */
public class InitiatorExecutor implements IExecutor {

    private final Handler mHandler;
    private final List<Future> mFutures;
    private final List<Runnable> mMainRunnable;
    private final Initiator mInitiator;

    public InitiatorExecutor(Initiator initiator) {
        this.mInitiator = initiator;
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mFutures = new ArrayList<>();
        this.mMainRunnable = new ArrayList<>();
    }

    @Override
    public void submit(Task task) {
        InitiatorTask runnable = new InitiatorTask(task, mInitiator);
        Future<?> future = null;
        switch (task.threadMode()) {
            case MAIN:
                mHandler.post(runnable);
                synchronized (mMainRunnable) {
                    mMainRunnable.add(runnable);
                }
                break;
            case CPU:
                future = InitiatorExecutors.getCPUExecutor().submit(runnable);
                break;
            case IO:
            default:
                future = InitiatorExecutors.getIOExecutor().submit(runnable);
        }
        if (future != null) {
            synchronized (mFutures) {
                mFutures.add(future);
            }
        }
    }

    @Override
    public void cancel() {
        if (!mMainRunnable.isEmpty()) {
            for (Runnable r : mMainRunnable)
                mHandler.removeCallbacks(r);

            synchronized (mMainRunnable) {
                mMainRunnable.clear();
            }
        }

        if (!mFutures.isEmpty()) {
            for (Future f : mFutures)
                f.cancel(true);

            synchronized (mFutures) {
                mFutures.clear();
            }
        }
    }
}