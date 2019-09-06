package com.jiochat.jiochatapp.manager;

import com.jiochat.jiochatapp.av.util.TaskScheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class AsyncRunAgain implements Runnable{

    protected ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    protected boolean isRunning = false;
    protected boolean isRunAgain = false;
    protected int iterationCount;
    private long delayIteration;
    private TaskScheduler taskScheduler;


    public boolean execute(long delayIteration){
        this.delayIteration = delayIteration;
        if(isRunning){
            isRunAgain = true;
        }else{
            isRunning = true;
            taskScheduler = new TaskScheduler(new TaskScheduler.ITaskScheduler() {
                @Override
                public void onSchedule(int repeatCount) {
                    executor.remove(AsyncRunAgain.this);
                    executor.execute(AsyncRunAgain.this);
                }
            });
            taskScheduler.start(delayIteration, 0, false);
        }
        return isRunning;
    }

    @Override
    public void run() {
        isRunAgain = false;
        isRunning = true;
        perFormTask();
        isRunning = false;
        iterationCount++;
        onIterationComplete(isRunAgain);
        if(isRunAgain){
            execute(delayIteration);
        }
    }

    public void stop(){
        if(taskScheduler != null){
            taskScheduler.stop();
        }
        if(executor != null){
            executor.remove(this);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isRunAgain() {
        return isRunAgain;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    protected abstract void perFormTask();

    protected abstract void onIterationComplete(boolean isTaskComplete);
}
