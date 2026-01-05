package kr.rtustudio.donation.service.chzzk.exception;

public class HttpResponseParseException extends Exception {

    public HttpResponseParseException() {
    }

    public HttpResponseParseException(String message) {
        super(message);
    }

    public HttpResponseParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpResponseParseException(Throwable cause) {
        super(cause);
    }

    public HttpResponseParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
