package sample.General;

import javafx.scene.control.TextInputDialog;
import lombok.Cleanup;

import java.io.*;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Configurator {
    
    private static Logger logger = MyLogger.getLogger();
    private static Configurator instance = new Configurator();
    private static Properties currentSettings;
    private static Properties defaultSettings;
    private static Boolean usingDefaultConfiguration;
    
    public static Configurator getInstance(){
        return instance;
    }
    
    private Configurator(){
        File propertiesFile = new File("application.properties");
        if (!propertiesFile.exists()) {
            setDefaultSettings();
            logger.log(Level.CONFIG, "Wprowadzono nowa konfiguracje");
        } else {
            importDefaultSettings();
        }
    }
    private static void setDefaultSettings(){
        String adress = null;
        String user = null;
        String password = null;
        Optional<String> result;
        
        result = Utils.getStringFromDialog("Konfiguracja polaczenia", "Konfiguracja polaczenia","Krok 1/3: IP + port");
        if (result.isPresent())
            adress = result.get();
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono adresu ip i/lub portu");
            System.exit(1);
        }
        
        result = Utils.getStringFromDialog("Konfiguracja polaczenia", "Konfiguracja polaczenia","Krok 2/3: Nazwa użytkownika");
        if (result.isPresent())
            user = result.get();
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono nazwy uzytkownika BD");
            System.exit(1);
        }
    
        result = Utils.getStringFromDialog("Konfiguracja polaczenia", "Konfiguracja polaczenia", "Krok 3/3: Haslo");
        if (result.isPresent())
            password = result.get();
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono hasla");
            System.exit(1);
        }
        //TODO:Max-Gates_Amount:
        Properties defSettings = new Properties();
        defSettings.setProperty("Server-Adress",adress);
        defSettings.setProperty("User", user);
        defSettings.setProperty("Password", password);
        usingDefaultConfiguration = false;
        
        try {
            currentSettings =  (Properties) defSettings.clone();
            defaultSettings = (Properties) defSettings.clone();
            @Cleanup
            FileOutputStream out = new FileOutputStream("application.properties");
            currentSettings.store(out, "Ustawienia programu");
            logger.log(Level.CONFIG, "Pomyslnie dodano konfiguracje programu");
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE,"Nie znaleziono pliku konfiguracyjnego");
            System.exit(1);
        } catch (IOException e) {
            logger.log(Level.SEVERE,"Nieudana proba zapisania pliku konfiguracyjnego");
            System.exit(1);
        }
    }
    public static void importDefaultSettings(){
        try {
            usingDefaultConfiguration = true;
            defaultSettings = new Properties();
            currentSettings = new Properties();
            @Cleanup
            FileInputStream in = new FileInputStream("application.properties");
            defaultSettings.load(in);
            currentSettings = (Properties) defaultSettings.clone();
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE,"Nie znaleziono pliku konfiguracyjnego (prawdopodobnie usuniety recznie)");
            System.exit(1);
        } catch (IOException e) {
            logger.log(Level.SEVERE,"Nieudana proba zaladowania pliku konfiguracyjnego");
            System.exit(1);
        }
    }
    
    public static void saveCurrentSettings(){
        try {
            @Cleanup
            FileOutputStream out = new FileOutputStream("application.properties");
            currentSettings.store(out, "Ustawienia programu");
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING,"Nie znaleziono pliku konfiguracyjnego przy probie zapisu");
        } catch (IOException e) {
            logger.log(Level.WARNING,"Nieudana proba zapisu pliku konfiguracyjnego");
        }
    }
    public void addProperty(String key, String value){
        currentSettings.setProperty(key, value);
        logger.log(Level.CONFIG, "application.properties dodano  " + key + ":" + value);
    }
    
    public static Boolean isUsingDefaultConfiguration(){
        return usingDefaultConfiguration ? true : false;
    }
    public static Properties getDefaultSettings(){
        return defaultSettings;
    }
    public static Properties getCurrentSettings(){
        return currentSettings;
    }
}
