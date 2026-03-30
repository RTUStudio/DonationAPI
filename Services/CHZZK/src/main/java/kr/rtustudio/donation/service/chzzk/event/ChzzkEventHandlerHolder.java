package kr.rtustudio.donation.service.chzzk.event;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;

public interface ChzzkEventHandlerHolder {

    @NotNull ImmutableSet<ChzzkEventHandler> getHandlers();

}
