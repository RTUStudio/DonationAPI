package kr.rtustudio.donation.common;

import kr.rtustudio.donation.service.chzzk.official.ChzzkService;
import kr.rtustudio.donation.service.chzzk.official.configuration.ChzzkConfig;
import kr.rtustudio.donation.service.chzzk.official.data.ChzzkPlayer;
import kr.rtustudio.donation.service.ssapi.SSAPIService;
import kr.rtustudio.donation.service.ssapi.configuration.SSAPIConfig;
import kr.rtustudio.donation.service.ssapi.data.SSPlayer;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j(topic = "DonationAPI")
public class DonationAPI {

    private SSAPIService ssapiService;
    private ChzzkService chzzkService;

    public SSAPIService getSSAPI() {
        return ssapiService;
    }

    public ChzzkService getChzzk() {
        return chzzkService;
    }

    public void startSSAPI(SSAPIConfig config, Consumer<Donation> donationHandler, Consumer<SSPlayer> registerHandler) {
        if (ssapiService != null) return;
        ssapiService = new SSAPIService(config, donationHandler, registerHandler);
        ssapiService.start();
    }

    public void startChzzk(ChzzkConfig config, Consumer<Donation> donationHandler, Consumer<ChzzkPlayer> registerHandler) {
        if (chzzkService != null) return;
        chzzkService = new ChzzkService(config, donationHandler, registerHandler);
        chzzkService.start();
    }

    public void close() {
        if (ssapiService != null) {
            ssapiService.close();
            ssapiService = null;
        }
        if (chzzkService != null) {
            chzzkService.close();
            chzzkService = null;
        }
    }
}
