package base;

/**
 * Exception class to wrap thrown exceptions.
 * @author sandeep
 *
 */
public class OlympicException extends Exception {
	private static final long serialVersionUID = 1L;

	public OlympicException() {}

    public OlympicException(String message, Exception nestedException) {
       super(message, nestedException);
    }
    
    public OlympicException(String message) {
        super(message);
     }
}
