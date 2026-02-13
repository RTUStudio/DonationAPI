package kr.rtustudio.donation.bukkit.platform;

/**
 * 플랫폼 이벤트 식별자
 * <p>
 * 빌더에서 {@code on(Event, callback)} 형태로 이벤트 콜백을 등록할 때 사용합니다.
 */
public final class Event {

    /**
     * 플레이어가 플랫폼에 최초 등록될 때 발생합니다.
     * <p>
     * 콜백: {@code BiConsumer<UUID, D>}
     */
    public static final Event REGISTER = new Event("REGISTER");

    /**
     * 플레이어가 재접속하여 저장된 데이터로 재연결할 때 발생합니다.
     * <p>
     * 콜백: {@code BiFunction<UUID, D, Boolean>} — 재연결 성공 여부를 반환
     */
    public static final Event RECONNECT = new Event("RECONNECT");

    private final String name;

    private Event(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
