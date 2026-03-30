# DonationAPI 아키텍처

이 문서는 DonationAPI의 설계 철학, 모듈 구조, 계층 간 상호작용, 데이터 흐름을 상세하게 기술합니다.

---

## 설계 원칙

### 1. Platform-Service 분리

프로젝트는 **비즈니스 로직(Services)**과 **프레임워크 종속 코드(Platform)**를 물리적으로 분리합니다.

- **Services** — 순수 Java로 작성된 플랫폼별 연동 구현체. Bukkit, Sponge 등 어떤 프레임워크에도 의존하지 않습니다.
- **Platform** — 특정 프레임워크(Bukkit 등)의 API를 활용하여 Service와 게임 서버를 연결하는 어댑터 계층입니다.

이 분리를 통해 Service 계층은 단독으로 테스트 가능하며, 향후 다른 프레임워크로의 이식이 용이합니다.

### 2. 통합 이벤트 모델

서로 다른 후원 플랫폼의 데이터를 단일 `Donation` 레코드로 추상화합니다.  
하위 플러그인은 플랫폼 차이를 의식하지 않고 동일한 `DonationEvent`를 수신할 수 있습니다.

### 3. 선언적 서비스 등록

`ServiceBuilder` 패턴을 통해 새로운 플랫폼 추가 시 보일러플레이트를 최소화합니다.  
설정 바인딩, 핸들러 연결, 플랫폼 등록이 체이닝으로 이루어집니다.

---

## 모듈 구조

```
DonationAPI/
│
├── Services/                          # 프레임워크 독립적 코어 서비스
│   ├── Common/                        # 공통 모델 및 인터페이스
│   │   ├── common/
│   │   │   ├── Donation.java          # 통합 후원 데이터 레코드
│   │   │   ├── DonationType.java      # CHAT | VIDEO
│   │   │   ├── Platform.java          # CHZZK | SOOP | YOUTUBE | TOONATION | CIME
│   │   │   ├── Response.java          # 공통 응답 모델
│   │   │   ├── data/
│   │   │   │   └── LiveStatus.java    # 라이브 상태 데이터
│   │   │   ├── live/
│   │   │   │   └── LiveStatusChecker  # 라이브 상태 체커 인터페이스
│   │   │   └── net/
│   │   │       ├── WebSocketClient    # 공통 WebSocket 클라이언트
│   │   │       └── PollingClient      # 공통 HTTP 폴링 클라이언트
│   │   └── service/
│   │       ├── Service.java           # 서비스 라이프사이클 인터페이스
│   │       ├── AbstractService.java   # 서비스 추상 클래스 (ServiceHandler 보유)
│   │       ├── ServiceHandler.java    # 콜백 번들 (donation, success, failure)
│   │       ├── Services.java          # 서비스 열거형 (SSAPI, Chzzk, SOOP ...)
│   │       ├── Disconnectable.java    # 플레이어 단위 연결 해제 인터페이스
│   │       ├── auth/
│   │       │   └── AuthResponsePage   # OAuth 인증 응답 페이지
│   │       └── data/
│   │           └── UserData.java      # 플랫폼별 사용자 데이터 인터페이스
│   │
│   ├── CHZZK/                         # 치지직 OpenAPI 구현체
│   │   ├── ChzzkService.java          #   서비스 엔트리포인트
│   │   ├── Chzzk.java                 #   API 클라이언트
│   │   ├── ChzzkSubscriber.java       #   WebSocket 후원 수신기
│   │   ├── ChzzkAuthServer.java       #   OAuth 로컬 서버
│   │   └── live/ChzzkLiveChecker      #   라이브 상태 체커
│   │
│   ├── SOOP/                          # 숲 OpenAPI 구현체
│   │   └── (바이너리 패킷 해석기 포함)
│   │
│   ├── SSAPI/                         # SSAPI Socket.io 통합 허브
│   ├── Toonation/                     # 투네이션 비공식 WebSocket
│   ├── CIME/                          # 씨메 HTTP API 폴링
│   └── Youtube/                       # 유튜브 라이브챗 폴링
│
└── Platform/                          # 프레임워크 종속 어댑터
    ├── Common/                        # 플랫폼 공통 API
    │   └── DonationAPI.java           #   서비스 레지스트리 (register/get/close)
    │
    └── Bukkit/                        # Bukkit 통합 계층
        ├── BukkitDonationAPI.java     #   메인 플러그인 클래스 (RSPlugin)
        ├── event/
        │   └── DonationEvent.java     #   Bukkit 이벤트 (Cancellable)
        ├── command/
        │   ├── MainCommand.java       #   메인 명령어 (/donationapi)
        │   ├── chzzk/                 #   치지직 서브커맨드
        │   ├── soop/                  #   숲 서브커맨드
        │   ├── ssapi/                 #   SSAPI 서브커맨드
        │   ├── toonation/             #   투네이션 서브커맨드
        │   ├── cime/                  #   씨메 서브커맨드
        │   ├── youtube/               #   유튜브 서브커맨드
        │   └── event/                 #   테스트 이벤트 커맨드
        ├── platform/
        │   ├── ServiceBuilder.java    #   서비스+플랫폼 통합 빌더
        │   ├── DonationPlatform.java  #   플랫폼 인터페이스
        │   ├── AbstractDonation...    #   플랫폼 추상 구현 (연결/저장/알림)
        │   └── PlatformRegistry.java  #   플랫폼 레지스트리
        ├── manager/
        │   ├── DonationManager.java   #   플레이어 데이터 저장소 연동
        │   ├── PlatformConnection...  #   플랫폼 연결/해제 관리
        │   └── LiveStatusManager.java #   라이브 상태 주기적 폴링 캐시
        ├── configuration/
        │   ├── GlobalConfig.java      #   전역 설정
        │   └── service/               #   플랫폼별 설정 (ChzzkConfig, SoopConfig ...)
        ├── integration/
        │   └── DonationPlaceholder    #   PlaceholderAPI 연동
        └── handler/
            └── PlayerJoinQuit         #   플레이어 입퇴장 리스너
```

---

## 핵심 인터페이스 및 클래스

### Service 계층 (Services/Common)

- **`Service`** (interface) — 서비스 라이프사이클
  - `getType()` → `Services`
  - `getPlatforms()` → `List<Platform>`
  - `start()` / `close()`
- **`AbstractService<R>`** extends Service — `ServiceHandler`를 보유하는 기본 구현
  - **`ServiceHandler<R>`** — 콜백 번들
    - `donation`: `Consumer<Donation>` — 후원 이벤트 수신
    - `success`: `Consumer<R>` — 등록 성공
    - `failure`: `Consumer<UUID>` — 등록 실패

| 인터페이스/클래스 | 역할 |
|------------------|------|
| `Service` | 서비스 라이프사이클 (start/close) |
| `AbstractService<R>` | ServiceHandler를 보유하는 서비스 기본 구현 |
| `ServiceHandler<R>` | 후원 수신, 등록 성공/실패 콜백 번들 |
| `Services` (enum) | 서비스 유형 열거 + 지원 Platform 매핑 |
| `Disconnectable` | 플레이어 단위 연결 해제 기능 |
| `UserData` | 플랫폼별 사용자 데이터 (uuid, platform, channelId) |

### Platform 계층 (Platform/Bukkit)

- **`DonationPlatform<T>`** (interface) — 플랫폼 연결/해제/상태 인터페이스
  - `connect(UUID, T)` → `boolean`
  - `disconnect(UUID)`
  - `load(UUID)`
  - `isActive(UUID)` / `isActive(UUID, Platform)`
  - `isEnabled()`, `initialize()`, `shutdown()`
- **`AbstractDonationPlatform<T>`** implements DonationPlatform — 기본 구현
  - `connections: ConcurrentHashMap<UUID, T>` — 연결 상태 관리
  - `connect()` → `save()` + `announce()` — 저장소 영구 저장 + 알림
  - `load()` → Storage 조회 → `onReconnect()` — 재연결 훅
  - `isActive()` → `DonationManager` 로 위임
  - `onRegister()` / `onReconnect()` — 하위 클래스 훅 포인트

| 인터페이스/클래스 | 역할 |
|------------------|------|
| `DonationPlatform<T>` | 플랫폼 연결/해제/상태 인터페이스 |
| `AbstractDonationPlatform<T>` | 저장소 연동, 알림 발송, 연결 상태 관리 기본 구현 |
| `PlatformRegistry` | 모든 DonationPlatform을 관리하는 중앙 레지스트리 |
| `PlatformConnectionManager` | 플레이어-플랫폼 연결/해제/로드 파사드 |

### Manager 계층

| 매니저 | 역할 |
|--------|------|
| `DonationManager` | 플레이어 엔티티(DonationEntity) 수명주기 관리, Storage 연동 |
| `PlatformConnectionManager` | 플랫폼 연결/해제 파사드, Registry를 통한 간접 접근 |
| `LiveStatusManager` | ScheduledExecutorService 기반 라이브 상태 주기적 폴링 및 캐싱 |

---

## 데이터 흐름

### 후원 수신 흐름

1. 외부 플랫폼(WebSocket/Polling)에서 원본 데이터 수신
2. **Service 구현체** (ChzzkService 등) → `Donation` 레코드로 변환
3. `ServiceHandler.donation()` 콜백 호출 (`Consumer<Donation>`)
4. **BukkitDonationAPI.handleDonation()**
   - 후원 수신 기록 (`DonationManager`)
   - 메인 스레드로 전환 (`CraftScheduler.sync`)
   - `DonationEvent` 생성 및 Bukkit 이벤트 발행
   - 후원 알림 메시지 발송 (`Notifier`)
5. **외부 플러그인** → `DonationEvent` 리스너로 통합 데이터 접근

### 플레이어 연동 흐름

1. 플레이어가 `/후원API 치지직 연동` 실행
2. **ChzzkCommand** → OAuth 인증 서버 시작 (`ChzzkAuthServer`)
3. 브라우저에서 인증 완료 → 인증 토큰 수신
4. **ChzzkService** → 토큰으로 WebSocket 연결 → `ServiceHandler.success()` 콜백 호출
5. **PlatformConnectionManager.connect()** → `DonationPlatform.connect(uuid, data)`
6. **AbstractDonationPlatform**
   - `connections` 맵에 추가
   - Storage에 영구 저장
   - `DonationManager.markConnected()`
   - 연결 성공 알림 발송

### 서버 시작 시 자동 재연결 흐름

```
플레이어 접속 (PlayerJoinEvent)
    │
    ▼
PlayerJoinQuit 리스너
    │
    ├── DonationManager.load(uuid)
    │     → Storage에서 PlatformStatusComponent 로드
    │     → DonationEntity 생성 및 모듈 등록
    │
    └── PlatformConnectionManager.loadAll(uuid)
          │
          ├── ChzzkPlatform.load(uuid)
          │     → Storage에서 ChzzkPlayer 로드
          │     → onReconnect() → ChzzkService.reconnect()
          │     → connections 맵에 추가
          │
          ├── SoopPlatform.load(uuid)
          │     → (동일한 패턴)
          │
          └── ... (활성화된 모든 플랫폼)
```

---

## ServiceBuilder 내부 동작

`ServiceBuilder`는 서비스 생성과 플랫폼 등록을 하나의 선언적 체인으로 처리합니다.

### 빌드 과정

```
builder()
  │
  ├── .config(ChzzkConfig.class)        ─── 설정 클래스 바인딩
  ├── .data(ChzzkPlayer.class)          ─── 데이터 타입 바인딩 → TypedBuilder 전환
  ├── .factory(ChzzkService::new)       ─── ServiceFactory 지정
  ├── .reconnect(...)                   ─── ReconnectFunction 지정 (선택)
  └── .build(plugin)                    ─── 최종 조립
        │
        ├── 1. Config 로드 및 EnabledConfig 검증
        ├── 2. ServiceHandler<D> 생성
        │      donation  → plugin::handleDonation
        │      success   → connectionManager.connect()
        ├── 3. Service 인스턴스 생성 (factory.create)
        ├── 4. GSON 빌더 조립 (UUIDTypeAdapter + 커스텀)
        ├── 5. AbstractDonationPlatform 내부 구현체 생성
        └── 6. ServiceBuilder<D, S> 반환
              │
              └── register(donationAPI, platformRegistry)
                    ├── platformRegistry.register(platform)
                    └── donationAPI.register(service)
```

### ServiceHandler 콜백 연결

`ServiceHandler`는 Service 계층에서 발생하는 이벤트를 Platform 계층으로 전달하는 브릿지입니다.

```java
ServiceHandler<D> handler = new ServiceHandler<>(
    plugin::handleDonation,             // 후원 이벤트 → BukkitDonationAPI
    player -> connectionManager.connect(  // 등록 성공 → PlatformConnectionManager
        player.uuid(),
        serviceHolder[0].getType(),
        player
    )
);
```

이 콜백 구조를 통해 Service는 Platform의 존재를 알 필요 없이, 순수한 Consumer를 통해 이벤트를 전달합니다.

---

## 라이브 상태 폴링

LiveStatusManager는 등록된 스트리머 채널의 라이브 상태를 주기적으로 체크하며 결과를 캐싱합니다.

1. `registerChecker(Services, LiveStatusChecker, intervalMs)` — 데몬 스레드 풀(2)에서 주기 실행 등록
2. `pollService(service, checker)` — 매 주기마다 실행되는 폴링 로직
   - PlatformRegistry에서 AbstractDonationPlatform 조회
   - 연결된 모든 플레이어의 `channelId` 순회
   - `LiveStatusChecker.checkLive(channelId)` 비동기 호출
   - 결과를 cache Map에 저장 (key: `"서비스:채널ID"`)
3. `getLiveStatus(Services, channelId)` — 캐싱된 결과 조회

### 라이브 상태 조회 예시

```java
import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.manager.LiveStatusManager;
import kr.rtustudio.donation.common.data.LiveStatus;
import kr.rtustudio.donation.service.Services;
import org.bukkit.entity.Player;

public class LiveCheckExample {

    public void checkStreamerLive(Player player) {
        LiveStatusManager manager = BukkitDonationAPI.getInstance().getLiveStatusManager();

        // 플레이어 UUID로 라이브 상태 조회 (channelId 자동 해석)
        LiveStatus status = manager.getLiveStatus(Services.Chzzk, player.getUniqueId());

        if (status != null && status.live()) {
            String title = status.title();           // 방송 제목
            int viewers  = status.viewerCount();     // 시청자 수
            String url   = status.channelUrl();      // 방송 URL

            player.sendMessage(
                String.format("§a방송 중: §f%s §7(시청자 %,d명)", title, viewers)
            );
        }
    }
}
```

**플랫폼별 LiveStatusChecker 구현체:**

| 구현체 | 동작 |
|--------|------|
| `ChzzkLiveChecker` | 치지직 OpenAPI로 방송 상태 확인 |
| `SoopLiveChecker` | 숲 API로 방송 상태 확인 |
| `CimeLiveChecker` | Cime HTTP API로 방송 상태 확인 |
| `YoutubeLiveChecker` | YouTube 라이브챗 존재 여부 확인 |

---

## 저장소 구조

DonationAPI는 RSFramework의 Storage API를 사용하여 데이터를 영구 저장합니다.

### 등록된 저장소

| 저장소 이름 | 용도 |
|------------|------|
| `User` | 플레이어 플랫폼 연결 상태 (PlatformStatusComponent) |
| `SSAPI` | SSAPI 연결 데이터 (SSAPIPlayer) |
| `CHZZK` | 치지직 연결 데이터 (ChzzkPlayer) |
| `SOOP` | 숲 연결 데이터 (SoopPlayer) |
| `Toonation` | 투네이션 연결 데이터 (ToonationPlayer) |
| `CIME` | 씨메 연결 데이터 (CimePlayer) |
| `Youtube` | 유튜브 연결 데이터 (YoutubePlayer) |

### 데이터 흐름

```
AbstractDonationPlatform.connect(uuid, data)
  → save(uuid, data)
    → Storage.get(JSON.of("uuid", uuid))
    → 존재 여부에 따라 Storage.add() 또는 Storage.set()

AbstractDonationPlatform.load(uuid)
  → Storage.get(JSON.of("uuid", uuid))
  → GSON으로 역직렬화
  → onReconnect() 호출하여 재연결 시도
```

---

## 설정 구조

```
plugins/DonationAPI/
├── Global.yml                    # 전역 설정 (알림 활성화 등)
└── Config/
    └── Services/
        ├── SSAPI.yml             # SSAPI 설정
        ├── Chzzk.yml             # 치지직 설정 (client-id, client-secret)
        ├── SOOP.yml              # 숲 설정
        ├── Cime.yml              # 씨메 설정
        ├── Toonation.yml         # 투네이션 설정
        └── Youtube.yml           # 유튜브 설정
```

모든 설정 클래스는 `ServiceBuilder.EnabledConfig`를 구현하여 `isEnabled()` 메서드를 제공합니다.  
비활성화된 플랫폼은 서비스 생성 자체가 생략됩니다.

---

## 새로운 플랫폼 추가 가이드

### 1. Service 모듈 생성

`Services/` 하위에 새 Gradle 모듈을 생성하고 `settings.gradle.kts`에 등록합니다.

```kotlin
// settings.gradle.kts
include("Services:NewPlatform")
```

### 2. 핵심 클래스 구현

```java
// 사용자 데이터
public record NewPlatformPlayer(UUID uuid, Platform platform, String channelId, String token) implements UserData { }

// 서비스 구현체
public class NewPlatformService extends AbstractService<NewPlatformPlayer> implements Disconnectable {
    
    public NewPlatformService(NewPlatformConfig config, ServiceHandler<NewPlatformPlayer> handler) {
        super(handler);
        // 초기화
    }

    @Override
    public Services getType() { return Services.NewPlatform; }

    @Override
    public void start() { /* 연결 시작 */ }

    @Override
    public void close() { /* 리소스 정리 */ }

    @Override
    public void disconnect(UUID uuid) { /* 플레이어 단위 해제 */ }
}
```

### 3. 열거형 등록

```java
// Services.java
NewPlatform("NewPlatform", Platform.NEW_PLATFORM);

// Platform.java
NEW_PLATFORM;
```

### 4. 설정 클래스 생성

```java
@SuppressWarnings({"unused", "CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal", "InnerClassMayBeStatic"})
public class NewPlatformConfig extends ConfigurationPart implements ServiceBuilder.EnabledConfig {
    private boolean enabled = false;
    private String clientId = "";
    private String clientSecret = "";
    // ...
}
```

### 5. ServiceBuilder 등록

`BukkitDonationAPI.setupServices()`에 등록 코드를 추가합니다.

```java
register(ServiceBuilder.builder()
    .config(NewPlatformConfig.class)
    .data(NewPlatformPlayer.class)
    .factory(NewPlatformService::new)
    .reconnect((service, data) -> service.reconnect(data.uuid(), data.token()))
    .build(this)
);
```

---

## 의존성 라이브러리

| 라이브러리 | 용도 |
|-----------|------|
| OkHttp 4.12 | HTTP 클라이언트 + WebSocket (커넥션 풀 공유) |
| Gson 2.13 | JSON 직렬화/역직렬화 |
| fastutil 8.5 | 고성능 컬렉션 (Platform 인덱싱 등) |
| Snappy 1.1 | 데이터 압축 (SOOP 바이너리 패킷) |
| Lombok 1.18 | 보일러플레이트 제거 |

> OkHttp, fastutil, Snappy는 런타임에 `loadLibrary()`를 통해 동적 로드됩니다.

---

<p align="center">
  <strong>Made with ❤️ by <a href="https://github.com/RTUStudio">RTU Studio</a></strong>
</p>
