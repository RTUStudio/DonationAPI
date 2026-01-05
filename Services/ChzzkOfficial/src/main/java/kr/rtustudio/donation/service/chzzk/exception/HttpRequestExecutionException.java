package kr.rtustudio.donation.service.chzzk.exception;

public class HttpRequestExecutionException extends Exception {

    public HttpRequestExecutionException() {
    }

    public HttpRequestExecutionException(String message) {
        super(message);
    }

    public HttpRequestExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpRequestExecutionException(Throwable cause) {
        super(cause);
    }

    public HttpRequestExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
