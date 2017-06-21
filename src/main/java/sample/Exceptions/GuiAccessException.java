package sample.Exceptions;

public class GuiAccessException extends Exception {
    public GuiAccessException(){
        super();
    }
    public GuiAccessException(String message){
        super(message);
    }
}
