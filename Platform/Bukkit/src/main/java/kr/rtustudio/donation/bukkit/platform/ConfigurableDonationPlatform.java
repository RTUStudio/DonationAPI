package kr.rtustudio.donation.bukkit.platform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.platform.adapter.UUIDTypeAdapter;
import kr.rtustudio.donation.bukkit.platform.data.SSAPIData;
import kr.rtustudio.donation.bukkit.platform.data.UserData;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private ConfigurableDonationPlatform(
            BukkitDonationAPI plugin,
            Services service,
            Class<T> dataClass,
            EnabledConfig config,
            Gson serializer
    ) {
        super(plugin, serializer);
        this.service = service;
        this.dataClass = dataClass;
        this.config = config;
    }

    /**
     * 빌더 인스턴스를 생성합니다.
     *
     * @return 새로운 빌더 인스턴스
     */
    public static Builder builder() {
        return new Builder();
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
    protected Class<T> getDataClass() {
        return dataClass;
    }

    @Override
    public boolean isActive(UUID uuid, Platform platform) {
        if (isActive(uuid)) {
            T data = getConnectionData(uuid);
            return data.platform() == platform;
        }
        return false;
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
     */
    public static class Builder {
        private final Map<Class<?>, TypeAdapter<?>> typeAdapters = new HashMap<>();
        private Services service;
        private Class<? extends ConfigurationPart> configClass;
        private Class<? extends UserData> dataClass;

        /**
         * 서비스 타입을 설정합니다.
         *
         * @param service 서비스 타입
         * @return 빌더 인스턴스
         */
        public Builder service(Services service) {
            this.service = service;
            return this;
        }

        /**
         * 설정 클래스를 지정합니다.
         *
         * @param configClass 설정 클래스
         * @return 빌더 인스턴스
         */
        public Builder config(Class<? extends ConfigurationPart> configClass) {
            this.configClass = configClass;
            return this;
        }

        /**
         * 데이터 클래스를 지정합니다.
         *
         * @param dataClass 데이터 클래스
         * @return 빌더 인스턴스
         */
        public Builder data(Class<? extends UserData> dataClass) {
            this.dataClass = dataClass;
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
        public <A> Builder typeAdapter(Class<A> type, TypeAdapter<A> adapter) {
            this.typeAdapters.put(type, adapter);
            return this;
        }

        /**
         * 플랫폼을 빌드합니다.
         *
         * @param plugin 플러그인 인스턴스
         * @param <T>    데이터 타입
         * @param <C>    설정 타입
         * @return 생성된 플랫폼 인스턴스
         * @throws IllegalStateException service, config, data가 설정되지 않은 경우
         */
        @SuppressWarnings("unchecked")
        public <T extends UserData, C extends ConfigurationPart> ConfigurableDonationPlatform<T, C> build(BukkitDonationAPI plugin) {
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
            return new ConfigurableDonationPlatform<>(plugin, service, (Class<T>) dataClass, config, gsonBuilder.create());
        }
    }
}
