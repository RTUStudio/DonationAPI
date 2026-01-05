package kr.rtustudio.donation.service.chzzk.event;

import org.jetbrains.annotations.NotNull;

public interface ChzzkEventHandlerHolder {

    @NotNull ChzzkEventHandler getHandler();

}
