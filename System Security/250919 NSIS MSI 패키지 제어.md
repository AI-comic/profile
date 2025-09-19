NSIS 스크립트에서 Windows Installer 패키지(.msi)를 제어하는 방법.

`Msiexec.exe`는 윈도우 운영체제에서 MSI 패키지의 설치, 제거, 수정 등을 담당하는 공식 도구.

이 기술은 스크립트를 통한 소프트웨어 배포 및 관리에 필수적인 요소다.

#### 핵심 기능

- **외부 프로그램 실행:** Exec 명령어를 사용하여 NSIS 스크립트 외부의 프로그램을 실행.
- **MSI 패키지 제어:** Msiexec.exe를 호출하여 MSI 기반 프로그램을 제거.
- **무음 모드:** /qr 옵션을 사용하여 사용자 인터페이스 없이 모든 과정을 자동으로 진행.

```nsis
SetCompressor LZMA
Name 'MsiexecExample'
OutFile 'MsiexecExample.exe'
SilentInstall silent

Section ""
    ; NSIS Exec 명령어를 사용한 외부 프로그램 실행 예제
    ; Msiexec.exe의 도움말 창을 띄웁니다.
    ; 이 명령어는 MSI 패키지 설치/제거에 사용됩니다.

    Exec '"$WINDIR\System32\Msiexec.exe" /?'

    ; 참고:
    ; /I{ProductCode}: 설치
    ; /X{ProductCode}: 제거
    ; /qr: UI 없이 자동 실행

SectionEnd
```
#### 코드 설명

- `SetCompressor LZMA`: 스크립트로 생성될 실행 파일의 크기를 줄이기 위해 압축을 설정.
- `Name 'MsiexecExample'`: 스크립트의 이름을 지정.
- `OutFile 'MsiexecExample.exe'`: 컴파일 후 생성될 실행 파일의 이름을 지정.
- `SilentInstall silent`: 설치 시 사용자에게 UI를 보여주지 않고 자동 진행되도록 설정.
- `Exec '"$WINDIR\System32\Msiexec.exe" /?'`:
  - `Exec` 명령어는 지정된 경로의 프로그램을 실행.
  - `"$WINDIR\System32\Msiexec.exe"`는 윈도우 시스템 폴더에 있는 `Msiexec.exe`의 경로를 가리킨다.
  - `/?`는 `Msiexec.exe`에게 도움말 창을 띄우라는 명령. 이를 통해 명령어의 다양한 옵션을 확인.

#### 사용 방법

- **스크립트 파일 생성:** 위 코드를 msiexec_example.nsi라는 이름으로 저장한다.
- **컴파일:** NSIS 컴파일러를 사용하여 해당 파일을 컴파일.
- **실행:** 생성된 MsiexecExample.exe를 실행하면 Msiexec.exe의 도움말 창이 나타난다.
