package kr.rtustudio.donation.bukkit;

import kr.rtustudio.donation.bukkit.command.MainCommand;
import kr.rtustudio.donation.bukkit.configuration.GlobalConfig;
import kr.rtustudio.donation.bukkit.configuration.service.ChzzkConfig;
import kr.rtustudio.donation.bukkit.configuration.service.SSAPIConfig;
import kr.rtustudio.donation.bukkit.event.DonationEvent;
import kr.rtustudio.donation.bukkit.integration.DonationPlaceholder;
import kr.rtustudio.donation.bukkit.handler.PlayerJoinQuit;
import kr.rtustudio.donation.bukkit.manager.DonationPlayerManager;
import kr.rtustudio.donation.bukkit.manager.PlatformConnectionManager;
import kr.rtustudio.donation.bukkit.platform.ConfigurableDonationPlatform;
import kr.rtustudio.donation.bukkit.platform.Event;
import kr.rtustudio.donation.bukkit.platform.PlatformRegistry;
import kr.rtustudio.donation.bukkit.platform.data.SSAPIData;
import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationAPI;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.chzzk.official.ChzzkService;
import kr.rtustudio.donation.service.chzzk.official.data.ChzzkPlayer;
import kr.rtustudio.donation.service.ssapi.SSAPIService;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.function.Consumer;

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
    private DonationPlayerManager playerManager;

    public BukkitDonationAPI() {
        super("ko_kr");
        loadLibrary("it.unimi.dsi:fastutil:8.5.18");
        loadLibrary("com.squareup.okhttp3:okhttp:4.12.0");
        loadLibrary("org.xerial.snappy:snappy-java:1.1.10.8");
    }

    @Override
    protected void enable() {
        instance = this;
        initializeStorage();
        initializeConfigurations();
        initializePlatforms();
        initializeListeners();
        initializeCommands();
        initializePlaceholder();
        setupServices();
    }

    private void initializeStorage() {
        initStorage("ChzzkOfficial", "ChzzkUnofficial", "SOOP", "SSAPI", "Toonation", "Youtube", "PlayerStatus");
    }

    private void initializeConfigurations() {
        registerConfiguration(GlobalConfig.class, "Global");
        registerConfiguration(SSAPIConfig.class, "Configs/Services", "SSAPI");
        registerConfiguration(ChzzkConfig.class, "Configs/Services", "Chzzk");
    }

    private void initializePlatforms() {
        playerManager = new DonationPlayerManager(this);
        platformRegistry = new PlatformRegistry();

        // 치지직 공식
        platformRegistry.register(
                ConfigurableDonationPlatform.builder()
                        .service(Services.ChzzkOfficial)
                        .config(ChzzkConfig.class)
                        .data(ChzzkPlayer.class)
                        .on(Event.RECONNECT, (uuid, player) -> {
                            if (donationAPI == null) return false;
                            ChzzkService chzzk = donationAPI.get(Services.ChzzkOfficial, ChzzkService.class);
                            if (chzzk == null) return false;
                            return chzzk.reconnect(uuid, player.token());
                        })
                        .build(this)
        );

        // SSAPI
        platformRegistry.register(
                ConfigurableDonationPlatform.builder()
                        .service(Services.SSAPI)
                        .config(SSAPIConfig.class)
                        .data(SSAPIData.class)
                        .build(this)
        );

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

    public void reloadServices() {
        if (donationAPI != null) donationAPI.close();
        setupServices();
    }

    private void setupServices() {
        donationAPI = new DonationAPI();
        Consumer<Donation> donationHandler = this::handleDonation;

        donationAPI.register(new SSAPIService(
                getConfiguration(SSAPIConfig.class), donationHandler, null
        ));
        donationAPI.register(new ChzzkService(
                getConfiguration(ChzzkConfig.class), donationHandler,
                player -> connectionManager.connect(player.uuid(), Services.ChzzkOfficial, player)
        ));
    }

    private void handleDonation(Donation donation) {
        verbose(donation.toString());

        // 후원 이벤트 수신 기록
        if (donation.uniqueId() != null) {
            playerManager.markDonationReceived(donation.uniqueId(), donation.service());
        }

        CraftScheduler.sync(() -> {
            DonationEvent event = new DonationEvent(
                    donation.uniqueId() != null ? Bukkit.getPlayer(donation.uniqueId()) : null,
                    donation
            );
            Bukkit.getPluginManager().callEvent(event);
        });
    }
}
