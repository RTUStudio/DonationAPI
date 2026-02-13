package kr.rtustudio.donation.bukkit.platform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.platform.adapter.UUIDTypeAdapter;
import kr.rtustudio.donation.service.data.UserData;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * 설정 가능한 후원 플랫폼
 * <p>
 * 빌더 패턴을 통해 동적으로 플랫폼을 구성할 수 있습니다.
 * 서비스, 설정, 데이터 타입, 직렬화 방식을 지정하여 플랫폼을 생성합니다.
 *
 * @param <T> 연결 데이터 타입 (UserData를 구현해야 함)
 * @param <C> 설정 타입
 */
public class ConfigurableDonationPlatform<T extends UserData, C extends ConfigurationPart> extends AbstractDonationPlatform<T> {

    private final Services service;
    private final Class<T> dataClass;
    private final EnabledConfig config;
    private final BiConsumer<UUID, T> registerCallback;
    private final BiFunction<UUID, T, Boolean> reconnectCallback;

    private ConfigurableDonationPlatform(
            BukkitDonationAPI plugin,
            Services service,
            Class<T> dataClass,
            EnabledConfig config,
            Gson serializer,
            BiConsumer<UUID, T> registerCallback,
            BiFunction<UUID, T, Boolean> reconnectCallback
    ) {
        super(plugin, serializer);
        this.service = service;
        this.dataClass = dataClass;
        this.config = config;
        this.registerCallback = registerCallback;
        this.reconnectCallback = reconnectCallback;
    }

    /**
     * 빌더 인스턴스를 생성합니다.
     *
     * @return 새로운 빌더 인스턴스
     */
    public static Builder<UserData> builder() {
        return new Builder<>();
    }

    @Override
    public Services getService() {
        return service;
    }

    @Override
    public boolean isEnabled() {
        return config.isEnabled();
    }

    @Override
    protected Class<T> dataClass() {
        return dataClass;
    }

    @Override
    public boolean isActive(UUID uuid, Platform platform) {
        if (isActive(uuid)) {
            T data = getConnection(uuid);
            return data.platform() == platform;
        }
        return false;
    }

    @Override
    protected void onRegister(UUID uuid, T data) {
        if (registerCallback != null) {
            registerCallback.accept(uuid, data);
        }
    }

    @Override
    protected boolean onReconnect(UUID uuid, T data) {
        if (reconnectCallback != null) {
            return reconnectCallback.apply(uuid, data);
        }
        return true;
    }

    /**
     * 활성화 가능한 설정 인터페이스
     * 모든 플랫폼 설정은 이 인터페이스를 구현해야 합니다.
     */
    public interface EnabledConfig {
        /**
         * 플랫폼이 활성화되어 있는지 확인합니다.
         *
         * @return 활성화 여부
         */
        boolean isEnabled();
    }

    /**
     * 플랫폼 빌더
     * <p>
     * 플랫폼을 동적으로 구성하기 위한 빌더 클래스입니다.
     *
     * @param <D> 데이터 타입 ({@link #data(Class)} 호출 시 바인딩됨)
     */
    public static class Builder<D extends UserData> {
        private final Map<Class<?>, TypeAdapter<?>> typeAdapters = new HashMap<>();
        private Services service;
        private Class<? extends ConfigurationPart> configClass;
        private Class<D> dataClass;
        private BiConsumer<UUID, D> registerCallback;
        private BiFunction<UUID, D, Boolean> reconnectCallback;

        /**
         * 서비스 타입을 설정합니다.
         *
         * @param service 서비스 타입
         * @return 빌더 인스턴스
         */
        public Builder<D> service(Services service) {
            this.service = service;
            return this;
        }

        /**
         * 설정 클래스를 지정합니다.
         *
         * @param configClass 설정 클래스
         * @return 빌더 인스턴스
         */
        public Builder<D> config(Class<? extends ConfigurationPart> configClass) {
            this.configClass = configClass;
            return this;
        }

        /**
         * 데이터 클래스를 지정합니다.
         * 이후 {@link #on} 콜백의 데이터 타입이 이 클래스로 결정됩니다.
         *
         * @param dataClass 데이터 클래스
         * @param <N>       새 데이터 타입
         * @return 타입이 바인딩된 빌더 인스턴스
         */
        @SuppressWarnings("unchecked")
        public <N extends UserData> Builder<N> data(Class<N> dataClass) {
            Builder<N> self = (Builder<N>) this;
            self.dataClass = dataClass;
            return self;
        }

        /**
         * {@link Event#REGISTER} 이벤트 콜백을 등록합니다.
         * 최초 등록 성공 시 호출됩니다.
         *
         * @param event    {@link Event#REGISTER}
         * @param callback 콜백 (UUID, 데이터)
         * @return 빌더 인스턴스
         */
        public Builder<D> on(Event event, BiConsumer<UUID, D> callback) {
            if (event != Event.REGISTER) {
                throw new IllegalArgumentException("BiConsumer callback is only for REGISTER event, got: " + event);
            }
            this.registerCallback = callback;
            return this;
        }

        /**
         * {@link Event#RECONNECT} 이벤트 콜백을 등록합니다.
         * 재연결 시 호출되며, 성공 여부를 반환해야 합니다.
         *
         * @param event    {@link Event#RECONNECT}
         * @param callback 콜백 (UUID, 데이터) → 성공 여부
         * @return 빌더 인스턴스
         */
        public Builder<D> on(Event event, BiFunction<UUID, D, Boolean> callback) {
            if (event != Event.RECONNECT) {
                throw new IllegalArgumentException("BiFunction callback is only for RECONNECT event, got: " + event);
            }
            this.reconnectCallback = callback;
            return this;
        }

        /**
         * 커스텀 TypeAdapter를 추가합니다.
         * 기본 UUID TypeAdapter에 추가로 등록됩니다.
         *
         * @param type    타입 클래스
         * @param adapter TypeAdapter 인스턴스
         * @param <A>     타입
         * @return 빌더 인스턴스
         */
        public <A> Builder<D> typeAdapter(Class<A> type, TypeAdapter<A> adapter) {
            this.typeAdapters.put(type, adapter);
            return this;
        }

        /**
         * 플랫폼을 빌드합니다.
         *
         * @param plugin 플러그인 인스턴스
         * @param <C>    설정 타입
         * @return 생성된 플랫폼 인스턴스
         * @throws IllegalStateException service, config, data가 설정되지 않은 경우
         */
        public <C extends ConfigurationPart> ConfigurableDonationPlatform<D, C> build(BukkitDonationAPI plugin) {
            if (service == null) throw new IllegalStateException("Service must be set");
            if (configClass == null) throw new IllegalStateException("Config class must be set");
            if (dataClass == null) throw new IllegalStateException("Data class must be set");

            ConfigurationPart configPart = plugin.getConfiguration().get(configClass);
            if (!(configPart instanceof EnabledConfig config)) {
                throw new IllegalStateException("Config must implement EnabledConfig");
            }

            // GSON 빌더 생성 (기본 UUID TypeAdapter 포함)
            GsonBuilder gsonBuilder = new GsonBuilder()
                    .registerTypeAdapter(java.util.UUID.class, new UUIDTypeAdapter()); // UUID 기본 등록

            // 커스텀 TypeAdapter 추가
            for (Map.Entry<Class<?>, TypeAdapter<?>> entry : typeAdapters.entrySet()) {
                gsonBuilder.registerTypeAdapter(entry.getKey(), entry.getValue());
            }
            return new ConfigurableDonationPlatform<>(plugin, service, dataClass, config, gsonBuilder.create(), registerCallback, reconnectCallback);
        }
    }
}
