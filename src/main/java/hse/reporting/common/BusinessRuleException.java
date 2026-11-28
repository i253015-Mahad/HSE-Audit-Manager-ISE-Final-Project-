package hse.reporting.common;

/** A refused use case with a message that is safe to show to the user. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
