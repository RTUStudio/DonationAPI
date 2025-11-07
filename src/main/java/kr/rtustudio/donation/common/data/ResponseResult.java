package kr.rtustudio.donation.common.data;

public record ResponseResult(int error, String message) {
    public boolean succeeded() {
        return error == 0;
    }
}
