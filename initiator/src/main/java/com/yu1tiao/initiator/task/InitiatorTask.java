package com.yu1tiao.initiator.task;

import android.os.Looper;
import android.os.Process;

import androidx.core.os.TraceCompat;

import com.yu1tiao.initiator.Initiator;
import com.yu1tiao.initiator.utils.InitiatorLog;

/**
 * 任务真正执行的地方
 */
public class InitiatorTask implements Runnable {
    private Task mTask;
    private Initiator mInitiator;

    public InitiatorTask(Task task, Initiator initiator) {
        this.mTask = task;
        this.mInitiator = initiator;
    }

    @Override
    public void run() {
        TraceCompat.beginSection(mTask.getClass().getSimpleName());
        InitiatorLog.i(mTask.getClass().getSimpleName() + " begin run");

        Process.setThreadPriority(mTask.priority());

        long startTime = System.currentTimeMillis();

        mTask.setWaiting(true);
        mTask.waitToSatisfy();

        long waitTime = System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();

        // 执行Task
        mTask.setRunning(true);
        mTask.run();

        // 执行Task的尾部任务
        Runnable tailRunnable = mTask.getTailRunnable();
        if (tailRunnable != null) {
            tailRunnable.run();
        }

        printTaskLog(startTime, waitTime);

        mTask.setFinished(true);
        if (mInitiator != null) {
            mInitiator.satisfyChildren(mTask);
            mInitiator.markTaskDone(mTask);
        }

        InitiatorLog.i(mTask.getClass().getSimpleName() + " finish");
        TraceCompat.endSection();
    }

    /**
     * 打印出来Task执行的日志
     *
     * @param startTime
     * @param waitTime
     */
    private void printTaskLog(long startTime, long waitTime) {
        long runTime = System.currentTimeMillis() - startTime;
        if (InitiatorLog.isDebug()) {
            InitiatorLog.i(mTask.getClass().getSimpleName() + "  wait " + waitTime + "    run "
                    + runTime + "   isMain " + (Looper.getMainLooper() == Looper.myLooper())
                    + "  needWait " + (mTask.needWait() || (Looper.getMainLooper() == Looper.myLooper()))
                    + "  ThreadId " + Thread.currentThread().getId()
                    + "  ThreadName " + Thread.currentThread().getName()
            );
        }
    }

}
