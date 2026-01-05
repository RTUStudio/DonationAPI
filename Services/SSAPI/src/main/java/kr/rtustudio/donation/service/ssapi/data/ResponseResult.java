package kr.rtustudio.donation.service.ssapi.data;

public record ResponseResult(int error, String message) {
    public boolean succeeded() {
        return error == 0;
    }
}
