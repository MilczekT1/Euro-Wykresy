package pl.konradboniecki.general;

import lombok.Cleanup;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        ArrayList<String> adresses = new ArrayList<>(3);
        ArrayList<String> users = new ArrayList<>(3);
        ArrayList<String> passwords = new ArrayList<>(3);
        Optional<String> result;
        
        setFirstTreblinka(adresses,users,passwords);
        setSecondTreblinka(adresses,users,passwords);
        setPaterek(adresses, users, passwords);
        
        Properties defSettings = new Properties();
        
        defSettings.setProperty("User-Treblinka-1", users.remove(0));
        defSettings.setProperty("Password-Treblinka-1", passwords.remove(0));
        defSettings.setProperty("Adress-Treblinka-1", adresses.remove(0));
        
        defSettings.setProperty("User-Treblinka-2", users.remove(0));
        defSettings.setProperty("Password-Treblinka-2", passwords.remove(0));
        defSettings.setProperty("Adress-Treblinka-2", adresses.remove(0));
        
        defSettings.setProperty("User-Paterek", users.remove(0));
        defSettings.setProperty("Password-Paterek", passwords.remove(0));
        defSettings.setProperty("Paterek-Adress", adresses.remove(0));
        
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
    
    public static String getCurrentProperty(String key){
        return Configurator.getCurrentSettings().getProperty(key);
    }
    
    private static void setPaterek(List<String> adresses, List<String> users, List<String> passwords){
        Optional<String> result;
        
        result = Utils.getStringFromDialog("Konfiguracja polaczenia", "Konfiguracja polaczenia","Krok 1/3: IP + port do paterka");
        if (result.isPresent())
            adresses.add(result.get());
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono adresu ip i/lub portu");
            System.exit(1);
        }
        
        result = Utils.getStringFromDialog("Konfiguracja polaczenia", "Konfiguracja polaczenia","Krok 2/3: Nazwa użytkownika do paterka");
        if (result.isPresent()) {
            users.add(result.get());
        }
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono nazwy uzytkownika BD");
            System.exit(1);
        }
    
        result = Utils.getStringFromDialog("Konfiguracja polaczenia", "Konfiguracja polaczenia", "Krok 3/3: Haslo do paterka");
        if (result.isPresent()) {
            passwords.add(result.get());
        }
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono hasla");
            System.exit(1);
        }
    }
    private static void setFirstTreblinka(List<String> adresses, List<String> users, List<String> passwords){
        Optional<String> result;
        
        result = Utils.getStringFromDialog("Glowny serwer", "Konfiguracja polaczenia","Krok 1/3: IP + port");
        if (result.isPresent())
            adresses.add(result.get());
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono adresu ip i/lub portu");
            System.exit(1);
        }
    
        result = Utils.getStringFromDialog("Glowny serwer", "Konfiguracja polaczenia","Krok 2/3: Nazwa użytkownika");
        if (result.isPresent()) {
            users.add(result.get());
        }
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono nazwy uzytkownika BD");
            System.exit(1);
        }
    
        result = Utils.getStringFromDialog("Glowny serwer", "Konfiguracja polaczenia", "Krok 3/3: Haslo");
        if (result.isPresent()) {
            passwords.add(result.get());
        }
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono hasla");
            System.exit(1);
        }
    }
    private static void setSecondTreblinka(List<String> adresses, List<String> users, List<String> passwords){
        Optional<String> result;
        
        result = Utils.getStringFromDialog("Drugi serwer", "Konfiguracja polaczenia","Krok 1/3: IP + port");
        if (result.isPresent())
            adresses.add(result.get());
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono adresu ip i/lub portu");
            System.exit(1);
        }
        
        result = Utils.getStringFromDialog("Drugi serwer", "Konfiguracja polaczenia","Krok 2/3: Nazwa użytkownika");
        if (result.isPresent()) {
            users.add(result.get());
        }
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono nazwy uzytkownika BD");
            System.exit(1);
        }
        
        result = Utils.getStringFromDialog("Drugi serwer", "Konfiguracja polaczenia", "Krok 3/3: Haslo");
        if (result.isPresent()) {
            passwords.add(result.get());
        }
        else {
            logger.log(Level.CONFIG, "Nie wprowadzono hasla");
            System.exit(1);
        }
    }
}
