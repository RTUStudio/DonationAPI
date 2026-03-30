package kr.rtustudio.donation.bukkit;

import kr.rtustudio.donation.bukkit.command.MainCommand;
import kr.rtustudio.donation.bukkit.configuration.GlobalConfig;
import kr.rtustudio.donation.bukkit.configuration.service.ChzzkConfig;
import kr.rtustudio.donation.bukkit.configuration.service.SoopConfig;
import kr.rtustudio.donation.bukkit.configuration.service.CimeConfig;
import kr.rtustudio.donation.bukkit.configuration.service.SSAPIConfig;
import kr.rtustudio.donation.bukkit.configuration.service.ToonationConfig;
import kr.rtustudio.donation.bukkit.configuration.service.YoutubeConfig;
import kr.rtustudio.donation.bukkit.event.DonationEvent;
import kr.rtustudio.donation.bukkit.integration.DonationPlaceholder;
import kr.rtustudio.donation.bukkit.handler.PlayerJoinQuit;
import kr.rtustudio.donation.bukkit.manager.DonationManager;
import kr.rtustudio.donation.bukkit.manager.LiveStatusManager;
import kr.rtustudio.donation.bukkit.manager.PlatformConnectionManager;
import kr.rtustudio.donation.bukkit.platform.PlatformRegistry;
import kr.rtustudio.donation.bukkit.platform.ServiceBuilder;
import kr.rtustudio.donation.service.ssapi.data.SSAPIPlayer;
import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationAPI;
import kr.rtustudio.donation.common.Platform;

import kr.rtustudio.donation.service.chzzk.ChzzkService;
import kr.rtustudio.donation.service.chzzk.data.ChzzkPlayer;
import kr.rtustudio.donation.service.cime.CimeService;
import kr.rtustudio.donation.service.cime.data.CimePlayer;
import kr.rtustudio.donation.service.soop.SoopService;
import kr.rtustudio.donation.service.soop.data.SoopPlayer;
import kr.rtustudio.donation.service.ssapi.SSAPIService;
import kr.rtustudio.donation.service.youtube.YoutubeService;
import kr.rtustudio.donation.service.youtube.data.YoutubePlayer;
import kr.rtustudio.donation.service.cime.live.CimeLiveChecker;
import kr.rtustudio.donation.common.live.LiveStatusChecker;
import kr.rtustudio.donation.service.soop.live.SoopLiveChecker;
import kr.rtustudio.donation.service.chzzk.live.ChzzkLiveChecker;
import kr.rtustudio.donation.service.youtube.live.YoutubeLiveChecker;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.toonation.ToonationService;
import kr.rtustudio.donation.service.toonation.data.ToonationPlayer;
import kr.rtustudio.configurate.model.ConfigPath;
import kr.rtustudio.configurate.model.ConfigurationPart;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtustudio.framework.bukkit.api.player.Notifier;
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class BukkitDonationAPI extends RSPlugin {

    @Getter
    private static BukkitDonationAPI instance;

    @Getter
    private DonationAPI donationAPI;

    @Getter
    private PlatformRegistry platformRegistry;

    @Getter
    private PlatformConnectionManager connectionManager;

    @Getter
    private DonationManager donationManager;

    @Getter
    private LiveStatusManager liveStatusManager;

    private final List<ServiceBuilder<?, ?>> services = new ArrayList<>();
    private GlobalConfig globalConfig;
    private MessageTranslation message;

    public BukkitDonationAPI() {
        super("ko_kr");
        loadLibrary("it.unimi.dsi:fastutil:8.5.18");
        loadLibrary("com.squareup.okhttp3:okhttp:4.12.0");
        loadLibrary("org.xerial.snappy:snappy-java:1.1.10.8");
    }

    @Override
    protected void load() {
        instance = this;
    }

    @Override
    protected void enable() {
        initializeStorage();
        initializeConfigurations();
        globalConfig = getConfiguration(GlobalConfig.class);
        message = getConfiguration().getMessage();
        initializeManagers();
        initializeListeners();
        initializeCommands();
        initializePlaceholder();
        applyPlatformSettings();
        setupServices();
    }

    private void applyPlatformSettings() {
        if (globalConfig == null || globalConfig.getPlatforms() == null) return;
        GlobalConfig.PlatformSettings settings = globalConfig.getPlatforms();
        for (Platform platform : Platform.values()) {
            GlobalConfig.PlatformSettings.PlatformInfo info = settings.get(platform);
            if (info == null) continue;
            if (info.getUnit() != null) platform.setUnit(info.getUnit());
            if (info.getRate() > 0) platform.setRate(info.getRate());
        }
    }

    private void initializeStorage() {
        registerStorage("User");
        for (Services service : Services.values()) {
            registerStorage(service.getStorage());
        }
    }

    private void initializeConfigurations() {
        registerConfiguration(GlobalConfig.class, ConfigPath.of("Global"));
        registerConfiguration(SSAPIConfig.class, ConfigPath.of("Config", "Services", "SSAPI"));
        registerConfiguration(ChzzkConfig.class, ConfigPath.of("Config", "Services", "Chzzk"));
        registerConfiguration(SoopConfig.class, ConfigPath.of("Config", "Services", "SOOP"));
        registerConfiguration(CimeConfig.class, ConfigPath.of("Config", "Services", "Cime"));
        registerConfiguration(ToonationConfig.class, ConfigPath.of("Config", "Services", "Toonation"));
        registerConfiguration(YoutubeConfig.class, ConfigPath.of("Config", "Services", "Youtube"));
    }

    private void initializeManagers() {
        donationManager = new DonationManager(this);
        platformRegistry = new PlatformRegistry();
        connectionManager = new PlatformConnectionManager(platformRegistry);
        liveStatusManager = new LiveStatusManager(platformRegistry);
    }

    private void initializeListeners() {
        registerEvent(new PlayerJoinQuit(this));
    }

    private void initializeCommands() {
        registerCommand(new MainCommand(this), true);
    }

    private void initializePlaceholder() {
        registerIntegration(new DonationPlaceholder(this));
    }

    @Override
    public void disable() {
        if (liveStatusManager != null) liveStatusManager.shutdown();
        if (donationAPI != null) donationAPI.close();
        if (platformRegistry != null) platformRegistry.shutdown();
    }

    public void disconnectServices(UUID uuid) {
        services.forEach(service -> service.disconnect(uuid));
    }

    public void reloadServices() {
        if (liveStatusManager != null) liveStatusManager.shutdown();
        if (donationAPI != null) donationAPI.close();
        if (platformRegistry != null) platformRegistry.shutdown();
        services.clear();
        globalConfig = getConfiguration(GlobalConfig.class);
        message = getConfiguration().getMessage();
        applyPlatformSettings();
        platformRegistry = new PlatformRegistry();
        connectionManager = new PlatformConnectionManager(platformRegistry);
        liveStatusManager = new LiveStatusManager(platformRegistry);
        setupServices();
    }

    private void setupServices() {
        donationAPI = new DonationAPI();

        // SSAPI
        register(ServiceBuilder.builder()
                .config(SSAPIConfig.class)
                .data(SSAPIPlayer.class)
                .factory(SSAPIService::new)
                .build(this)
        );

        // 치지직
        register(ServiceBuilder.builder()
                .config(ChzzkConfig.class)
                .data(ChzzkPlayer.class)
                .factory(ChzzkService::new)
                .reconnect((service, data) -> service.reconnect(data.uuid(), data.token()))
                .build(this)
        );

        // SOOP
        register(ServiceBuilder.builder()
                .config(SoopConfig.class)
                .data(SoopPlayer.class)
                .factory(SoopService::new)
                .reconnect((service, data) -> service.reconnect(data.uuid(), data.token()))
                .build(this)
        );

        // Cime
        register(ServiceBuilder.builder()
                .config(CimeConfig.class)
                .data(CimePlayer.class)
                .factory(CimeService::new)
                .reconnect((service, data) -> service.reconnect(data.uuid(), data))
                .build(this)
        );

        // Toonation
        register(ServiceBuilder.builder()
                .config(ToonationConfig.class)
                .data(ToonationPlayer.class)
                .factory(ToonationService::new)
                .reconnect((service, data) -> service.reconnect(data.uuid(), data))
                .build(this)
        );

        // Youtube
        register(ServiceBuilder.builder()
                .config(YoutubeConfig.class)
                .data(YoutubePlayer.class)
                .factory(YoutubeService::new)
                .reconnect((service, data) -> service.reconnect(data.uuid(), data))
                .build(this)
        );

        // 라이브 상태 체커 등록
        registerLiveCheckers();
    }

    private void registerLiveCheckers() {
        registerLiveChecker(CimeConfig.class, Services.Cime, new CimeLiveChecker(), CimeConfig::getLiveCheckInterval);
        registerLiveChecker(SoopConfig.class, Services.SOOP, new SoopLiveChecker(), SoopConfig::getLiveCheckInterval);
        registerLiveChecker(ChzzkConfig.class, Services.Chzzk, new ChzzkLiveChecker(), ChzzkConfig::getLiveCheckInterval);
        registerLiveChecker(YoutubeConfig.class, Services.Youtube, new YoutubeLiveChecker(), YoutubeConfig::getLiveCheckInterval);
    }

    private <C extends ConfigurationPart & ServiceBuilder.EnabledConfig> void registerLiveChecker(
            Class<C> configClass, Services service, LiveStatusChecker checker, Function<C, Long> intervalExtractor
    ) {
        C config = getConfiguration(configClass);
        if (config != null && config.isEnabled()) {
            liveStatusManager.registerChecker(service, checker, intervalExtractor.apply(config));
        }
    }

    private void register(ServiceBuilder<?, ?> builder) {
        builder.register(donationAPI, platformRegistry);
        services.add(builder);
    }

    public void handleDonation(Donation donation) {
        verbose(donation.toString());

        // 후원 이벤트 수신 기록
        if (donation.uniqueId() != null) {
            donationManager.markDonationReceived(donation.uniqueId(), donation.service());
        }

        CraftScheduler.sync(() -> {
            Player player = donation.uniqueId() != null ? Bukkit.getPlayer(donation.uniqueId()) : null;

            DonationEvent event = new DonationEvent(player, donation);
            Bukkit.getPluginManager().callEvent(event);

            // 후원 알림 전송
            if (!event.isCancelled() && player != null && player.isOnline()) {
                if (globalConfig != null && globalConfig.isDonationNotify()) {
                    Notifier notifier = Notifier.of(BukkitDonationAPI.this);
                    String donationMessage = donation.message() != null ? donation.message() : "";
                    String msg = message.get(player, "donation.notify")
                            .replace("{service}", donation.service().name())
                            .replace("{nickname}", donation.nickname())
                            .replace("{amount}", String.valueOf(donation.amount()))
                            .replace("{price}", String.valueOf(donation.price()))
                            .replace("{unit}", donation.unit())
                            .replace("{message}", donationMessage);
                    notifier.announce(player, msg);
                }
            }
        });
    }
}
