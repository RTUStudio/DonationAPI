package kr.rtustudio.donation.service.chzzk.net.http.factory;

import kr.rtustudio.donation.service.chzzk.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.net.http.executor.Requests;
import org.jetbrains.annotations.NotNull;

public interface HttpRequestExecutorFactory {

    @NotNull <Request, Response, HttpClient> HttpRequestExecutor<Request, Response, HttpClient> create(@NotNull Requests type);

}
