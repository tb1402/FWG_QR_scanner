package de.fwg.qr.scanner.tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This abstract class is the implementation of a basic asynchronous task, based on java.util.concurrent, fitted for the needs in this project.
 * Class is necessary because Android's asyncTask class is deprecated since API level 30
 */
public abstract class asyncTask implements Runnable {

    private ExecutorService executorService= Executors.newSingleThreadExecutor();//executor service, with one workerThread

    /**
     * Method to pass the runnable (this) to the executorService and thus execute it
     */
    public void execute(){
        executorService.execute(this);
    }

    /**
     * Method to stop the executorService, while finishing current task
     */
    public void stop(){
        executorService.shutdown();
    }

    /**
     * run method of runnable, which must be overwritten in the implementation of this class
     */
    @Override
    public abstract void run();
}