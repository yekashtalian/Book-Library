package exception;

public class DbUtilOperationException extends RuntimeException{
    public DbUtilOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
