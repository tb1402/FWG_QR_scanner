package de.fwg.qr.scanner.tools.async;

import java.util.concurrent.Callable;

public abstract class asyncTask<result> implements Callable<result> {

    public abstract result call();


}