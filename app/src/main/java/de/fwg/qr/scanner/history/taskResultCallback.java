package de.fwg.qr.scanner.history;

public interface taskResultCallback<T> {
    void onFinished(T result);
}
