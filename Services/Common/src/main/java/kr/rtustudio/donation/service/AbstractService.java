package kr.rtustudio.donation.service;

import kr.rtustudio.donation.common.Donation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * 서비스 추상 클래스
 * <p>
 * 모든 서비스는 후원 핸들러와 등록 핸들러를 가집니다.
 *
 * @param <R> 등록 데이터 타입 (서비스별 플레이어 타입)
 */
@Getter
@RequiredArgsConstructor
public abstract class AbstractService<R> implements Service {

    protected final ServiceHandler<R> handler;

}
