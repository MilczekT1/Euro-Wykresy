package pl.konradboniecki.exceptions;

public class GuiAccessException extends Exception {
    public GuiAccessException(){
        super();
    }
    public GuiAccessException(String message){
        super(message);
    }
    public GuiAccessException(String message, Throwable cause) { super(message, cause); }
    public GuiAccessException(Throwable cause) { super(cause); }
}
