package com.yu1tiao.initiator.sample;

import android.util.Log;

import com.yu1tiao.initiator.task.Task;

/**
 * @author matheew
 * @date 2020/3/9
 */
public class TaskB extends Task {
//    @Override
//    public List<Class<? extends Task>> dependsOn() {
//        return listOf(
//                TaskE.class
//        );
//    }
    @Override
    public void run() {

        Log.e("task_", getClass().getSimpleName() + "在" + Thread.currentThread().getName() + "  执行了");
    }
}
