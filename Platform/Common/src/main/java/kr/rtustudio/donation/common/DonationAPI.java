package kr.rtustudio.donation.common;

import kr.rtustudio.donation.service.Service;
import kr.rtustudio.donation.service.Services;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;
import java.util.Map;

/**
 * 서비스 레지스트리
 * <p>
 * 모든 후원 서비스를 등록하고 관리합니다.
 */
@Slf4j(topic = "DonationAPI")
public class DonationAPI {

    private final Map<Services, Service> services = new EnumMap<>(Services.class);

    /**
     * 서비스를 등록하고 시작합니다.
     * 이미 등록된 서비스는 무시됩니다.
     *
     * @param service 등록할 서비스
     */
    public void register(Service service) {
        Services type = service.getType();
        if (services.containsKey(type)) {
            log.warn("Service {} is already registered", type);
            return;
        }
        services.put(type, service);
        service.start();
        log.info("Service {} registered and started", type);
    }

    /**
     * 서비스를 조회합니다.
     *
     * @param type 서비스 타입
     * @return 서비스 인스턴스 (없으면 null)
     */
    public Service get(Services type) {
        return services.get(type);
    }

    /**
     * 서비스를 타입 안전하게 조회합니다.
     *
     * @param type  서비스 타입
     * @param clazz 서비스 클래스
     * @param <T>   서비스 타입
     * @return 서비스 인스턴스 (없거나 타입 불일치 시 null)
     */
    @SuppressWarnings("unchecked")
    public <T extends Service> T get(Services type, Class<T> clazz) {
        Service service = services.get(type);
        if (clazz.isInstance(service)) {
            return (T) service;
        }
        return null;
    }

    /**
     * 모든 서비스를 종료하고 제거합니다.
     */
    public void close() {
        services.values().forEach(service -> {
            try {
                service.close();
            } catch (Exception e) {
                log.error("Failed to close service {}", service.getType(), e);
            }
        });
        services.clear();
    }
}
