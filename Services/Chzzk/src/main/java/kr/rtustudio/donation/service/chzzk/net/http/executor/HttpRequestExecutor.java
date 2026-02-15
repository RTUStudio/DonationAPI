package kr.rtustudio.donation.service.chzzk.net.http.executor;

import kr.rtustudio.donation.service.chzzk.net.http.client.ChzzkHttpClient;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface HttpRequestExecutor<Request, Response, HttpClient> {

    @NotNull Optional<Response> execute(@NotNull ChzzkHttpClient<HttpClient> client, @NotNull Request request);

}
