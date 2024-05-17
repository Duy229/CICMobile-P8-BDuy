package org.service;

public class CardReaderException extends Exception {
    public CardReaderException(String message) {
        super(message);
    }

    public CardReaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
