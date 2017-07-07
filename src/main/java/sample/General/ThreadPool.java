package sample.General;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public final class ThreadPool {
    
    private static ExecutorService instance = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public static ExecutorService getInstance(){
        return instance;
    }
    public void turnOnAfterShutdown(){
        instance = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
}
