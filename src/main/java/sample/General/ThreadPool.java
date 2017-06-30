package sample.General;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public final class ThreadPool {
    
    private static ExecutorService Instance = Executors.newFixedThreadPool(8);
    public static ExecutorService getInstance(){
        return Instance;
    }
}
