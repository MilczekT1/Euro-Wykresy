package pl.konradboniecki.exceptions;

public class GuiNodeAccessException extends Exception {
    public GuiNodeAccessException(){
        super();
    }
    public GuiNodeAccessException(String message){
        super(message);
    }
    public GuiNodeAccessException(String message, Throwable cause) { super(message, cause); }
    public GuiNodeAccessException(Throwable cause) { super(cause); }
}
