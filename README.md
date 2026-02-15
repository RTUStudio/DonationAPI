# DonationAPI

[![Version](https://img.shields.io/badge/version-0.18.0-blue.svg)](https://github.com/RTUStudio/DonationAPI)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1+-green.svg)](https://www.minecraft.net/)

**치지직, 숲 등 다양한 후원 플랫폼을 하나의 API로 통합 관리하는 Bukkit/Paper 플러그인**

실시간 후원 알림, 다중 플랫폼 지원, 1000명 이상 동시 연결을 지원하는 엔터프라이즈급 후원 통합 시스템입니다.

## 📋 목차

- [주요 기능](#-주요-기능)
- [지원 플랫폼](#-지원-플랫폼)
- [설치 방법](#-설치-방법)
- [명령어](#-명령어)
- [DonationEvent 사용법](#-donationevent-사용법)
- [PlaceholderAPI](#️-placeholderapi)
- [아키텍처](#-아키텍처)
- [빌드](#-빌드)

## ✨ 주요 기능

### 🎯 핵심 기능
- **다중 플랫폼 통합**: 치지직, 숲(SOOP), SSAPI를 단일 API로 통합 관리
- **실시간 후원 수신**: WebSocket 기반 실시간 후원 이벤트 처리
- **무제한 확장성**: OkHttp Dispatcher 최적화로 1000명 이상 동시 연결 지원
- **타입 안전 설계**: UserData 인터페이스 기반 타입 안전성 보장

### 🏗️ 아키텍처
- **ServiceBuilder 패턴**: 선언적 서비스 등록으로 보일러플레이트 최소화
- **ServiceHandler**: 후원/성공/실패 핸들러를 하나의 객체로 캡슐화
- **독립 세션 관리**: 플레이어별 독립 WebSocket 세션으로 안정성 보장
- **플랫폼 추상화**: DonationPlatform 인터페이스로 일관된 API 제공

### 🔌 통합 기능
- **PlaceholderAPI**: 후원 상태를 플레이스홀더로 조회
- **이벤트 시스템**: DonationEvent를 통한 유연한 후원 처리
- **자동 재연결**: 플레이어 재접속 시 자동으로 후원 연결 복구

## 🎯 지원 플랫폼

| 플랫폼 | 상태 | 연결 방식 | 특징 |
|--------|------|-----------|------|
| **SSAPI** | ✅ 사용 가능 | Socket.IO (단일 연결) | 치지직/숲 통합, 서버 중심 라우팅 |
| **치지직** | ✅ 사용 가능 | OAuth + WebSocket | 채널별 독립 세션, 1000명+ 지원 |
| **숲 (SOOP)** | ✅ 사용 가능 | OAuth + WebSocket (바이너리) | BJ별 독립 세션, Chat SDK 역분석 구현 |
| **유튜브** | 🚧 개발 예정 | - | YouTube Live |
| **투네이션** | 🚧 개발 예정 | - | 투네이션 |

### 📊 성능 특징

**64명 제한 해결:**
- **치지직/숲**: OkHttp Dispatcher 설정 (`maxRequests=1000`) 으로 1000명 동시 연결 지원
- **SSAPI**: 단일 WebSocket 연결로 무제한 사용자 지원
- 각 플레이어마다 독립적인 세션 유지로 안정성 보장

## 🚀 설치 방법

### 필수 요구사항

- **Minecraft**: 1.20.1 이상 (Paper 권장)
- **Java**: 21 이상
- **RSFramework**: 3.3.9 이상
- **PlaceholderAPI**: (선택) Placeholder 기능 사용 시

### 설치 단계

1. `DonationAPI-x.x.x.jar` 파일을 `plugins/` 폴더에 복사
2. 서버 시작 (설정 파일 자동 생성)
3. 서버 종료 후 설정 파일 수정
4. 서버 재시작

### 설정 파일

#### SSAPI 설정 (`plugins/DonationAPI/Configs/Services/SSAPI.yml`)
```yaml
enabled: true
apiKey: "your-ssapi-key"
```

#### 치지직 설정 (`plugins/DonationAPI/Configs/Services/Chzzk.yml`)
```yaml
enabled: true
clientId: "your-client-id"
clientSecret: "your-client-secret"
baseUri: "http://localhost:12345"
host: "0.0.0.0"
port: 12345
```

#### 숲 설정 (`plugins/DonationAPI/Configs/Services/SOOP.yml`)
```yaml
enabled: true
clientId: "your-client-id"
clientSecret: "your-client-secret"
baseUri: "http://localhost:12346"
host: "0.0.0.0"
port: 12346
```

## 📖 명령어

### 기본 명령어

| 명령어 | 설명 |
|--------|------|
| `/후원API SSAPI(치지직) 연동 <스트리머ID>` | SSAPI를 사용하여 치지직 스트리머와 연동 |
| `/후원API SSAPI(치지직) 연동해제` | SSAPI 치지직 연동 해제 |
| `/후원API SSAPI(숲) 연동 <스트리머ID>` | SSAPI를 사용하여 숲 스트리머와 연동 |
| `/후원API SSAPI(숲) 연동해제` | SSAPI 숲 연동 해제 |
| `/후원API 치지직 연동` | 치지직 공식 API 연동 (OAuth 인증 페이지 열림) |
| `/후원API 치지직 연동해제` | 치지직 공식 API 연동 해제 |
| `/후원API 숲 연동` | 숲 공식 API 연동 (OAuth 인증 페이지 열림) |
| `/후원API 숲 연동해제` | 숲 연동 해제 |
| `/후원API 이벤트 <플레이어> <가격> <내용>` | 후원 이벤트 강제 발생 (테스트용) |

### OAuth 인증 흐름

**치지직/숲 공식 API 사용 시:**
1. `/후원API 치지직 연동` 또는 `/후원API 숲 연동` 입력
2. 채팅창에 OAuth 인증 링크 표시
3. 링크 클릭하여 브라우저에서 인증
4. 인증 완료 후 자동으로 연동 완료
5. 서버 재접속 시 자동으로 재연결

## 📦 DonationEvent 사용법

`DonationEvent`는 후원이 발생했을 때 호출되는 Bukkit 이벤트입니다.
모든 후원 플랫폼의 후원 정보가 통합된 `Donation` 레코드로 전달됩니다.

### 🎯 기본 사용 예제

후원이 들어오면 `DonationEvent`가 자동으로 발생합니다. 이 이벤트를 리스닝하여 원하는 기능을 구현할 수 있습니다.

```java
import kr.rtustudio.donation.bukkit.event.DonationEvent;
import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.common.Platform;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MyDonationListener implements Listener {
    
    @EventHandler
    public void onDonation(DonationEvent event) {
        // 후원 정보 가져오기
        Donation donation = event.getDonation();
        Player player = event.getPlayer();
        
        // 후원자 정보
        String nickname = donation.nickname();      // 후원자 닉네임
        int amount = donation.amount();             // 후원 금액
        String message = donation.message();        // 후원 메시지
        
        // 플랫폼 정보
        Services service = donation.service();      // SSAPI, Chzzk, SOOP 등
        Platform platform = donation.platform();    // CHZZK, SOOP 등
        
        // 플레이어가 온라인인 경우에만 처리
        if (player != null && player.isOnline()) {
            player.sendMessage("§a" + nickname + "님이 " + amount + "원을 후원했습니다!");
            player.sendMessage("§7메시지: " + message);
        }
    }
}
```

### 💰 금액별 보상 지급

```java
@EventHandler
public void onDonation(DonationEvent event) {
    Donation donation = event.getDonation();
    Player player = event.getPlayer();
    
    if (player == null || !player.isOnline()) return;
    
    int amount = donation.amount();
    
    // 금액별 보상
    if (amount >= 10000) {
        player.sendMessage("§6★ 1만원 이상 후원! 특별 보상!");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
            "give " + player.getName() + " diamond 64");
    } else if (amount >= 5000) {
        player.sendMessage("§e★ 5천원 이상 후원! 보상!");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
            "give " + player.getName() + " gold_ingot 32");
    } else {
        player.sendMessage("§a후원 감사합니다!");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
            "give " + player.getName() + " iron_ingot 16");
    }
}
```

### Donation 데이터 구조

```java
public record Donation(
    UUID uniqueId,          // 플레이어 UUID
    Services service,       // 서비스 (SSAPI, Chzzk, SOOP 등)
    Platform platform,      // 플랫폼 (CHZZK, SOOP 등)
    DonationType type,      // 후원 타입 (CHAT, SUBSCRIPTION 등)
    String streamerId,      // 스트리머 ID
    String userId,          // 후원자 ID
    String nickname,        // 후원자 닉네임
    String message,         // 후원 메시지
    int amount              // 후원 금액
) {}
```

**필드 설명:**
- `uniqueId`: 후원을 받는 플레이어의 UUID
- `service`: 서비스 종류 (SSAPI, Chzzk, SOOP)
- `platform`: 플랫폼 종류 (CHZZK, SOOP)
- `type`: 후원 타입 (CHAT, SUBSCRIPTION 등)
- `streamerId`: 스트리머 채널 ID
- `userId`: 후원자 사용자 ID
- `nickname`: 후원자 닉네임 (기본값: "익명의 후원자")
- `message`: 후원 메시지 (기본값: "")
- `amount`: 후원 금액
```

## 🏷️ PlaceholderAPI

### 지원 Placeholder

| Placeholder | 설명 | 반환값 |
|-------------|------|--------|
| `%donationapi_chzzk%` | 치지직 연결 및 후원 수신 여부 | `true` / `false` |
| `%donationapi_soop%` | 숲 연결 및 후원 수신 여부 | `true` / `false` |
| `%donationapi_ssapi_chzzk%` | SSAPI 치지직 연결 및 후원 수신 여부 | `true` / `false` |
| `%donationapi_ssapi_soop%` | SSAPI 숲 연결 및 후원 수신 여부 | `true` / `false` |
| `%donationapi_youtube%` | 유튜브 연결 및 후원 수신 여부 | `true` / `false` |
| `%donationapi_toonation%` | 투네이션 연결 및 후원 수신 여부 | `true` / `false` |

### 사용 예시

**조건부 보상 (DeluxeMenus)**
```yaml
item:
  material: DIAMOND
  display_name: "&a후원자 전용 보상"
  view_requirement: "%donationapi_chzzk% == true"
  click_commands:
    - "[console] give %player% diamond 64"
```

## 🏗️ 아키텍처

### 핵심 설계 패턴

#### ServiceBuilder 패턴
서비스 등록을 선언적으로 처리하여 보일러플레이트 코드를 최소화합니다.

```java
// BukkitDonationAPI.java
register(ServiceBuilder.builder()
    .config(ChzzkConfig.class)
    .data(ChzzkPlayer.class)
    .factory(ChzzkService::new)
    .reconnect((service, data) -> service.reconnect(data.uuid(), data.token()))
    .build(this)
);
```

#### ServiceHandler
후원/성공/실패 핸들러를 하나의 객체로 캡슐화합니다.

```java
public record ServiceHandler<R>(
    Consumer<Donation> donation,    // 후원 수신 핸들러
    Consumer<R> success,             // 연결 성공 핸들러
    Consumer<UUID> failure           // 연결 실패 핸들러
) {}
```

#### 독립 세션 관리
각 플레이어마다 독립적인 WebSocket 세션을 유지하여 안정성을 보장합니다.

```java
// ChzzkSubscriber.java
private final Map<String, Chzzk> activeSessions = new ConcurrentHashMap<>();

// 각 채널마다 독립적인 Chzzk 인스턴스 생성
activeSessions.put(channelId, chzzk);
```

### 64명 제한 해결

**문제:** OkHttp의 기본 Dispatcher 설정은 `maxRequests=64`로 제한되어 있습니다.

**해결:**
```java
// SessionSocketImpl.java (Chzzk)
static {
    Dispatcher dispatcher = new Dispatcher();
    dispatcher.setMaxRequests(1000);      // 64 → 1000
    dispatcher.setMaxRequestsPerHost(1000); // 5 → 1000
    SHARED_HTTP_CLIENT = new OkHttpClient.Builder()
        .dispatcher(dispatcher)
        .build();
}
```

**결과:**
- **치지직**: 1000명 동시 연결 지원
- **숲**: 1000명 동시 연결 지원
- **SSAPI**: 단일 연결로 무제한 지원

### 프로젝트 구조

```
DonationAPI/
├── Services/              # 플랫폼 독립적 서비스 (순수 Java)
│   ├── Common/           # 공통 데이터 모델
│   ├── Chzzk/            # 치지직 공식 API
│   ├── SOOP/             # 숲 공식 API
│   └── SSAPI/            # SSAPI 통합
│
└── Platform/
    ├── Common/           # 플랫폼 공통 인터페이스
    └── Bukkit/           # Bukkit 플러그인 구현
        ├── platform/     # ServiceBuilder, DonationPlatform
        ├── command/      # 명령어 시스템
        ├── event/        # DonationEvent
        └── integration/  # PlaceholderAPI
```

## 🔨 빌드

```bash
./gradlew build -x test
```

빌드 결과물: `builds/DonationAPI-x.x.x.jar`

## 📚 문서

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - 상세한 아키텍처 설명, SOOP WebSocket 프로토콜, 설계 패턴

## 📧 문의

- **개발자**: IPECTER
- **조직**: RTU Studio
- **버전**: 0.18.0

---

**Made with ❤️ by RTU Studio**
