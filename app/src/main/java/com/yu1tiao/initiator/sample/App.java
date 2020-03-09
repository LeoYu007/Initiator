package com.yu1tiao.initiator.sample;

import android.app.Application;

import com.yu1tiao.initiator.Initiator;

/**
 * @author matheew
 * @date 2020/3/9
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Initiator.init(this);

        Initiator.create()
                .addTask(new TaskA())
                .addTask(new TaskE())
                .addTask(new TaskC())
                .addTask(new TaskD())
                .addTask(new TaskB())
                .start();
    }
}
