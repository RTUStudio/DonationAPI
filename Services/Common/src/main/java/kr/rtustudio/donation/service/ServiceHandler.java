package kr.rtustudio.donation.service;

import kr.rtustudio.donation.common.Donation;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * 서비스 핸들러
 * <p>
 * 후원 핸들러, 등록 성공/실패 핸들러를 하나로 묶습니다.
 *
 * @param donation  후원 이벤트 핸들러
 * @param success 등록 성공 핸들러
 * @param failure 등록 실패 핸들러 (nullable)
 * @param <R> 등록 데이터 타입
 */
public record ServiceHandler<R>(
        Consumer<Donation> donation,
        Consumer<R> success,
        Consumer<UUID> failure
) {

    public ServiceHandler(Consumer<Donation> donation, Consumer<R> success) {
        this(donation, success, null);
    }

}
