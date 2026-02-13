package kr.rtustudio.donation.service.chzzk.official.event;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;

public interface ChzzkEventHandlerHolder {

    @NotNull ImmutableSet<ChzzkEventHandler> getHandlers();

}
