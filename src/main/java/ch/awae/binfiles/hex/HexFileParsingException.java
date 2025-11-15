package ch.awae.binfiles.hex;

/**
 * Exception class indicating a format-level processing error in the {@link HexFileReader}.
 */
public class HexFileParsingException extends RuntimeException {
    public HexFileParsingException(String message) {
        super(message);
    }

    public HexFileParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
