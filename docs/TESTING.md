# 테스트 가이드

메모 생성 → 위젯 반영 → 위젯 탭으로 해당 메모 열기(딥링크)까지 확인하는 방법입니다.

---

## iOS (실기기 권장, 위젯 데이터 공유 때문)

> ⚠️ 위젯이 앱 데이터를 읽으려면 **App Group**이 실제로 활성화돼야 합니다.
> 시뮬레이터에서 서명 없이 빌드하면 App Group 컨테이너가 동작하지 않아, 위젯에
> 데이터가 안 보일 수 있습니다. 아래 1번(서명 설정) 후 실기기에서 테스트하세요.

### 1. 서명 / App Group 설정 (최초 1회)
```bash
cd iosApp && xcodegen generate && open kmemo.xcodeproj
```
- `kmemo`, `MemoWidgetExtension` **두 타겟 모두**:
  - Signing & Capabilities → **Team** 선택
  - **+ Capability → App Groups** → `group.com.kmemo.app` 체크
- Bundle ID `com.kmemo`가 충돌하면 본인 고유값으로 변경 (README의 주의사항 참고).

### 2. 앱에서 메모 만들기
1. iPhone에 `kmemo` 실행 → 오른쪽 아래 **+** 탭.
2. 제목/내용 입력, 색상 선택, 필요하면 편집화면에서 저장(체크) 후 목록의 핀 아이콘으로 상단 고정.
3. 저장(체크 ✓) → 목록에 카드가 보이면 성공.

### 3. 홈 화면 위젯 추가
1. 홈 화면 빈 곳을 길게 눌러 편집 모드 → 좌상단 **+**.
2. "kmemo" 검색 → 소/중/대 중 원하는 크기 선택 → **위젯 추가**.
3. 위젯을 **원하는 자리로 드래그**해서 배치.
4. 앱에서 만든 메모가 위젯에 나타나는지 확인.
   (앱에서 메모를 바꾸면 위젯도 갱신됩니다. 즉시 반영이 안 되면 몇 초 대기 —
    WidgetKit이 타임라인을 새로고침합니다.)

### 4. 딥링크 (위젯 탭 → 해당 메모 열기)
- 위젯의 특정 메모를 탭 → 앱이 열리며 **그 메모의 편집화면**이 바로 뜹니다.
- 작은 위젯(systemSmall)은 첫 번째 메모로 연결됩니다.

---

## Android (에뮬레이터 또는 실기기)

### 1. 설치 / 실행
```bash
export JAVA_HOME=/Users/dohyunkim/Library/Java/JavaVirtualMachines/corretto-17.0.14/Contents/Home
./gradlew :composeApp:installDebug     # 연결된 기기/에뮬레이터에 설치
adb shell am start -n com.kmemo/.MainActivity
```

### 2. 메모 만들기 / 위젯 추가
- 앱에서 **+** 로 메모 생성 (iOS와 동일한 UI).
- 홈 화면 길게 누르기 → **위젯** → **kmemo** → 원하는 자리에 배치.

### 3. 딥링크 확인 (확인창 없이 바로 전달됨)
```bash
# 특정 메모(id) 열기
adb shell am start -a android.intent.action.VIEW -d "kmemo://memo/1"
```
→ 앱이 해당 메모 편집화면으로 바로 진입합니다. 위젯의 메모 카드를 탭해도 동일합니다.

### DB를 직접 들여다보기 (선택)
```bash
adb shell run-as com.kmemo sqlite3 databases/kmemo.db "SELECT id,title,pinned FROM MemoEntity;"
```

---

## 참고: 이 저장소에서 이미 검증된 것
- Android: `./gradlew :composeApp:assembleDebug` 빌드 성공.
- iOS: 시뮬레이터에서 앱 실행 + Compose UI 렌더링 + SQLite 메모 목록 표시 확인,
  `kmemo://` URL 스킴이 앱으로 라우팅됨을 확인.
- 위젯의 실제 데이터 표시는 App Group 서명이 필요하므로 실기기에서 확인하세요.
