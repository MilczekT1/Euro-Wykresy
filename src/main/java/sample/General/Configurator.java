package sample.General;

import lombok.Cleanup;

import java.io.*;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public final class Configurator {
    
    private static Logger logger = MyLogger.getLogger();
    private static Configurator instance = new Configurator();
    private static Properties currentSettings;
    
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
        String firstAdress = null;
        String secondAdress = null;
        String user = null;
        String password = null;
        /*String paterekAdress = null;
        String paterekUser = null;
        String paterekPassword = null;*/
        Optional<String> result;
        
        /*
         * Paterek
        result = Utils.getStringFromDialog("Konfiguracja polaczenia", "Konfiguracja polaczenia","Krok 1/3: IP + port do paterka");
        if (result.isPresent())
            paterekAdress = result.get();
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono adresu ip i/lub portu");
            System.exit(1);
        }
        result = Utils.getStringFromDialog("Konfiguracja polaczenia", "Konfiguracja polaczenia","Krok 2/3: Nazwa użytkownika do paterka");
        if (result.isPresent()) {
            paterekUser = result.get();
        }
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono nazwy uzytkownika BD");
            System.exit(1);
        }
    
        result = Utils.getStringFromDialog("Konfiguracja polaczenia", "Konfiguracja polaczenia", "Krok 3/3: Haslo do paterka");
        if (result.isPresent()) {
            paterekPassword = result.get();
        }
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono hasla");
            System.exit(1);
        }
        */
        
        
        result = Utils.getStringFromDialog("Konfiguracja polaczenia", "Konfiguracja polaczenia","Krok 1/4: IP + port");
        if (result.isPresent())
            firstAdress = result.get();
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono adresu ip i/lub portu");
            System.exit(1);
        }
        
        result = Utils.getStringFromDialog("Konfiguracja polaczenia", "Konfiguracja polaczenia", "Krok 2/4: drugie IP + port");
        if (result.isPresent()) {
            secondAdress = result.get();
        }
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono adresu ip i/lub portu");
            System.exit(1);
        }
    
        result = Utils.getStringFromDialog("Konfiguracja polaczenia", "Konfiguracja polaczenia","Krok 3/4: Nazwa użytkownika");
        if (result.isPresent()) {
            user = result.get();
        }
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono nazwy uzytkownika BD");
            System.exit(1);
        }
    
        result = Utils.getStringFromDialog("Konfiguracja polaczenia", "Konfiguracja polaczenia", "Krok 4/4: Haslo");
        if (result.isPresent()) {
            password = result.get();
        }
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono hasla");
            System.exit(1);
        }
        
        Properties defSettings = new Properties();
        defSettings.setProperty("User", user);
        defSettings.setProperty("Password", password);
        defSettings.setProperty("Server-Adress-1", firstAdress);
        defSettings.setProperty("Server-Adress-2", secondAdress);
        /*defSettings.setProperty("Paterek-User", paterekUser);
        defSettings.setProperty("Paterek-Password", paterekPassword);
        defSettings.setProperty("Paterek-Adress", paterekAdress);*/
        
        try {
            currentSettings =  (Properties) defSettings.clone();
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
    private static void importDefaultSettings(){
        try {
            currentSettings = new Properties();
            @Cleanup
            FileInputStream in = new FileInputStream("application.properties");
            currentSettings.load(in);
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
    public static Properties getCurrentSettings(){
        return currentSettings;
    }
}
