package kr.rtustudio.donation.service.chzzk.official.net.http.factory;

import kr.rtustudio.donation.service.chzzk.official.net.http.executor.HttpRequestExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface HttpRequestExecutorFactory {

    @NotNull <Request, Response, HttpClient> Optional<HttpRequestExecutor<Request, Response, HttpClient>> create(@NotNull String type);

}
