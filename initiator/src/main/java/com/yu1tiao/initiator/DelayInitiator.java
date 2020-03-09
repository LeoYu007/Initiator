package com.yu1tiao.initiator;

import android.os.Looper;
import android.os.MessageQueue;

import com.yu1tiao.initiator.task.InitiatorTask;
import com.yu1tiao.initiator.task.Task;

import java.util.LinkedList;
import java.util.Queue;

public class DelayInitiator {

    private Queue<Task> mDelayTasks = new LinkedList<>();

    private MessageQueue.IdleHandler mIdleHandler = new MessageQueue.IdleHandler() {
        @Override
        public boolean queueIdle() {
            if (mDelayTasks.size() > 0) {
                Task task = mDelayTasks.poll();
                new InitiatorTask(task).run();
            }
            return !mDelayTasks.isEmpty();
        }
    };

    public DelayInitiator addTask(Task task) {
        mDelayTasks.add(task);
        return this;
    }

    public void start() {
        Looper.myQueue().addIdleHandler(mIdleHandler);
    }

}
