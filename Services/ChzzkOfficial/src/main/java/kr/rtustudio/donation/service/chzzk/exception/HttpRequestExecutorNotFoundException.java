package kr.rtustudio.donation.service.chzzk.exception;

import kr.rtustudio.donation.service.chzzk.net.http.executor.Requests;
import kr.rtustudio.donation.service.chzzk.net.http.factory.HttpRequestExecutorFactory;
import org.jetbrains.annotations.NotNull;

public class HttpRequestExecutorNotFoundException extends RuntimeException {

    public HttpRequestExecutorNotFoundException() {
    }

    public HttpRequestExecutorNotFoundException(@NotNull Requests type, @NotNull HttpRequestExecutorFactory factory) {
        this(String.format(
                "Cannot found %s executor in %s factory",
                type, factory.getClass().getSimpleName()
        ));
    }

    public HttpRequestExecutorNotFoundException(String message) {
        super(message);
    }

    public HttpRequestExecutorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpRequestExecutorNotFoundException(Throwable cause) {
        super(cause);
    }

    public HttpRequestExecutorNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
