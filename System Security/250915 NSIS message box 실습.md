NSIS(Nullsoft Scriptable Install System)를 이용해 MessageBox를 출력하는 다양한 예제.

NSIS는 윈도우 설치 프로그램을 제작할 때 많이 활용되며, 스크립트 기반으로 다양한 사용자 상호작용을 구현할 수 있다.

---

### 1. 기본 구조
```nsis
OutFile "file_name.exe"

Section "section_name" section_index_output

  Messagebox MB_OK|MB_ICONINFORMATION \
"This is a sample that shows how to use line breaks for larger commands in NSIS scripts"

SectionEnd
```
- OutFile : 생성될 실행 파일 이름 지정
- Section ~ SectionEnd : 설치 동작 정의 블록
- MessageBox : 메시지 창 출력

---

### 2. 설치 초기 확인 메시지
```nsis
OutFile "file_name.exe"

Function .onInit
  MessageBox MB_YESNO "This will install My Program. Do you wish to continue?" IDYES gogogo
    Abort
  gogogo:
FunctionEnd
 
Section ""
SectionEnd
```
- Function .onInit : 설치 시작 전에 실행됨
- Abort : 사용자가 "No"를 선택했을 경우 설치 중단

---

### 3. 단순 메시지 출력
```nsis
SetCompressor /SOLID LZMA
Name 'MessageBox'

OutFile "file_name.exe"

SilentInstall silent

Section ""

  Messagebox MB_OK|MB_ICONINFORMATION \
"안녕하세요. 저는 클라우드 서비스 보안 전공자 입니다!!!"

SectionEnd
```
- SilentInstall silent : 설치 UI를 숨기고 메시지 박스만 출력
 
- MB_ICONINFORMATION : 정보 아이콘 사용

---
### 4. 기본 메시지 박스
```nsis
Name "MessageBox"
OutFile "file_name.exe"
SilentInstall silent

Section ""
    MessageBox MB_OK "안녕하세요! 설치를 시작합니다."
    MessageBox MB_YESNO "계속하시겠습니까?"
SectionEnd
```

---
### 5. 다양한 버튼
```nsis
Name "MessageBox Examples"
OutFile "messagebox_examples.exe"
SilentInstall silent

Section "Basic Examples"
    ; 1. 기본 OK 버튼
    MessageBox MB_OK "기본 메시지 박스입니다."
    
    ; 2. Yes/No 선택
    MessageBox MB_YESNO "설치를 계속하시겠습니까?" IDYES yes IDNO no
    yes:
        DetailPrint "Yes를 선택했습니다."
        Goto continue
    no:
        DetailPrint "No를 선택했습니다."
        Goto continue
    continue:
    
    ; 3. OK/Cancel
    MessageBox MB_OKCANCEL "파일을 삭제하시겠습니까?" IDOK delete IDCANCEL skip
    delete:
        DetailPrint "삭제를 진행합니다."
        Goto next
    skip:
        DetailPrint "삭제를 취소했습니다."
    next:
    
    ; 4. Retry/Cancel
    MessageBox MB_RETRYCANCEL "연결에 실패했습니다. 다시 시도하시겠습니까?" IDRETRY retry IDCANCEL cancel
    retry:
        DetailPrint "다시 시도합니다."
        Goto end
    cancel:
        DetailPrint "취소했습니다."
    end:
    
SectionEnd
```
---
### 6. 아이콘 포함
```nsis
Unicode True
Name "MessageBox with Icons"
OutFile "messagebox_icons.exe"
SilentInstall silent

Section "Icon Examples"
    ; 정보 아이콘
    MessageBox MB_OK|MB_ICONINFORMATION "정보: 설치가 완료되었습니다."
    
    ; 경고 아이콘
    MessageBox MB_OK|MB_ICONEXCLAMATION "경고: 디스크 공간이 부족합니다."
    
    ; 오류 아이콘
    MessageBox MB_OK|MB_ICONSTOP "오류: 파일을 찾을 수 없습니다."
    
    ; 질문 아이콘
    MessageBox MB_YESNO|MB_ICONQUESTION "질문: 기존 파일을 덮어쓰시겠습니까?"
SectionEnd
```
---
### 7. 옵션 정리

- 버튼 타입
  - MB_OK: 확인 버튼만
  - MB_OKCANCEL: 확인/취소
  - MB_YESNO: 예/아니오
  - MB_YESNOCANCEL: 예/아니오/취소
  - MB_RETRYCANCEL: 다시 시도/취소
  - MB_ABORTRETRYIGNORE: 중단/다시 시도/무시

- 아이콘 타입
  - MB_ICONINFORMATION: 정보 아이콘 (i)
  - MB_ICONQUESTION: 질문 아이콘 (?)
  - MB_ICONEXCLAMATION: 경고 아이콘 (!)
  - MB_ICONSTOP: 오류 아이콘 (X)

- 특수 문자
  - $\n: 줄바꿈
  - $\t: 탭
  - $$: $ 문자 자체

- 반환값 처리
  - IDOK, IDCANCEL, IDYES, IDNO, IDRETRY, IDABORT, IDIGNORE

