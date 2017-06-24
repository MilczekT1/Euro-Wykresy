package sample.General;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.*;

public class MyLogger {
    
    public static MyLogger instance = new MyLogger();
    private static Logger logger;
    private static FileHandler fileHandler;
    
    public static MyLogger getInstance(){
        return instance;
    }
    private MyLogger() {
        SimpleFormatter simpleFormatter = new SimpleFormatter(){
            @Override
            public synchronized String format(LogRecord record) {
                return LocalDateTime.now().toString() + " " +
                               record.getLevel() + ":" +
                               record.getMessage() + "\n" +
                               "-------------------------------------------------------------------------------------------\n";
            }
        };
        
        logger = Logger.getLogger(MyLogger.class.getName());
        logger.setLevel(Level.CONFIG);
        logger.setUseParentHandlers(false);
    
        addConsoleHandler();
        addFileHandler();
        setOrChangeFormatter(simpleFormatter);
    }
    
    private void addConsoleHandler(){
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.SEVERE);
        logger.addHandler(handler);
    }
    private void addFileHandler(){
        try {
            fileHandler = new FileHandler("Charts_%u.log",true);
        } catch (IOException e) {
            System.exit(1);
        }
        logger.addHandler(fileHandler);
    }
    public void setOrChangeFormatter(SimpleFormatter newSimpleFormatter){
        fileHandler.setFormatter(newSimpleFormatter);
    }
    
    public static Logger getLogger() {
        return logger;
    }
}