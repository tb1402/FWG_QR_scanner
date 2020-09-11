package de.fwg.qr.scanner.tools.async;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public abstract class asyncTask implements Runnable {

    private ExecutorService executorService= Executors.newSingleThreadExecutor();

    public void execute(){
        executorService.execute(this);
    }
    public void stop(){
        executorService.shutdown();
    }

    @Override
    public abstract void run();
}