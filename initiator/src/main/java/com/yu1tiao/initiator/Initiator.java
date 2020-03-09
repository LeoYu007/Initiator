package com.yu1tiao.initiator;

import android.content.Context;
import android.os.Looper;

import androidx.annotation.UiThread;

import com.yu1tiao.initiator.executor.InitiatorExecutor;
import com.yu1tiao.initiator.executor.ThreadMode;
import com.yu1tiao.initiator.sort.TaskSortUtil;
import com.yu1tiao.initiator.task.Task;
import com.yu1tiao.initiator.utils.InitiatorLog;
import com.yu1tiao.initiator.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 启动器调用类
 */
public class Initiator {
    private long mStartTime;
    private static final int WAIT_TIME = 10000;
    private static Context sContext;
    private static boolean sIsMainProcess;
    private static volatile boolean sHasInit;
    private List<Task> mAllTasks = new ArrayList<>();
    private List<Class<? extends Task>> mClsAllTaskClzs = new ArrayList<>();
    private CountDownLatch mCountDownLatch;
    private AtomicInteger mNeedWaitCount = new AtomicInteger();//保存需要Wait的Task的数量
    private List<Task> mNeedWaitTasks = new ArrayList<>();//调用了await的时候还没结束的且需要等待的Task
    private volatile List<Class<? extends Task>> mFinishedTasks = new ArrayList<>(100);//已经结束了的Task
    private HashMap<Class<? extends Task>, ArrayList<Task>> mDependedMap = new HashMap<>();
    private AtomicInteger mAnalyseCount = new AtomicInteger();//启动器分析的次数，统计下分析的耗时；

    private InitiatorExecutor mExecutor;

    private Initiator() {
        mExecutor = new InitiatorExecutor(this);
    }

    public static void init(Context context) {
        if (context != null) {
            sContext = context.getApplicationContext();
            sIsMainProcess = Utils.isMainProcess(sContext);
            sHasInit = true;
        }
    }

    /**
     * 注意：每次获取的都是新对象
     *
     * @return
     */
    public static Initiator create() {
        if (!sHasInit) {
            throw new RuntimeException("must call Initiator.init first");
        }
        return new Initiator();
    }

    public Initiator addTask(Task task) {
        if (task != null) {
            collectDepends(task);
            mAllTasks.add(task);
            mClsAllTaskClzs.add(task.getClass());
            // 非主线程且需要wait的，主线程不需要CountDownLatch也是同步的
            if (ifNeedWait(task)) {
                mNeedWaitTasks.add(task);
                mNeedWaitCount.getAndIncrement();
            }
        }
        return this;
    }

    /**
     * 例如TaskA依赖于TaskB，则把BClass和TaskA存入Map
     * Key：任务的Class     Value：依赖于我的任务集合
     *
     * @param task TaskA
     */
    private void collectDepends(Task task) {
        if (task.dependsOn() != null && task.dependsOn().size() > 0) {
            for (Class<? extends Task> cls : task.dependsOn()) {
                if (mDependedMap.get(cls) == null) {
                    mDependedMap.put(cls, new ArrayList<Task>());
                }
                mDependedMap.get(cls).add(task);
                if (mFinishedTasks.contains(cls)) {
                    task.satisfy();
                }
            }
        }
    }

    private boolean ifNeedWait(Task task) {
        return task.threadMode() != ThreadMode.MAIN && task.needWait();
    }

    @UiThread
    public void start() {
        mStartTime = System.currentTimeMillis();
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException("must be called from UiThread");
        }
        if (mAllTasks.size() > 0) {
            mAnalyseCount.getAndIncrement();
            printDependedMsg();

            mAllTasks = TaskSortUtil.getSortResult(mAllTasks, mClsAllTaskClzs);
            mCountDownLatch = new CountDownLatch(mNeedWaitCount.get());

            // 执行任务
            for (Task task : mAllTasks) {
                if (task.onlyInMainProcess() && !sIsMainProcess) {
                    throw new RuntimeException("task " + task.getClass().getSimpleName() + " only run on main process!");
                }
                mExecutor.submit(task);
                task.setSent(true);
            }

            InitiatorLog.i("task analyse cost " + (System.currentTimeMillis() - mStartTime) + "  begin main ");
        }
        InitiatorLog.i("task analyse cost startTime cost " + (System.currentTimeMillis() - mStartTime));
    }

    public void cancel() {
        mExecutor.cancel();
    }

    /**
     * 查看被依赖的信息
     */
    private void printDependedMsg() {
        InitiatorLog.i("needWait size : " + (mNeedWaitCount.get()));
        if (InitiatorLog.isDebug()) {
            for (Class<? extends Task> cls : mDependedMap.keySet()) {
                InitiatorLog.i("cls " + cls.getSimpleName() + "   " + mDependedMap.get(cls).size());
                for (Task task : mDependedMap.get(cls)) {
                    InitiatorLog.i("cls       " + task.getClass().getSimpleName());
                }
            }
        }
    }

    /**
     * 通知Children一个前置任务已完成
     *
     * @param launchTask
     */
    public void satisfyChildren(Task launchTask) {
        ArrayList<Task> depend = mDependedMap.get(launchTask.getClass());
        if (depend != null && depend.size() > 0) {
            for (Task task : depend) {
                task.satisfy();
            }
        }
    }

    public void markTaskDone(Task task) {
        if (ifNeedWait(task)) {
            mFinishedTasks.add(task.getClass());
            mNeedWaitTasks.remove(task);
            mCountDownLatch.countDown();
            mNeedWaitCount.getAndDecrement();
        }
    }

    public void executeTask(Task task) {
        if (ifNeedWait(task)) {
            mNeedWaitCount.getAndIncrement();
        }
        mExecutor.submit(task);
    }

    @UiThread
    public void await() {
        try {
            if (InitiatorLog.isDebug()) {
                InitiatorLog.i("still has " + mNeedWaitCount.get());
                for (Task task : mNeedWaitTasks) {
                    InitiatorLog.i("needWait: " + task.getClass().getSimpleName());
                }
            }

            if (mNeedWaitCount.get() > 0) {
                mCountDownLatch.await(WAIT_TIME, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public static Context getContext() {
        return sContext;
    }

    public static boolean isMainProcess() {
        return sIsMainProcess;
    }
}
