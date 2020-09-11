package de.fwg.qr.scanner.tools.async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
public abstract class asyncTask<result> implements Callable<result> {

    private ExecutorService executorService= Executors.newSingleThreadExecutor();

    public void execute(){
        executorService.execute(new FutureTask<>(this));
        executorService.shutdown();
    }

    public abstract result call();

}