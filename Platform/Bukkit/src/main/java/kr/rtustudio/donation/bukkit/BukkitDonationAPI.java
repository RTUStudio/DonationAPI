package kr.rtustudio.donation.bukkit;

import kr.rtustudio.donation.bukkit.command.MainCommand;
import kr.rtustudio.donation.bukkit.configuration.GlobalConfig;
import kr.rtustudio.donation.bukkit.configuration.service.ChzzkConfig;
import kr.rtustudio.donation.bukkit.configuration.service.SOOPConfig;
import kr.rtustudio.donation.bukkit.configuration.service.SSAPIConfig;
import kr.rtustudio.donation.bukkit.event.DonationEvent;
import kr.rtustudio.donation.bukkit.integration.DonationPlaceholder;
import kr.rtustudio.donation.bukkit.handler.PlayerJoinQuit;
import kr.rtustudio.donation.bukkit.manager.DonationManager;
import kr.rtustudio.donation.bukkit.manager.PlatformConnectionManager;
import kr.rtustudio.donation.bukkit.platform.PlatformRegistry;
import kr.rtustudio.donation.bukkit.platform.ServiceBuilder;
import kr.rtustudio.donation.service.ssapi.data.SSAPIPlayer;
import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationAPI;

import kr.rtustudio.donation.service.chzzk.ChzzkService;
import kr.rtustudio.donation.service.chzzk.data.ChzzkPlayer;
import kr.rtustudio.donation.service.soop.SOOPService;
import kr.rtustudio.donation.service.soop.data.SOOPPlayer;
import kr.rtustudio.donation.service.ssapi.SSAPIService;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.player.PlayerChat;
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public BukkitDonationAPI() {
        super("ko_kr");
        loadLibrary("it.unimi.dsi:fastutil:8.5.18");
        loadLibrary("com.squareup.okhttp3:okhttp:4.12.0");
        loadLibrary("org.xerial.snappy:snappy-java:1.1.10.8");
    }

    private final List<ServiceBuilder<?, ?>> services = new ArrayList<>();

    @Override
    protected void enable() {
        instance = this;
        initializeStorage();
        initializeConfigurations();
        initializeManagers();
        initializeListeners();
        initializeCommands();
        initializePlaceholder();
        setupServices();
    }

    private void initializeStorage() {
        initStorage("Chzzk", "SOOP", "SSAPI", "Toonation", "Youtube", "PlayerStatus");
    }

    private void initializeConfigurations() {
        registerConfiguration(GlobalConfig.class, "Global");
        registerConfiguration(SSAPIConfig.class, "Configs/Services", "SSAPI");
        registerConfiguration(ChzzkConfig.class, "Configs/Services", "Chzzk");
        registerConfiguration(SOOPConfig.class, "Configs/Services", "SOOP");
    }

    private void initializeManagers() {
        donationManager = new DonationManager(this);
        platformRegistry = new PlatformRegistry();
        connectionManager = new PlatformConnectionManager(platformRegistry);
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
        if (donationAPI != null) donationAPI.close();
        if (platformRegistry != null) platformRegistry.shutdown();
    }

    public void disconnectServices(UUID uuid) {
        services.forEach(service -> service.disconnect(uuid));
    }

    public void reloadServices() {
        if (donationAPI != null) donationAPI.close();
        if (platformRegistry != null) platformRegistry.shutdown();
        services.clear();
        platformRegistry = new PlatformRegistry();
        connectionManager = new PlatformConnectionManager(platformRegistry);
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
                .config(SOOPConfig.class)
                .data(SOOPPlayer.class)
                .factory(SOOPService::new)
                .reconnect((service, data) -> service.reconnect(data.uuid(), data.token()))
                .build(this)
        );
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
                GlobalConfig globalConfig = getConfiguration(GlobalConfig.class);
                 if (globalConfig != null && globalConfig.isDonationNotify()) {
                    String msg = getConfiguration().getMessage().get(player, "donation.notify")
                            .replace("{service}", donation.service().name())
                            .replace("{nickname}", donation.nickname())
                            .replace("{amount}", String.valueOf(donation.amount()))
                            .replace("{message}", donation.message());
                    PlayerChat.of(this).announce(player, msg);
                }
            }
        });
    }
}
