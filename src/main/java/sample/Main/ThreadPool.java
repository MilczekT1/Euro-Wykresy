package sample.Main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {
    
    private static ExecutorService Instance = Executors.newCachedThreadPool();
    public static ExecutorService getInstance(){
        return Instance;
    }
}
