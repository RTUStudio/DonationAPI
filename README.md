<p align="center">
  <h1 align="center">DonationAPI</h1>
  <p align="center">
    <strong>실시간 후원 플랫폼 통합 API for Minecraft</strong>
  </p>
  <p align="center">
    <a href="https://github.com/RTUStudio/DonationAPI"><img alt="Version" src="https://img.shields.io/badge/version-1.1.0-blue.svg"></a>
    <a href="https://openjdk.java.net/"><img alt="Java" src="https://img.shields.io/badge/Java-21-orange.svg"></a>
    <a href="https://www.minecraft.net/"><img alt="Minecraft" src="https://img.shields.io/badge/Minecraft-1.20.1+-green.svg"></a>
    <a href="https://www.gnu.org/licenses/gpl-3.0"><img alt="License" src="https://img.shields.io/badge/License-GPLv3-red.svg"></a>
  </p>
</p>

치지직, 숲, 투네이션 등 **6개 후원 플랫폼**을 하나의 통합 API로 관리하는 Bukkit 플러그인입니다.  
WebSocket 기반 실시간 후원 수신, 다중 플랫폼·다중 채널 동시 연결을 지원합니다.

> **필수 의존성**: [RSFramework](https://github.com/RTUStudio/RSFramework) 4.7.9 이상

---

## 📋 목차

<table>
<tr>
<td width="50%" valign="top">

### 사용자 가이드
- [지원 플랫폼](#-지원-플랫폼)
- [설치 및 설정](#-설치-및-설정)
- [명령어 가이드](#-명령어-가이드)
- [PlaceholderAPI 연동](#️-placeholderapi-연동)

</td>
<td width="50%" valign="top">

### 개발자 가이드
- [DonationEvent 수신](#-donationevent-수신)
- [Donation 객체 레퍼런스](#-donation-객체-레퍼런스)
- [라이브 상태 확인](#-라이브-상태-확인)
- [아키텍처](#-아키텍처)
- [ServiceBuilder 패턴](#-servicebuilder-패턴)
- [빌드 방법](#-빌드-방법)

</td>
</tr>
</table>

---

## 📚 사용자 가이드

### 🎯 지원 플랫폼

<table>
<tr>
<th>플랫폼</th>
<th>구분</th>
<th>연동 방식</th>
<th>지원 기능</th>
</tr>
<tr>
<td><strong>치지직 (CHZZK)</strong></td>
<td>🟢 공식</td>
<td>OpenAPI OAuth + WebSocket</td>
<td>채팅, 치즈, 구독</td>
</tr>
<tr>
<td><strong>숲 (SOOP)</strong></td>
<td>🟢 공식</td>
<td>OpenAPI OAuth + 바이너리 웹소켓</td>
<td>별풍선</td>
</tr>
<tr>
<td><strong>SSAPI</strong></td>
<td>🔵 통합</td>
<td>Socket.io 기반 통합 중계</td>
<td>치지직 + SOOP 통합</td>
</tr>
<tr>
<td><strong>투네이션 (Toonation)</strong></td>
<td>🟡 비공식</td>
<td>페이로드 토큰 + WebSocket</td>
<td>일반 후원</td>
</tr>
<tr>
<td><strong>씨미 (Cime)</strong></td>
<td>🟡 비공식</td>
<td>HTTP API 폴링</td>
<td>일반 후원</td>
</tr>
<tr>
<td><strong>유튜브 (YouTube)</strong></td>
<td>🟡 비공식</td>
<td>라이브챗 폴링</td>
<td>슈퍼챗</td>
</tr>
</table>

### 🚀 설치 및 설정

#### 필수 요구사항

| 항목 | 최소 버전 |
|------|----------|
| Minecraft | 1.20.1+ (Paper/Folia) |
| Java | 21 |
| RSFramework | 4.7.7+ |

#### 설치 방법

1. 최신 릴리즈에서 `DonationAPI-1.1.0.jar`를 다운로드합니다.
2. 서버의 `plugins/` 폴더에 JAR 파일을 배치합니다.
3. 서버를 시작하면 기본 설정 파일이 자동 생성됩니다.
4. `plugins/DonationAPI/Config/Services/` 하위의 플랫폼별 설정 파일을 편집합니다.
   - 각 플랫폼에서 발급받은 `client-id`, `client-secret` 등을 입력합니다.
5. `/후원API reload` 또는 서버 재시작으로 설정을 적용합니다.

### 📖 명령어 가이드

기본 명령어: `/donationapi` (별칭: `/후원API`)

#### 플랫폼 연동

| 명령어 | 설명 |
|--------|------|
| `/후원API 치지직 연동` | 치지직 OAuth 인증 링크 생성 |
| `/후원API 치지직 연동해제` | 치지직 연결 해제 |
| `/후원API 숲 연동` | 숲 OAuth 인증 링크 생성 |
| `/후원API 숲 연동해제` | 숲 연결 해제 |
| `/후원API 투네이션 연동 <토큰>` | 투네이션 위젯 토큰으로 연동 |
| `/후원API 투네이션 연동해제` | 투네이션 연결 해제 |
| `/후원API SSAPI치지직 연동 <ID>` | SSAPI 치지직 통합 연동 |
| `/후원API SSAPI숲 연동 <ID>` | SSAPI 숲 통합 연동 |

#### 관리

| 명령어 | 설명 |
|--------|------|
| `/후원API 이벤트 <닉네임> <금액> [메시지]` | 테스트용 후원 이벤트 발생 |
| `/후원API reload` | 전체 설정 및 서비스 재로드 |

> 모든 명령어는 OP 권한이 필요합니다.

### 🏷️ PlaceholderAPI 연동

[PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI)가 설치되면 자동으로 Placeholder가 등록됩니다.

**연결 상태:**

| Placeholder | 반환값 |
|-------------|--------|
| `%donationapi_chzzk%` | 치지직 연결 상태 (`true`/`false`) |
| `%donationapi_soop%` | 숲 연결 상태 |
| `%donationapi_toonation%` | 투네이션 연결 상태 |
| `%donationapi_cime%` | 씨미 연결 상태 |
| `%donationapi_youtube%` | 유튜브 연결 상태 |
| `%donationapi_ssapi_chzzk%` | SSAPI 치지직 연결 상태 |
| `%donationapi_ssapi_soop%` | SSAPI 숲 연결 상태 |

**라이브 상태:**

| Placeholder | 반환값 |
|-------------|--------|
| `%donationapi_live_chzzk%` | 치지직 방송 중 여부 |
| `%donationapi_live_soop%` | 숲 방송 중 여부 |
| `%donationapi_live_cime%` | 씨미 방송 중 여부 |
| `%donationapi_live_youtube%` | 유튜브 방송 중 여부 |
| `%donationapi_title_chzzk%` | 치지직 방송 제목 |
| `%donationapi_title_soop%` | 숲 방송 제목 |
| `%donationapi_title_cime%` | 씨미 방송 제목 |
| `%donationapi_viewers_chzzk%` | 치지직 시청자 수 |
| `%donationapi_viewers_soop%` | 숲 시청자 수 |
| `%donationapi_url_chzzk%` | 치지직 채널 URL |
| `%donationapi_url_soop%` | 숲 채널 URL |
| `%donationapi_url_cime%` | 씨미 채널 URL |
| `%donationapi_url_youtube%` | 유튜브 채널 URL |

---

## 🛠️ 개발자 가이드

### 📡 DonationEvent 수신

```java
import kr.rtustudio.donation.bukkit.event.DonationEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DonationListener implements Listener {

    @EventHandler
    public void onDonation(DonationEvent event) {
        String nickname = event.getNickname();
        int amount      = event.getAmount();     // count (플랫폼 단위)
        int price       = event.getPrice();      // 원 환산 금액
        String unit     = event.getUnit();        // 단위명 (치즈, 별풍선 등)
        String message  = event.getMessage();

        if (event.getPlayer() != null) {
            event.getPlayer().sendMessage(
                String.format("§e%s§f님이 §d%,d§f%s를 후원! §7%s",
                              nickname, amount, unit, message)
            );
        }
    }
}
```

**DonationEvent 주요 메서드:**

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `getPlayer()` | `@Nullable Player` | 후원 대상 플레이어 (오프라인이면 `null`) |
| `getDonation()` | `Donation` | 통합 후원 데이터 레코드 |
| `getService()` | `Services` | 서비스 유형 (`CHZZK`, `SOOP`, `SSAPI` 등) |
| `getPlatform()` | `Platform` | 실제 플랫폼 (`CHZZK`, `SOOP`, `TOONATION` 등) |
| `getNickname()` | `String` | 후원자 닉네임 |
| `getAmount()` | `int` | 후원 개수 (플랫폼 단위) |
| `getPrice()` | `int` | 원 환산 금액 (`count × rate`) |
| `getUnit()` | `String` | 플랫폼 단위명 (치즈, 별풍선, 원, 캐시, 빔) |
| `getMessage()` | `String` | 후원 메시지 |
| `isCancelled()` | `boolean` | 이벤트 취소 여부 |

### 📄 Donation 객체 레퍼런스

`Donation`은 Java `record`로 정의된 불변 데이터 객체입니다.

```java
public record Donation(
    @Nullable UUID uniqueId,     // 후원 대상 플레이어 UUID
    Services service,            // 서비스 유형 (CHZZK, SSAPI, SOOP ...)
    Platform platform,           // 실제 플랫폼 (CHZZK, SOOP, TOONATION ...)
    DonationType type,           // CHAT 또는 VIDEO
    String streamer,             // 스트리머 식별자
    String donator,              // 후원자 식별자
    String nickname,             // 후원자 닉네임 (기본값: "익명의 후원자")
    String message,              // 후원 메시지
    int amount                   // 후원 개수 (플랫폼 단위 기준)
) {
    public String unit()  { return platform.unit(); }          // 단위명
    public int    price() { return amount * platform.rate(); } // 원 환산 금액
}
```

**플랫폼별 단위 및 환산:**

| 플랫폼 | 단위 | rate | amount 예시 | price() |
|--------|------|------|-----------|---------|
| 치지직 | 치즈 | `1` | `1000` | `1,000원` |
| 숲 | 별풍선 | `100` | `5` | `500원` |
| 투네이션 | 캐시 | `1` | `5000` | `5,000원` |
| 씨미 | 빔 | `1` | `1000` | `1,000원` |
| 유튜브 | 원 | `1` | `1000` | `1,000원` |

> 단위와 환산 비율은 `GlobalConfig`에서 플랫폼별로 커스텀 설정이 가능합니다.

**Services vs Platform:**
- `Services` — 연동 서비스 자체 (예: `SSAPI`는 치지직과 숲 모두 지원)
- `Platform` — 실제 후원이 발생한 플랫폼

| Services | 지원 Platform |
|----------|--------------|
| `SSAPI` | `CHZZK`, `SOOP` |
| `CHZZK` | `CHZZK` |
| `SOOP` | `SOOP` |
| `Toonation` | `TOONATION` |
| `CIME` | `CIME` |
| `Youtube` | `YOUTUBE` |

### 📡 라이브 상태 확인

`LiveStatusManager`를 통해 연결된 스트리머의 방송 상태를 조회할 수 있습니다.

```java
import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.manager.LiveStatusManager;
import kr.rtustudio.donation.common.data.LiveStatus;
import kr.rtustudio.donation.service.Services;

public class LiveCheckExample {

    public void checkLive(Player player) {
        LiveStatusManager manager = BukkitDonationAPI.getInstance().getLiveStatusManager();
        LiveStatus status = manager.getLiveStatus(Services.CHZZK, player.getUniqueId());

        if (status != null && status.live()) {
            player.sendMessage(
                String.format("§a방송 중: §f%s §7(시청자 %,d명)",
                              status.title(), status.viewerCount())
            );
        }
    }
}
```

**LiveStatus 필드:**

| 필드 | 타입 | 설명 |
|------|------|------|
| `live()` | `boolean` | 방송 중 여부 |
| `title()` | `@Nullable String` | 방송 제목 |
| `viewerCount()` | `int` | 시청자 수 |
| `channelUrl()` | `String` | 방송 채널 URL |
| `updatedAt()` | `long` | 캐시 갱신 시각 (epoch ms) |

> 라이브 상태는 플랫폼별 설정의 `liveCheckInterval`에 지정된 주기로 자동 폴링됩니다.

### 🏗️ 아키텍처

프로젝트는 **Platform-Service 분리 패턴**을 기반으로, 프레임워크 의존 코드와 비즈니스 로직을 분리합니다.

```
DonationAPI/
├── Services/                    # 프레임워크 독립적 코어 서비스 (순수 Java)
│   ├── Common/                  # 통합 모델 (Donation, Platform, Service 인터페이스)
│   ├── CHZZK/                   # 치지직 OpenAPI + WebSocket
│   ├── SOOP/                    # 숲 OpenAPI + 바이너리 소켓
│   ├── SSAPI/                   # SSAPI Socket.io 통합
│   ├── Toonation/               # 투네이션 WebSocket
│   ├── CIME/                    # 씨미 HTTP 폴링
│   └── Youtube/                 # 유튜브 라이브챗 폴링
│
└── Platform/                    # 프레임워크 종속 어댑터
    ├── Common/                  # 플랫폼 공통 API (DonationAPI 레지스트리)
    └── Bukkit/                  # Bukkit 통합 계층
        ├── event/               #   DonationEvent
        ├── command/             #   MainCommand + 플랫폼별 서브커맨드
        ├── platform/            #   ServiceBuilder, PlatformRegistry
        ├── manager/             #   DonationManager, LiveStatusManager
        ├── configuration/       #   GlobalConfig + 플랫폼별 Config
        ├── integration/         #   PlaceholderAPI 연동
        └── handler/             #   PlayerJoinQuit 리스너
```

**핵심 계층:**

- **BukkitDonationAPI** — RSPlugin 메인 클래스
  - **PlatformRegistry** — DonationPlatform 관리
  - **DonationAPI** — Service 인스턴스 레지스트리
  - **ServiceBuilder** — 서비스 + 플랫폼 통합 생성 빌더
    - **DonationPlatform\<T\>** → **AbstractDonationPlatform\<T\>** (연결/해제)
    - **Service** → **AbstractService\<R\>** (라이프사이클)
      - **ServiceHandler\<R\>** — 콜백 (`donation`, `success`, `failure`, `messenger`)

> 상세 설계 및 플랫폼 추가 가이드는 [ARCHITECTURE.md](ARCHITECTURE.md)를 참고하세요.

### ⚙️ ServiceBuilder 패턴

새 후원 플랫폼을 추가할 때 `ServiceBuilder`로 **서비스 생성 → 핸들러 연결 → 플랫폼 등록**을 선언적으로 처리합니다.

```java
register(ServiceBuilder.builder()
    .config(ChzzkConfig.class)          // 설정 클래스 (EnabledConfig 구현)
    .data(ChzzkPlayer.class)            // 플레이어 데이터 타입
    .factory(ChzzkService::new)         // 서비스 팩토리
    .reconnect((service, data) ->       // 재연결 콜백 (선택)
        service.reconnect(data.uuid(), data.token()))
    .build(this)
);
```

**빌더 흐름:**

1. `builder()` → 공통 빌더 생성
2. `.config(Class)` → 설정 클래스 바인딩
3. `.data(Class)` → 데이터 타입 바인딩 → `TypedBuilder` 전환
4. `.factory(ServiceFactory)` → 서비스 인스턴스 팩토리
5. `.reconnect(ReconnectFunction)` → 재연결 로직 (선택)
6. `.build(plugin)` → `ServiceBuilder<D, S>` 완성

### 🔨 빌드 방법

```bash
# 빌드 (테스트 포함)
./gradlew build

# 빌드 (테스트 제외)
./gradlew build -x test
```

빌드 결과물: `build/libs/DonationAPI-1.1.0-all.jar`

### 📝 라이선스

이 프로젝트는 [GNU General Public License v3.0](LICENSE) 하에 배포됩니다.

---

<p align="center">
  <strong>Made with ❤️ by <a href="https://github.com/RTUStudio">RTU Studio</a></strong>
</p>
