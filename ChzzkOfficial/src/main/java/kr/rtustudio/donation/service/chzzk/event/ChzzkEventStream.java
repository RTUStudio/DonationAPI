package kr.rtustudio.donation.service.chzzk.event;

import kr.rtustudio.donation.service.chzzk.Chzzk;
import kr.rtustudio.donation.service.chzzk.data.ChzzkChatMessage;
import kr.rtustudio.donation.service.chzzk.data.ChzzkDonationMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public final class ChzzkEventStream implements ChzzkEventHandler, AutoCloseable {

    private final SubmissionPublisher<ChzzkEvent<?>> publisher;

    public ChzzkEventStream() {
        this(new SubmissionPublisher<>());
    }

    public ChzzkEventStream(@NotNull SubmissionPublisher<ChzzkEvent<?>> publisher) {
        this.publisher = publisher;
    }

    @Override
    public @NotNull Flow.Publisher<ChzzkEvent<?>> publisher() {
        return publisher;
    }

    @Override
    public void onUserRegistered(@NotNull Chzzk chzzk, @Nullable String user) {
        publisher.submit(ChzzkEvent.userRegistered(chzzk, user));
    }

    @Override
    public void onGrantToken(@NotNull Chzzk chzzk) {
        publisher.submit(ChzzkEvent.tokenGranted(chzzk));
    }

    @Override
    public void onRefreshToken(@NotNull Chzzk chzzk) {
        publisher.submit(ChzzkEvent.tokenRefreshed(chzzk));
    }

    @Override
    public void onRevokeToken(@NotNull Chzzk chzzk) {
        publisher.submit(ChzzkEvent.tokenRevoked(chzzk));
    }

    @Override
    public void onChatMessage(@NotNull Chzzk chzzk, @NotNull ChzzkChatMessage message) {
        publisher.submit(ChzzkEvent.chatMessage(chzzk, message));
    }

    @Override
    public void onDonationMessage(@NotNull Chzzk chzzk, @NotNull ChzzkDonationMessage message) {
        publisher.submit(ChzzkEvent.donationMessage(chzzk, message));
    }

    public <T> @NotNull CompletableFuture<ChzzkEvent<T>> awaitFirst(@NotNull ChzzkEventType type, @NotNull Class<T> payloadType) {
        CompletableFuture<ChzzkEvent<T>> future = new CompletableFuture<>();
        publisher.subscribe(new Flow.Subscriber<>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ChzzkEvent<?> event) {
                if (event.type() == type && payloadType.isInstance(event.payload())) {
                    future.complete(new ChzzkEvent<>(type, event.source(), payloadType.cast(event.payload())));
                    subscription.cancel();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                if (!future.isDone()) {
                    future.completeExceptionally(new IllegalStateException("Event stream completed before event arrived"));
                }
            }

        });
        return future;
    }

    @Override
    public void close() {
        publisher.close();
    }

}