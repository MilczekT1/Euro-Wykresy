package sample.General;

import com.google.common.base.Throwables;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.logging.Level;

public class Utils {

    public static Optional<String> getStringFromDialog(String functionTitle, String title, String description ){
        TextInputDialog dialog = new TextInputDialog(functionTitle);
        dialog.setTitle(title);
        dialog.setHeaderText(description);
        return dialog.showAndWait();
    }
    public static void showMessageDialog(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static String hashPassword(String password){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashArray = digest.digest(password.getBytes("UTF-8"));
            return DatatypeConverter.printHexBinary(hashArray);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        }
        return null;
    }
}
