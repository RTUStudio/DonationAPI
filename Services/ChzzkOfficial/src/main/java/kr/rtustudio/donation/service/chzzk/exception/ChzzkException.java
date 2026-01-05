package kr.rtustudio.donation.service.chzzk.exception;

public class ChzzkException extends RuntimeException {

    public ChzzkException() {
    }

    public ChzzkException(String message) {
        super(message);
    }

    public ChzzkException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChzzkException(Throwable cause) {
        super(cause);
    }

}
