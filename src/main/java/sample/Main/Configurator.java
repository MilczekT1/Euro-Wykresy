package sample.Main;

import javafx.scene.control.TextInputDialog;
import lombok.Cleanup;

import java.io.*;
import java.util.Optional;
import java.util.Properties;

public final class Configurator {
    
    private static Configurator instance = new Configurator();
    private static Properties currentSettings;
    private static Properties defaultSettings;
    private static Boolean usingDefaultConfiguration;
    
    public static Configurator getInstance(){
        return instance;
    }
    
    private Configurator(){
        File propertiesFile = new File("program.properties");
        if (!propertiesFile.exists()) {
            setDefaultSettings();
        } else {
            importDefaultSettings();
        }
    }
    private static void setDefaultSettings(){
        String adress = null;
        String user = null;
        String password = null;
    
        TextInputDialog adressDialog = new TextInputDialog("Konfiguracja polaczenia");
        adressDialog.setTitle("Konfiguracja polaczenia");
        adressDialog.setHeaderText("Krok 1/3: IP + port");
        
        TextInputDialog userDialog = new TextInputDialog("Konfiguracja polaczenia");
        userDialog.setTitle("Konfiguracja polaczenia");
        userDialog.setHeaderText("Krok 2/3: Nazwa użytkownika");
        
        TextInputDialog passwordDialog = new TextInputDialog("Konfiguracja polaczenia");
        passwordDialog.setTitle("Konfiguracja polaczenia");
        passwordDialog.setHeaderText("Krok 3/3: Haslo");
    
        Optional<String> result = adressDialog.showAndWait();
        if (result.isPresent()) {
            adress = result.get();
        }
        else {
            System.exit(0);
        }
        result = userDialog.showAndWait();
        if (result.isPresent()) {
            user = result.get();
        }
        else {
            System.exit(0);
        }
        result = passwordDialog.showAndWait();
        if (result.isPresent()) {
            password = result.get();
        }
        else {
            System.exit(0);
        }
    
        Properties defSettings = new Properties();
        defSettings.setProperty("Server-Adress",adress);
        defSettings.setProperty("User", user);
        defSettings.setProperty("Password", password);
        usingDefaultConfiguration = false;
        
        try {
            currentSettings =  (Properties) defSettings.clone();
            defaultSettings = (Properties) defSettings.clone();
            @Cleanup
            FileOutputStream out = new FileOutputStream("program.properties");
            currentSettings.store(out, "Ustawienia programu");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void importDefaultSettings(){
        try {
            usingDefaultConfiguration = true;
            defaultSettings = new Properties();
            currentSettings = new Properties();
            @Cleanup
            FileInputStream in = new FileInputStream("program.properties");
            defaultSettings.load(in);
            currentSettings = (Properties) defaultSettings.clone();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void saveCurrentSettings(){
        try {
            @Cleanup
            FileOutputStream out = new FileOutputStream("program.properties");
            currentSettings.store(out, "Ustawienia programu");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void addProperty(String key, String value){
        currentSettings.setProperty(key, value);
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
