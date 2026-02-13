package kr.rtustudio.donation.bukkit.platform;

import kr.rtustudio.donation.service.data.UserData;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.Services;

import java.util.UUID;

/**
 * 후원 플랫폼 인터페이스
 * <p>
 * 모든 후원 플랫폼(치지직, SSAPI, 유튜브 등)이 구현해야 하는 기본 인터페이스입니다.
 * 플랫폼별 연결, 해제, 활성 상태 확인 등의 기능을 제공합니다.
 *
 * @param <T> 플랫폼별 연결 데이터 타입 (UserData를 구현해야 함)
 */
public interface DonationPlatform<T extends UserData> {

    /**
     * 플랫폼이 속한 서비스 타입을 반환합니다.
     *
     * @return 서비스 타입 (예: SSAPI, ChzzkOfficial)
     */
    Services getService();

    /**
     * 플랫폼이 활성화되어 있는지 확인합니다.
     *
     * @return 활성화 여부
     */
    boolean isEnabled();

    /**
     * 플레이어를 플랫폼에 연결합니다.
     *
     * @param uuid 플레이어 UUID
     * @param data 연결 데이터 객체
     * @return 연결 성공 여부
     */
    boolean connect(UUID uuid, T data);

    /**
     * 플레이어의 플랫폼 연결을 해제합니다.
     *
     * @param uuid 플레이어 UUID
     */
    void disconnect(UUID uuid);

    /**
     * 저장소에서 플레이어의 연결 정보를 불러옵니다.
     *
     * @param uuid 플레이어 UUID
     */
    void load(UUID uuid);

    /**
     * 플랫폼을 초기화합니다.
     * 서버 시작 시 호출됩니다.
     */
    void initialize();

    /**
     * 플랫폼을 종료합니다.
     * 서버 종료 시 호출됩니다.
     */
    void shutdown();

    /**
     * 플레이어가 플랫폼에서 활성 상태인지 확인합니다.
     * 활성 상태란 연결되어 있고 후원을 받을 수 있는 상태를 의미합니다.
     *
     * @param uuid 플레이어 UUID
     * @return 활성 상태 여부
     */
    boolean isActive(UUID uuid);

    /**
     * 특정 플랫폼에서 플레이어가 활성 상태인지 확인합니다.
     * SSAPI와 같이 여러 플랫폼을 지원하는 서비스에서 사용됩니다.
     *
     * @param uuid     플레이어 UUID
     * @param platform 확인할 플랫폼 (CHZZK, SOOP 등)
     * @return 활성 상태 여부
     */
    default boolean isActive(UUID uuid, Platform platform) {
        return isActive(uuid);
    }
}
