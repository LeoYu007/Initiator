package com.yu1tiao.initiator.executor;

import com.yu1tiao.initiator.task.Task;

/**
 * @author mathew
 * @date 2020/3/9
 */
public interface IExecutor {

    void submit(Task task);

    void cancel();
}
