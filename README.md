# DonationAPI

[![Version](https://img.shields.io/badge/version-0.7.0-blue.svg)](https://github.com/RTUStudio/DonationAPI)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1+-green.svg)](https://www.minecraft.net/)

**치지직, 숲, 유튜브, 투네이션 등 다양한 후원 플랫폼을 하나의 API로 통합 관리하는 Bukkit/Paper 플러그인**

## 📋 목차

- [주요 기능](#-주요-기능)
- [지원 플랫폼](#-지원-플랫폼)

## ✨ 주요 기능

- **🎯 다중 플랫폼 지원**: 치지직, 숲(SOOP), 유튜브, 투네이션 등 여러 후원 플랫폼 통합
- **⚡ 실시간 후원 알림**: 후원이 발생하면 즉시 이벤트로 전달
- **🔌 PlaceholderAPI 연동**: 플레이어의 후원 상태를 플레이스홀더로 확인
- **🚀 유연한 확장성**: 새로운 플랫폼 추가가 용이한 구조
- **🎨 타입 안전성**: UserData 인터페이스 기반의 타입 안전한 설계
- **⚙️ GSON 직렬화**: UUID TypeAdapter 포함, 커스텀 TypeAdapter 추가 가능

## 🎯 지원 플랫폼

| 플랫폼 | 상태 | 설명 |
|--------|------|------|
| **SSAPI (치지직)** | ✅ 사용 가능 | SSAPI를 통한 치지직 후원 |
| **SSAPI (숲)** | ✅ 사용 가능 | SSAPI를 통한 숲 후원 |
| **치지직 공식** | ✅ 사용 가능 | OAuth 기반 공식 API |
| **치지직 비공식** | 🚧 개발 예정 | 비공식 API |
| **숲 공식** | 🚧 개발 예정 | 공식 API |
| **유튜브** | 🚧 개발 예정 | YouTube Live |
| **투네이션** | 🚧 개발 예정 | 투네이션 |

## 🚀 설치 방법

### 필수 요구사항

- **Minecraft**: 1.20.1 이상 (Paper 권장)
- **Java**: 21 이상
- **RSFramework**: 3.3.7 이상
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

#### 치지직 공식 설정 (`plugins/DonationAPI/Configs/Services/Chzzk.yml`)
```yaml
enabled: true
clientId: "your-client-id"
clientSecret: "your-client-secret"
baseUri: "http://localhost:12345"
host: "0.0.0.0"
port: 12345
```

## 📖 명령어 사용법

### SSAPI 연동

**치지직 연결**
```
/후원API ssapi-chzzk <스트리머ID>
```

**숲 연결**
```
/후원API ssapi-soop <스트리머ID>
```

**연결 해제**
```
/후원API ssapi-chzzk
/후원API ssapi-soop
```

### 치지직 공식 연동

**연결**
```
/후원API chzzk-official
```
- 웹 인증 페이지로 이동하여 인증 완료

**연결 해제**
```
/후원API chzzk-official
```

### 기타 플랫폼

```
/후원API chzzk-unofficial <스트리머ID>  # 치지직 비공식 (개발 예정)
/후원API soop                          # 숲 공식 (개발 예정)
/후원API youtube <주소>                # 유튜브 (개발 예정)
/후원API toonation <주소>              # 투네이션 (개발 예정)
```

## 📦 DonationEvent 사용법

`DonationEvent`는 후원이 발생했을 때 호출되는 Bukkit 이벤트입니다.
모든 후원 플랫폼의 후원 정보가 통합된 `Donation` 레코드로 전달됩니다.

### 🎯 기본 사용 예제법

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
        Services service = donation.service();      // SSAPI, ChzzkOfficial 등
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
    String id,              // 후원 고유 ID
    UUID uniqueId,          // 플레이어 UUID
    Services service,       // 서비스 (SSAPI, ChzzkOfficial 등)
    Platform platform,      // 플랫폼 (CHZZK, SOOP, YOUTUBE 등)
    DonationType type,      // 후원 타입 (CHAT, SUBSCRIPTION 등)
    String streamer,        // 스트리머 ID
    String donator,         // 후원자 ID
    String nickname,        // 후원자 닉네임
    String message,         // 후원 메시지
    int amount              // 후원 금액
) {}
```

## 🏷️ PlaceholderAPI

### 지원 Placeholder

| Placeholder | 설명 | 반환값 |
|-------------|------|--------|
| `%donationapi_chzzk_official%` | 치지직 공식 연결 및 후원 수신 여부 | `true` / `false` |
| `%donationapi_chzzk_unofficial%` | 치지직 비공식 연결 및 후원 수신 여부 | `true` / `false` |
| `%donationapi_ssapi_chzzk%` | SSAPI 치지직 연결 및 후원 수신 여부 | `true` / `false` |
| `%donationapi_ssapi_soop%` | SSAPI 숲 연결 및 후원 수신 여부 | `true` / `false` |
| `%donationapi_soop%` | 숲 공식 연결 및 후원 수신 여부 | `true` / `false` |
| `%donationapi_youtube%` | 유튜브 연결 및 후원 수신 여부 | `true` / `false` |
| `%donationapi_toonation%` | 투네이션 연결 및 후원 수신 여부 | `true` / `false` |

### 사용 예시

**조건부 보상 (DeluxeMenus)**
```yaml
item:
  material: DIAMOND
  display_name: "&a후원자 전용 보상"
  view_requirement: "%donationapi_chzzk_official% == true"
  click_commands:
    - "[console] give %player% diamond 64"
```

## 📚 고급 문서

프로젝트의 상세한 구조와 아키텍처에 대한 정보는 다음 문서를 참고하세요:

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - 프로젝트 구조, ECS 아키텍처, 플랫폼 추상화 등 상세 설명

## 🔨 빌드

```bash
./gradlew build -x test
```

빌드 결과물: `builds/DonationAPI-x.x.x.jar`

## 📧 문의

- **개발자**: IPECTER
- **조직**: RTU Studio
- **버전**: 0.7.0

---

**더 자세한 정보는 [ARCHITECTURE.md](ARCHITECTURE.md)를 참고하세요.**
