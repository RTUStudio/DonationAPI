package kr.rtustudio.donation.service.soop.net.http.executor;

import kr.rtustudio.donation.service.soop.net.http.client.SOOPHttpClient;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface HttpRequestExecutor<Request, Response, HttpClient> {

    @NotNull Optional<Response> execute(@NotNull SOOPHttpClient<HttpClient> client, @NotNull Request request);

}
