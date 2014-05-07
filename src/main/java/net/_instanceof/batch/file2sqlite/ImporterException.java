/*
 * Copyright(C) 2014 syuu256\gmail.com. All Rights Reserved.
 */
package net._instanceof.batch.file2sqlite;

public class ImporterException extends RuntimeException {

    private static final long serialVersionUID = -584845776323829160L;

    public ImporterException() {
        super();
    }

    public ImporterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImporterException(String message) {
        super(message);
    }

    public ImporterException(Throwable cause) {
        super(cause);
    }
}
