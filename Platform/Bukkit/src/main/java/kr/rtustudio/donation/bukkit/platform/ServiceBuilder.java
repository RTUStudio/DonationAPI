package kr.rtustudio.donation.bukkit.platform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.platform.adapter.UUIDTypeAdapter;

import kr.rtustudio.donation.common.DonationAPI;
import kr.rtustudio.donation.common.configuration.ServiceConfig;
import kr.rtustudio.donation.service.Disconnectable;
import kr.rtustudio.donation.service.ServiceHandler;
import kr.rtustudio.donation.service.Service;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.data.UserData;
import kr.rtustudio.configurate.model.ConfigurationPart;
import kr.rtustudio.framework.bukkit.api.player.Notifier;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * 서비스 + 플랫폼 통합 빌더
 * <p>
 * 서비스 생성, 플랫폼 등록, 콜백 연결을 한 번에 처리합니다.
 *
 * @param <D> 플랫폼별 연결 데이터 타입
 * @param <S> 서비스 타입
 */
public class ServiceBuilder<D extends UserData, S extends Service> {

    /**
     * -- GETTER --
     *  서비스 인스턴스를 반환합니다.
     */
    @Getter
    private final S service;
    private final DonationPlatform<D> platform;

    private ServiceBuilder(S service, DonationPlatform<D> platform) {
        this.service = service;
        this.platform = platform;
    }

    /**
     * 서비스를 DonationAPI에 등록하고, 플랫폼을 PlatformRegistry에 등록합니다.
     *
     * @param donationAPI      서비스 레지스트리
     * @param platformRegistry 플랫폼 레지스트리
     */
    public void register(DonationAPI donationAPI, PlatformRegistry platformRegistry) {
        platformRegistry.register(platform);
        donationAPI.register(service);
    }

    /**
     * 서비스가 {@link Disconnectable}을 구현하면 해당 플레이어의 연결을 해제합니다.
     *
     * @param uuid 플레이어 UUID
     */
    public void disconnect(UUID uuid) {
        if (service instanceof Disconnectable disconnectable) {
            disconnectable.disconnect(uuid);
        }
    }

    /**
     * 빌더 인스턴스를 생성합니다.
     */
    public static Builder<ConfigurationPart> builder() {
        return new Builder<>();
    }

    /**
     * 서비스 팩토리 인터페이스
     *
     * @param <S> 서비스 타입
     * @param <D> 데이터 타입
     */
    @FunctionalInterface
    public interface ServiceFactory<S extends Service, D extends UserData, C> {
        S create(C config, ServiceHandler<D> handler);
    }

    /**
     * 재연결 함수 인터페이스
     *
     * @param <S> 서비스 타입
     * @param <D> 데이터 타입
     */
    @FunctionalInterface
    public interface ReconnectFunction<S extends Service, D extends UserData> {
        boolean apply(S service, D data);
    }

    /**
     * 서비스 빌더 (1단계: 공통 설정)
     */
    public static class Builder<C extends ConfigurationPart> {
        private Class<C> configClass;
        private final Map<Class<?>, TypeAdapter<?>> typeAdapters = new HashMap<>();

        @SuppressWarnings("unchecked")
        public <N extends ConfigurationPart> Builder<N> config(Class<N> configClass) {
            Builder<N> self = (Builder<N>) this;
            self.configClass = configClass;
            return self;
        }

        public <A> Builder<C> typeAdapter(Class<A> type, TypeAdapter<A> adapter) {
            this.typeAdapters.put(type, adapter);
            return this;
        }

        /**
         * 데이터 클래스를 지정하고 타입이 바인딩된 빌더를 반환합니다.
         */
        public <D extends UserData> TypedBuilder<D, C, Service> data(Class<D> dataClass) {
            return new TypedBuilder<>(this, dataClass);
        }
    }

    /**
     * 서비스 빌더 (2단계: 데이터 타입 바인딩 후)
     *
     * @param <D> 데이터 타입
     */
    public static class TypedBuilder<D extends UserData, C extends ConfigurationPart, S extends Service> {
        private final Builder<C> parent;
        private final Class<D> dataClass;
        private ServiceFactory<S, D, C> factory;
        private ReconnectFunction<S, D> reconnect;

        TypedBuilder(Builder<C> parent, Class<D> dataClass) {
            this.parent = parent;
            this.dataClass = dataClass;
        }

        /**
         * 서비스 팩토리를 설정합니다.
         */
        @SuppressWarnings("unchecked")
        public <NS extends Service> TypedBuilder<D, C, NS> factory(ServiceFactory<NS, D, C> factory) {
            TypedBuilder<D, C, NS> self = (TypedBuilder<D, C, NS>) this;
            self.factory = factory;
            return self;
        }

        /**
         * 재연결 콜백을 설정합니다.
         */
        public TypedBuilder<D, C, S> reconnect(ReconnectFunction<S, D> reconnect) {
            this.reconnect = reconnect;
            return this;
        }

        /**
         * 서비스를 빌드합니다.
         */
        public ServiceBuilder<D, S> build(BukkitDonationAPI plugin) {
            return buildInternal(plugin);
        }

        private ServiceBuilder<D, S> buildInternal(BukkitDonationAPI plugin) {
            if (parent.configClass == null) throw new IllegalStateException("Config class must be set");
            if (factory == null) throw new IllegalStateException("Factory must be set");

            // 설정 로드
            C configInstance = plugin.getConfiguration().get(parent.configClass);
            if (!(configInstance instanceof ServiceConfig enabledConfig)) {
                throw new IllegalStateException("Config must implement ServiceConfig");
            }

            // ServiceHandler 생성
            final java.util.concurrent.atomic.AtomicReference<Service> serviceHolder = new java.util.concurrent.atomic.AtomicReference<>();
            ServiceHandler<D> handler = new ServiceHandler<>(
                    plugin::handleDonation,
                    player -> plugin.getConnectionManager().connect(player.uuid(), serviceHolder.get().getType(), player),
                    null,
                    (UUID uuid, String msgKey, String extra) -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p == null || !p.isOnline()) return;
                            Notifier notifier = Notifier.of(plugin);
                            String serviceName = plugin.getConfiguration().getMessage()
                                    .get(p, "service_name." + serviceHolder.get().getType().name());
                            String color = enabledConfig.getColor();
                            String coloredName = "<color:" + color + ">" + serviceName + "</color>";
                            String key = msgKey;
                            if ("connection.trying".equals(msgKey) && extra != null && !extra.isEmpty()) {
                                key = "connection.trying_id";
                            }
                            String message = plugin.getConfiguration().getMessage().get(p, key)
                                    .replace("{service}", coloredName)
                                    .replace("{id}", extra != null ? extra : "");
                            notifier.announce(p, message);
                        });
                    }
            );

            // 서비스 생성
            S service = factory.create(configInstance, handler);
            serviceHolder.set(service);

            Services serviceType = service.getType();

            // GSON 빌더 생성
            GsonBuilder gsonBuilder = new GsonBuilder()
                    .registerTypeAdapter(UUID.class, new UUIDTypeAdapter());
            for (Map.Entry<Class<?>, TypeAdapter<?>> entry : parent.typeAdapters.entrySet()) {
                gsonBuilder.registerTypeAdapter(entry.getKey(), entry.getValue());
            }

            // reconnect 콜백
            BiFunction<UUID, D, Boolean> reconnectCallback = null;
            if (reconnect != null) {
                reconnectCallback = (uuid, data) -> reconnect.apply(service, data);
            }

            // 플랫폼 생성
            ServiceBuilder.Platform<D> platform = new ServiceBuilder.Platform<>(
                    plugin, serviceType, dataClass, enabledConfig,
                    gsonBuilder.create(), reconnectCallback
            );

            return new ServiceBuilder<>(service, platform);
        }
    }

    /**
     * 내부 플랫폼 구현
     */
    private static class Platform<T extends UserData> extends AbstractDonationPlatform<T> {

        private final Services service;
        private final Class<T> dataClass;
        private final ServiceConfig config;
        private final BiFunction<UUID, T, Boolean> reconnectCallback;

        Platform(
                BukkitDonationAPI plugin,
                Services service,
                Class<T> dataClass,
                ServiceConfig config,
                Gson serializer,
                BiFunction<UUID, T, Boolean> reconnectCallback
        ) {
            super(plugin, serializer);
            this.service = service;
            this.dataClass = dataClass;
            this.config = config;
            this.reconnectCallback = reconnectCallback;
        }

        @Override
        public Services getService() {
            return service;
        }

        @Override
        protected Class<T> dataClass() {
            return dataClass;
        }

        @Override
        public boolean isEnabled() {
            return config.isEnabled();
        }

        @Override
        public boolean isActive(UUID uuid, kr.rtustudio.donation.common.Platform platform) {
            if (isActive(uuid)) {
                T data = getConnection(uuid);
                return data.platform() == platform;
            }
            return false;
        }

        @Override
        protected void onRegister(UUID uuid, T data) {
            if (reconnectCallback != null) {
                reconnectCallback.apply(uuid, data);
            }
        }

        @Override
        protected boolean onReconnect(UUID uuid, T data) {
            if (reconnectCallback != null) {
                return reconnectCallback.apply(uuid, data);
            }
            return true;
        }
    }
}
