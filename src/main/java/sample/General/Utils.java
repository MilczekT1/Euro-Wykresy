package sample.General;

import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public class Utils {

    public static Optional<String> getStringFromDialog(String functionTitle, String title, String description ){
        TextInputDialog dialog = new TextInputDialog(functionTitle);
        dialog.setTitle(title);
        dialog.setHeaderText(description);
        return dialog.showAndWait();
    }
    
}
