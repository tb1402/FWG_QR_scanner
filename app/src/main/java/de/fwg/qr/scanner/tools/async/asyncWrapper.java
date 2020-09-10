package de.fwg.qr.scanner.tools.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class asyncWrapper<result> {

    private ExecutorService executorService;

    public asyncWrapper(){
        executorService=Executors.newSingleThreadExecutor();
    }

    public void execute(asyncTask<result> at){
        executorService.execute(new FutureTask<result>(at));
    }
}
