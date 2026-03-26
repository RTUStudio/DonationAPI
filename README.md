# DonationAPI

[![Version](https://img.shields.io/badge/version-1.0.3-blue.svg)](https://github.com/RTUStudio/DonationAPI)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1+-green.svg)](https://www.minecraft.net/)

**치지직, 숲, 투네이션 등 6개 이상의 후원 플랫폼을 하나의 API로 통합 관리하는 Bukkit 플러그인**

실시간 후원 알림, 다중 플랫폼 다중 채널 동시 연결, OkHttp 최적화를 통해 대규모 동시 접속 환경을 지원하는 엔터프라이즈급 후원 통합 시스템입니다.

> **의존성**: RSFramework (4.7.3 이상)

---

## 📋 목차

- [사용자 가이드](#-사용자-가이드)
  - [주요 기능](#-주요-기능)
  - [지원 플랫폼](#-지원-플랫폼)
  - [설치 및 설정](#-설치-및-설정)
  - [명령어 가이드](#-명령어-가이드)
  - [PlaceholderAPI](#️-placeholderapi)
- [개발자 가이드](#-개발자-가이드)
  - [의존성 추가](#-의존성-추가)
  - [DonationEvent 사용법](#-donationevent-사용법)
  - [아키텍처 구조](#-아키텍처-구조)
  - [빌드 방법](#-빌드-방법)

---

## 📚 사용자 가이드

### ✨ 주요 기능
- **통합 플랫폼 관리**: 단일 인스턴스로 복수의 스트리머, 복수의 플랫폼(치지직, 숲 등) 방송과 동시 연결 지원
- **실시간 후원 이벤트**: WebSocket 기반의 저지연 실시간 후원 수신
- **강력한 확장성**: 계층화된 `ServiceBuilder` 구조로 새로운 플랫폼 추가가 매우 용이함
- **독립 세션 및 로깅**: 각 채널별 독립 세션으로 연결 안정성을 보장하며, 문제 발생 시 플랫폼별 로깅 지원

### 🎯 지원 플랫폼

| 플랫폼 | 구분 | 연동 방식 | 지원 기능 |
|--------|------|-----------|-----------|
| **치지직 (CHZZK)** | **공식** | OpenAPI OAuth + WebSocket | 채팅, 별풍선(치즈), 구독 등 |
| **숲 (SOOP)** | **공식** | OpenAPI OAuth + 바이너리 웹소켓 | 채팅, 별풍선, 스티커, 애드벌룬 등 |
| **SSAPI** | **SSAPI** | Socket.io 서버 기반 통합 | 치지직, SOOP 통합 중계 |
| **투네이션 (Toonation)** | **비공식** | 페이로드 토큰 + 비동기 기반 연동 | 일반 후원, 룰렛, 미니게임 등 |
| **씨메 (Cime)** | **비공식** | 커스텀 HTTP API 주기적 확인 | 일반 후원, 포인트 연동 |
| **유튜브 (YouTube)** | **비공식** | 라이브챗 폴링 및 내부 처리 방식 | 슈퍼챗, 멤버십 등 |

### 🚀 설치 및 설정

#### 필수 요구사항
- **Minecraft**: 1.20.1 이상 (Paper/Folia 지원)
- **Java**: 21
- **RSFramework**: 4.7.3 이상

#### 설치 방법
1. 최신 릴리즈의 `DonationAPI-1.0.3.jar` 파일을 서버의 `plugins/` 폴더에 넣습니다.
2. 서버를 구동하면 플러그인이 로드되며 기본 설정 파일이 생성됩니다.
3. `plugins/DonationAPI/settings/services/` 폴더 안의 각 플랫폼 설정(예: `Chzzk.yml`, `SOOP.yml` 등)에 발급받은 `client-id`, `client-secret` 등을 입력합니다.
4. `/rtu reload` (또는 서버 재시작) 로 설정을 적용합니다.

### 📖 명령어 가이드
플러그인 기본 명령어 접두사는 `/donationapi` (별칭: `/후원API`) 입니다.

| 명령어 | 권한 | 설명 |
|--------|------|------|
| `/후원API 치지직 연동` | OP | 치지직 공식 API 연동 (OAuth 페이지 열림) |
| `/후원API 치지직 연동해제` | OP | 치지직 연결 해제 |
| `/후원API 숲 연동` | OP | 숲 공식 API 연동 (OAuth 페이지 열림) |
| `/후원API 숲 연동해제` | OP | 숲 연결 해제 |
| `/후원API 투네이션 연동 <토큰>` | OP | 투네이션 위젯 토큰으로 연동 |
| `/후원API SSAPI(플랫폼) 연동 <ID>` | OP | SSAPI 통합 연동 |
| `/후원API 이벤트 <닉네임> <가격> [메시지]`| OP | 테스트용 강제 후원 발생 |

### 🏷️ PlaceholderAPI
후원 연동 정보를 PAPI를 통해 확인할 수 있습니다.
- `%donationapi_chzzk%` : 치지직 연결 상태 (`true`/`false`)
- `%donationapi_soop%` : 숲 연결 상태 (`true`/`false`)
- `%donationapi_toonation%` : 투네이션 연결 상태
- `%donationapi_ssapi_chzzk%`, `%donationapi_ssapi_soop%` 등

---

## 🛠️ 개발자 가이드

### 📦 의존성 추가
다른 플러그인에서 DonationAPI를 사용하려면 빌드 도구에 저장소와 의존성을 추가하세요.

**Gradle (Kotlin DSL)**
```kotlin
repositories {
    maven {
        name = "RTUStudio"
        url = uri("https://repo.codemc.io/repository/rtustudio/")
    }
}

dependencies {
    compileOnly("kr.rtustudio:donationapi:1.0.3")
}
```

`plugin.yml` 에 의존성 추가:
```yaml
depend:
  - RSFramework
  - DonationAPI
```

### 🎯 DonationEvent 사용법

`DonationEvent`는 후원이 발생했을 때 호출되는 Bukkit 이벤트입니다.
어떤 플랫폼에서 접수된 후원이든 **통합된 `Donation` 객체** 포맷으로 전달되므로 플랫폼을 의식하지 않고 개발할 수 있습니다.

```java
import kr.rtustudio.donation.bukkit.event.DonationEvent;
import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DonationListener implements Listener {
    
    @EventHandler
    public void onDonation(DonationEvent event) {
        Donation donation = event.getDonation();
        
        // 채팅 이벤트는 무시하고 후원만 처리
        if (donation.type() == DonationType.CHAT) return;
        
        String platform = donation.platform().name(); // CHZZK, SOOP, TOONATION 등
        String nickname = donation.nickname();
        int amount = donation.amount();
        String message = donation.message() != null ? donation.message() : "";
        
        event.getPlayer().sendMessage(
            String.format("§a[%s] §e%s§f님이 §d%d§f원을 후원했습니다! §7(%s)", 
                          platform, nickname, amount, message)
        );
    }
}
```

### 🏗️ 아키텍처 구조

프로젝트는 강력한 모듈화 모델인 **Platform-Service** 패턴을 기반으로 설계되었습니다.

```text
DonationAPI/
├── Services/              # 프레임워크 독립적 코어 서비스
│   ├── Common/            # 통합 Donation 모델, Socket 인터페이스
│   ├── Chzzk/             # 치지직 OpenAPI 구현체
│   ├── SOOP/              # 숲 OpenAPI + 바이너리 패킷 해석기
│   ├── Toonation/         # 투네이션 웹소켓 구현체
│   ├── Cime/              # 씨메 구현체
│   ├── SSAPI/             # SSAPI 허브
│   └── Youtube/           # (예정)
│
└── Platform/
    ├── Common/            # 플랫폼 데이터(Config, Log) 어댑터
    └── Bukkit/            # Bukkit 통합 계층 (Event, Command, PAPI)
```

아키텍처의 상세 철학, `ServiceBuilder`를 활용한 의존성 주입 등은 [ARCHITECTURE.md](ARCHITECTURE.md)를 참고하세요.

### 🔨 빌드 방법

Windows:
```cmd
gradlew build shadowJar
```

macOS/Linux:
```bash
./gradlew build shadowJar
```

컴파일된 파일은 `builds/plugin/DonationAPI-1.0.3.jar` 에 생성됩니다.

---

**Made with ❤️ by RTU Studio**
