package kr.rtustudio.donation.service.chzzk.official;

import kr.rtustudio.donation.service.chzzk.official.data.ChzzkToken;
import org.jetbrains.annotations.NotNull;

public interface ChzzkTokenMutator {

    void setToken(@NotNull ChzzkToken token);

}
