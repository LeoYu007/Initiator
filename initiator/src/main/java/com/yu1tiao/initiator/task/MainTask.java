package com.yu1tiao.initiator.task;

public abstract class MainTask extends Task {

    @Override
    public boolean runOnMainThread() {
        return true;
    }
}
