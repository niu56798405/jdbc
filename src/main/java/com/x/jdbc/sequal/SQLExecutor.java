package com.x.jdbc.sequal;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SQLExecutor {
    
    private int nThreads;
    
    private ExecutorService[] executors;
    
    private AtomicBoolean isRunning;
    
    public SQLExecutor(int nThreads) {
        this.nThreads = nThreads;
        this.executors = new ExecutorService[nThreads];
        ThreadsFactory threadFactory = new ThreadsFactory();
        for (int i = 0; i < nThreads; i++) {
            this.executors[i] = Executors.newSingleThreadExecutor(threadFactory);
        }
        this.isRunning = new AtomicBoolean(true);
    }

    public void execute(SQLTask task) {
        if(isRunning.get()) {
            ExecutorService exec = getExecutor(task.hashCode());
            if(exec.isShutdown()) {
                task.run();
            } else {
                exec.execute(task);
            }
        } else {
            task.run();
        }
    }
    
    private ExecutorService getExecutor(int hashCode) {
        return executors[hashCode % nThreads];
    }

    public void shutdown() {
        isRunning.set(false);
        Arrays.stream(executors).forEach(ExecutorService::shutdown);
    }
    
    public boolean isShutdown() {
        return !isRunning.get();
    }
    
    public class ThreadsFactory implements ThreadFactory {
        private ThreadGroup group;
        private AtomicInteger number;
        public ThreadsFactory() {
            this.group = new ThreadGroup("jdbc-async");
            this.number = new AtomicInteger();
        }
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, group.getName() + "-" + number.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
    
}
