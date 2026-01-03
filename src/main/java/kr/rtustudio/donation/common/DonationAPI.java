package kr.rtustudio.donation.common;

import kr.rtustudio.donation.service.chzzk.ChzzkService;
import kr.rtustudio.donation.service.chzzk.configuration.ChzzkConfig;
import kr.rtustudio.donation.service.ssapi.SSAPIService;
import kr.rtustudio.donation.service.ssapi.configuration.SSAPIConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j(topic = "DonationAPI")
@RequiredArgsConstructor
public class DonationAPI {

    private SSAPIService ssapiService;
    private ChzzkService chzzkService;

    public void startSSAPI(SSAPIConfig config, Consumer<Donation> handler) {
        if (ssapiService != null) return;
        ssapiService = new SSAPIService(config);
        ssapiService.start(handler);
    }

    public void startChzzk(ChzzkConfig config, Consumer<Donation> handler) {
        if (chzzkService != null) return;
        chzzkService = new ChzzkService(config);
        chzzkService.start(handler);
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
