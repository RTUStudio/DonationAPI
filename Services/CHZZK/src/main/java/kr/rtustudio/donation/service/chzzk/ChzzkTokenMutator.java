package kr.rtustudio.donation.service.chzzk;

import kr.rtustudio.donation.service.chzzk.data.ChzzkToken;
import org.jetbrains.annotations.NotNull;

public interface ChzzkTokenMutator {

    void setToken(@NotNull ChzzkToken token);

}
