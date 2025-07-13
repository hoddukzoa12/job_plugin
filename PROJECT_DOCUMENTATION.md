# Job Plugin 프로젝트 상세 문서

## 1. 프로젝트 개요

이 프로젝트는 Spigot/PaperMC API를 기반으로 하는 마인크래프트 서버용 "직업(Job)" 플러그인입니다. 플레이어는 '농부', '광부' 등 다양한 직업을 선택할 수 있으며, 각 직업에 맞는 활동(예: 작물 수확, 광물 채굴)을 통해 경험치(XP)를 얻고 레벨을 올릴 수 있습니다. 레벨이 오르면 해당 직업의 고유한 스킬을 습득하여 게임 플레이에 이점을 얻게 됩니다.

**핵심 기능:**
-   다양한 직업 시스템 제공
-   직업별 레벨 및 경험치 시스템
-   레벨에 따라 해금되는 고유 스킬
-   GUI를 통한 직업 선택 및 스킬 확인
-   플레이어 화면에 직업 정보를 표시하는 HUD
-   관리자가 쉽게 값을 수정할 수 있는 설정 파일(`config.yml`, `SkillConfig.yml` 등)

---

## 2. 핵심 개념

-   **직업 (Job)**: 플레이어가 선택하는 전문 분야입니다. `JobType` 열거형(Enum)으로 관리됩니다.
-   **경험치 (XP)**: 직업 관련 활동을 통해 얻는 포인트입니다. `PlayerDataManager`를 통해 플레이어별로 관리됩니다.
-   **레벨 (Level)**: 일정량의 경험치가 쌓이면 상승하며, 새로운 스킬을 해금하는 기준이 됩니다.
-   **스킬 (Skill)**: 특정 레벨에 도달하면 사용할 수 있는 직업 고유의 능력입니다. `SkillType`으로 정의되고 `SkillManager`가 로직을 처리합니다.
-   **GUI (Graphical User Interface)**: 플레이어가 인벤토리 화면을 통해 직업을 선택하거나 스킬 정보를 확인할 수 있는 인터페이스입니다.
-   **HUD (Heads-Up Display)**: 플레이어의 화면(주로 Action Bar)에 현재 직업, 레벨, 경험치 상태를 지속적으로 표시해주는 기능입니다.

---

## 3. 디렉토리 및 파일 구조

```
job_plugin/
├── src/main/java/org/job/job/  # 플러그인의 메인 소스 코드
│   ├── Job.java                 # 플러그인 진입점 (onEnable, onDisable)
│   ├── commands/                # 명령어 처리 클래스
│   │   ├── ChangeJobCommand.java
│   │   └── JobCommand.java
│   ├── config/                  # 설정 파일 로드 및 관리
│   │   ├── CropXPConfig.java
│   │   ├── RareDropConfig.java
│   │   └── SkillConfigManager.java
│   ├── cooking/                 # 요리 시스템 관련 클래스 (CookingPlugin 통합)
│   │   ├── command/
│   │   ├── event/
│   │   ├── gui/
│   │   ├── listener/
│   │   ├── recipe/
│   │   └── util/
│   │   └── CookingManager.java
│   ├── data/                    # 플레이어 데이터 관리
│   │   └── PlayerDataManager.java
│   ├── gui/                     # 인게임 GUI 관련 클래스
│   │   ├── JobSelectionGUI.java
│   │   └── SkillGUI.java
│   ├── handlers/                # 특정 직업의 액션 처리
│   │   └── FarmerActionHandler.java
│   ├── hud/                     # HUD(Heads-Up Display) 관리
│   │   └── HUDManager.java
│   ├── jobs/                    # 직업 종류 정의
│   │   └── JobType.java
│   ├── listeners/               # 게임 내 이벤트 리스너
│   │   ├── ItemProtectionListener.java
│   │   ├── JobGUIListener.java
│   │   ├── PlayerJoinListener.java
│   │   ├── PlayerQuitListener.java
│   │   ├── SkillGUIListener.java
│   │   └── farmer/              # 농부 직업 관련 리스너
│   │       ├── FarmerHarvestListener.java
│   │       ├── FarmerToolListener.java
│   │       └── HarvestMasteryListener.java
│   │       └── FarmerCookingListener.java
│   ├── skills/                  # 스킬 시스템 관련 클래스
│   │   ├── SkillManager.java
│   │   ├── SkillType.java
│   │   └── farmer/              # 농부 직업 스킬 관리
│   └── util/                    # 유틸리티 클래스
│       ├── CustomItemsCrops.java
│       └── ItemUtils.java
│
└── src/main/resources/          # 리소스 파일
    ├── config.yml               # 메인 설정 파일
    ├── plugin.yml               # 플러그인 정보 및 명령어 정의
    ├── SkillConfig.yml          # 스킬 기본 설정 파일
    └── recipes.yml              # 요리 레시피 정의 (CookingPlugin 통합)

---

## 4. 주요 클래스 분석

### `Job.java`
-   **역할**: 플러그인의 메인 클래스이자 진입점입니다. `JavaPlugin`을 상속받습니다.
-   **주요 메서드**:
    -   `onEnable()`: 플러그인 활성화 시 호출됩니다. 설정 파일을 로드하고, 명령어와 이벤트 리스너를 서버에 등록하며, 모든 관리자(Manager) 클래스를 초기화하는 역할을 수행합니다.
    -   `onDisable()`: 플러그인 비활성화 시 호출됩니다. 온라인 상태인 모든 플레이어의 데이터를 안전하게 저장하고, 스케줄러 등 실행 중인 작업을 모두 종료합니다.

### `data/PlayerDataManager.java`
-   **역할**: 플레이어의 직업, 레벨, 경험치, 스킬 습득 상태 등 모든 데이터를 관리하는 핵심 클래스입니다.
-   **주요 로직**:
    -   플레이어 접속 시(`PlayerJoinEvent`), 해당 플레이어의 UUID에 해당하는 `.yml` 파일을 찾아 데이터를 메모리로 로드합니다. 파일이 없으면 새로운 데이터 객체를 생성합니다.
    -   플레이어의 활동(예: 작물 수확)에 따라 경험치를 증가시키고 레벨업을 처리합니다.
    -   플레이어 퇴장 시(`PlayerQuitEvent`), 메모리에 있는 데이터를 다시 파일에 저장하여 영속성을 보장합니다.
    -   내부적으로 `Map<UUID, PlayerData>`와 같은 형태로 실시간 데이터를 관리할 가능성이 높습니다.

### `commands/JobCommand.java`
-   **역할**: `/job` 또는 `/직업` 명령어를 처리합니다.
-   **주요 로직**:
    -   명령어 실행자가 플레이어인지 확인합니다.
    -   `JobSelectionGUI`를 생성하여 플레이어에게 보여줍니다. 이를 통해 플레이어는 직업을 선택하거나 변경할 수 있습니다.

### `handlers/FarmerActionHandler.java`
-   **역할**: 농부 직업과 관련된 다양한 액션(예: 작물 수확, 도구 사용)을 처리하는 중앙 핸들러입니다.
-   **주요 로직**:
    -   `FarmerHarvestListener`와 같은 리스너로부터 특정 농부 관련 이벤트 발생 시 호출됩니다.
    -   경험치 지급, 스킬 발동 조건 확인 및 적용 등 농부 직업의 핵심 로직을 캡슐화합니다.

### `gui/JobSelectionGUI.java`
-   **역할**: 직업 선택을 위한 인벤토리 GUI를 생성하고 관리합니다.
-   **주요 로직**:
    -   `Bukkit.createInventory()`를 사용하여 가상 인벤토리를 생성합니다.
    -   `JobType`에 정의된 각 직업에 대한 아이콘(예: 농부는 밀, 광부는 철 곡괭이)을 인벤토리에 배치합니다. 아이템의 Lore에는 직업에 대한 설명이 포함됩니다.
    -   플레이어가 이 GUI에서 아이템을 클릭하는 이벤트는 `listeners/JobGUIListener.java`에서 처리됩니다.

### `listeners/farmer/FarmerHarvestListener.java`
-   **역할**: 농부 직업을 가진 플레이어의 작물 수확 이벤트를 감지하고 `FarmerActionHandler`에 처리를 위임합니다.
-   **주요 로직**:
    -   `@EventHandler` 어노테이션을 통해 `BlockBreakEvent`를 감지합니다.
    -   이벤트 발생 시, `FarmerActionHandler.handleHarvestAction()`을 호출하여 실제 수확 관련 로직(경험치 지급, 스킬 발동 등)을 처리하도록 합니다.

### `listeners/farmer/FarmerToolListener.java`
-   **역할**: 농부 직업을 가진 플레이어의 도구 사용 이벤트를 감지하고 처리합니다.
-   **주요 로직**:
    -   `@EventHandler` 어노테이션을 통해 `PlayerInteractEvent` 등을 감지합니다.
    -   농부 관련 도구(예: 괭이) 사용 시 특정 액션(예: 밭 갈기)에 대한 경험치 지급 또는 스킬 발동을 `FarmerActionHandler`에 위임합니다.

### `listeners/farmer/HarvestMasteryListener.java`
-   **역할**: 농부 직업의 '수확의 달인' 스킬 발동을 감지하고 처리합니다.
-   **주요 로직**:
    -   `@EventHandler` 어노테이션을 통해 `BlockBreakEvent` 등을 감지합니다.
    -   플레이어가 작물을 수확할 때 '수확의 달인' 스킬이 활성화되어 있고 발동 조건이 충족되면, 추가 아이템 드롭 등의 스킬 효과를 적용합니다.

### `skills/SkillManager.java`
-   **역할**: 모든 직업의 스킬을 총괄 관리합니다.
-   **주요 로직**:
    -   `SkillConfig.yml` 파일에서 각 스킬의 이름, 설명, 필요 레벨, 효과 등의 정보를 로드합니다.
    -   각 직업별 이벤트 리스너(`FarmerHarvestListener` 등)로부터 스킬 발동 요청을 받으면, 플레이어의 레벨과 조건을 확인한 후 실제 스킬 효과를 적용합니다.

---

## 5. 데이터 및 이벤트 흐름

### 플레이어 접속 시
1.  `PlayerJoinListener`가 `PlayerJoinEvent`를 감지합니다.
2.  `PlayerDataManager.loadPlayerData(player)`를 호출하여 해당 플레이어의 데이터를 파일에서 메모리로 로드합니다.
3.  `HUDManager.addPlayer(player)`를 호출하여 해당 플레이어의 HUD 업데이트 스케줄러를 시작합니다.

### 농부가 작물을 수확할 때
1.  플레이어가 작물을 파괴하면 `FarmerHarvestListener`가 `BlockBreakEvent`를 감지합니다.
2.  `FarmerHarvestListener`는 `FarmerActionHandler.handleHarvestAction(player, block)`을 호출하여 실제 수확 로직을 위임합니다.
3.  `FarmerActionHandler`는 플레이어의 직업이 `JobType.FARMER`인지 확인하고, `CropXPConfig`에서 해당 작물에 대한 경험치 값을 가져옵니다.
4.  `PlayerDataManager.addXP(player, amount)`를 호출하여 경험치를 추가합니다.
5.  `PlayerDataManager`는 경험치 추가 후 레벨업 조건을 확인하고, 충족 시 레벨을 올립니다.
6.  `HarvestMasteryListener`와 같은 스킬 리스너가 스킬 발동 조건을 확인하고 효과를 적용합니다.

### 플레이어 퇴장 시
1.  `PlayerQuitListener`가 `PlayerQuitEvent`를 감지합니다.
2.  `PlayerDataManager.savePlayerData(player)`를 호출하여 메모리에 있던 플레이어 데이터를 파일에 저장합니다.
3.  `HUDManager.removePlayer(player)`를 호출하여 HUD 스케줄러를 중지하고 메모리 누수를 방지합니다.

---

## 6. 설정 파일

### `plugin.yml`
-   **경로**: `src/main/resources/plugin.yml`
-   **역할**: Spigot/Paper 서버에 플러그인을 등록하기 위한 필수 설정 파일입니다.
-   **주요 항목**:
    -   `name`: 플러그인 이름 (e.g., `JobPlugin`)
    -   `version`: 플러그인 버전
    -   `main`: 메인 클래스의 전체 경로 (`org.job.job.Job`)
    -   `api-version`: 대상 서버 API 버전 (e.g., `1.19`)
    -   `commands`: 플러그인에서 사용하는 모든 명령어를 등록하고, 권한(permission)과 별칭(aliases)을 설정합니다.

### `SkillConfig.yml`
-   **경로**: `src/main/resources/SkillConfig.yml`
-   **역할**: 각 직업의 스킬에 대한 상세 설정을 정의합니다. 서버 관리자는 이 파일을 수정하여 스킬 밸런스를 조정할 수 있습니다.
-   **구조 예시**:
    ```yaml
    skills:
      FARMER:
        HARVEST_MASTERY: # 수확의 정석
          max-level: 7
          points-per-level: 1
          range: 3 # N*N 범위 (홀수만 가능, 예: 3은 3x3)
          duration-seconds: 8 # 스킬 지속 시간 (초)
          cooldown-seconds: 60 # 스킬 쿨타임 (초)
        RARE_CROP_CHANCE: # 특수 작물 확률 업
          max-level: 10
          points-per-level: 1
          # 레벨당 추가 드롭 확률 (기본 확률에 더해짐)
          chance-per-level: 0.005 # 0.5%
        COOKING_SUCCESS_RATE:
          max-level: 10
          points-per-level: 1
          # 레벨당 성공 확률 증가량
          success-rate-per-level: 0.01 # 1%
        COOKING_GREAT_SUCCESS:
          max-level: 10
          points-per-level: 2
          # 레벨당 대성공(+1) 확률
          great-success-chance-per-level: 0.005 # 0.5%
        COOKING_TIME_REDUCTION:
          max-level: 5
          points-per-level: 2
          # 레벨당 요리 시간 감소량 (비율)
          time-reduction-per-level: 0.05 # 5%
        AUTO_REPLANT: # 자동 심기
          level-required: 30
        AUTO_PICKUP: # 자동 줍기
          level-required: 60
    ```

---

## 7. 빌드 방법

이 프로젝트는 Gradle 빌드 시스템을 사용합니다. 프로젝트를 빌드하여 서버에 적용할 수 있는 `.jar` 파일을 생성하려면 다음 단계를 따르세요.

1.  프로젝트의 루트 디렉토리에서 터미널(명령 프롬프트 또는 PowerShell)을 엽니다.
2.  아래 명령어를 실행합니다.
    -   Windows: `gradlew.bat build`
    -   Linux/macOS: `./gradlew build`
3.  빌드가 성공적으로 완료되면, `build/libs/` 디렉토리 안에 플러그인 `.jar` 파일이 생성됩니다.
4.  이 `.jar` 파일을 마인크래프트 서버의 `plugins/` 폴더에 넣고 서버를 재시작하면 플러그인이 적용됩니다.
