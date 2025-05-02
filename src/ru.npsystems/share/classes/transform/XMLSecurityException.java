package ru.npsystems.transform;

public class XMLSecurityException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Field msgID
     */
    protected String msgID;

    /**
     * Constructor XMLSecurityException
     */
    public XMLSecurityException() {
        super("Missing message string");

        this.msgID = null;
    }

    /**
     * Constructor XMLSecurityException
     *
     * @param msgID
     */
    public XMLSecurityException(String msgID) {
        super();

        this.msgID = msgID;
    }

    /**
     * Constructor XMLSecurityException
     *
     * @param msgID
     * @param exArgs
     */
    public XMLSecurityException(String msgID, Object[] exArgs) {

        super();

        this.msgID = msgID;
    }

    /**
     * Constructor XMLSecurityException
     *
     * @param originalException
     */
    public XMLSecurityException(Exception originalException) {

        super(originalException.getMessage(), originalException);
    }

    /**
     * Constructor XMLSecurityException
     *
     * @param msgID
     * @param originalException
     */
    public XMLSecurityException(Exception originalException, String msgID) {
        super();

        this.msgID = msgID;
    }

    @Deprecated
    public XMLSecurityException(String msgID, Exception originalException) {
        this(originalException, msgID);
    }

    /**
     * Constructor XMLSecurityException
     *
     * @param msgID
     * @param exArgs
     * @param originalException
     */
    public XMLSecurityException(Exception originalException, String msgID, Object[] exArgs) {
        super();

        this.msgID = msgID;
    }

    @Deprecated
    public XMLSecurityException(String msgID, Object[] exArgs, Exception originalException) {
        this(originalException, msgID, exArgs);
    }


    /**
     * Method getMsgID
     *
     * @return the messageId
     */
    public String getMsgID() {
        if (msgID == null) {
            return "Missing message ID";
        }
        return msgID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String s = this.getClass().getName();
        String message = super.getLocalizedMessage();

        if (message != null) {
            message = s + ": " + message;
        } else {
            message = s;
        }

        if (super.getCause() != null) {
            message = message + "\nOriginal Exception was " + super.getCause().toString();
        }

        return message;
    }

    /**
     * Method printStackTrace
     */
    @Override
    public void printStackTrace() {
        synchronized (System.err) {
            super.printStackTrace(System.err);
        }
    }

    /**
     * Method getOriginalException
     *
     * @return the original exception
     */
    public Exception getOriginalException() {
        if (this.getCause() instanceof Exception) {
            return (Exception) this.getCause();
        }
        return null;
    }
}
