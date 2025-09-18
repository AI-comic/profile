NSIS 스크립트에서 창(Window)과 컨트롤(Control)을 식별하고 제어하는 방법.

- 메모장(notepad.exe)을 실행하고 "파일" 메뉴의 Save As 대화상자에서 "취소" 버튼을 누르는 과정을 자동화.

이 기술은 NSIS를 사용하여 자동화된 설치/제거 스크립트를 작성할 때 유용하게 활용될 수 있다.

#### 핵심 기능
- 창 찾기 (FindWindow): 특정 제목이나 클래스명을 가진 윈도우를 찾아 핸들(고유 식별자)을 얻기.
- 컨트롤 찾기 (GetDlgItem): 특정 윈도우 내에서 ID를 통해 버튼, 체크박스 등 특정 컨트롤을 찾기.
- 메시지 전송 (SendMessage): 찾은 컨트롤에 클릭과 같은 동작 메시지를 보내어 사용자의 직접적인 상호작용 없이 컨트롤을 조작.
  
```nsis
!include WinMessages.nsh

SetCompressor LZMA
Name 'ControlExample'
OutFile 'ControlExample.exe'
SilentInstall silent

Section ""
    ; 1. 메모장 실행
    Exec '"$SYSDIR\notepad.exe"'
    
    ; 2. 메모장 창이 뜰 때까지 대기
    find_notepad_window:
        Sleep 1000
        FindWindow $0 'Notepad' ''
        IntCmp $0 0 find_notepad_window
    
    ; 3. '파일' 메뉴에 해당하는 키 조합 (ALT+F) 전송
    SendKeys "$0" "{ALT}{F}"

    ; 4. '다른 이름으로 저장'에 해당하는 키 조합 (S) 전송
    SendKeys "$0" "S"

    ; 5. '다른 이름으로 저장' 대화상자가 뜰 때까지 대기
    find_saveas_dialog:
        Sleep 1000
        FindWindow $1 '#32770' '다른 이름으로 저장'
        IntCmp $1 0 find_saveas_dialog
        
    ; 6. 대화상자에서 '취소' 버튼을 찾아 클릭 메시지 전송
    ; '취소' 버튼의 컨트롤 ID는 2입니다. (AutoIt 또는 Spy++로 확인 가능)
    GetDlgItem $2 $1 2 ; ID 2인 컨트롤(취소 버튼) 찾기
    SendMessage $2 ${BM_CLICK} 0 0 ; 버튼에 클릭 메시지 전송
    
    ; 7. 메모장 프로세스 종료
    ExecWait 'taskkill /F /IM notepad.exe'
    
SectionEnd
```
#### 코드 설명
- `!include WinMessages.nsh`: `BM_CLICK`와 같은 Windows 메시지 상수를 사용하기 위해 필요한 헤더 파일을 포함.
- `FindWindow $0 'Notepad' ''`: 제목이 'Notepad'인 창을 찾아 핸들을 `$0`에 저장.
- `IntCmp $0 0 find_notepad_window`: `$0` 값이 0(창을 찾지 못함)이면, `find_notepad_window` 라벨로 돌아가 다시 시도. 프로그램이 완전히 실행될 때까지 기다리는 역할.
- `SendKeys "$0" "{ALT}{F}"`: `$0` 핸들에 해당하는 창에 키 입력 이벤트를 보냄. `{ALT}{F}`는 'Alt + F' 키 조합으로, 메모장의 '파일' 메뉴를 엶.
- `FindWindow $1 '#32770' '다른 이름으로 저장'`: '다른 이름으로 저장' 대화상자를 찾아 핸들을 `$1`에 저장. `#32770`은 표준 대화상자의 클래스명.
- `GetDlgItem $2 $1 2`: `$1` 핸들이 가리키는 창에서 ID가 2인 컨트롤(이 경우 '취소' 버튼)을 찾아 핸들을 `$2`에 저장.
- `SendMessage $2 ${BM_CLICK} 0 0`: `$2` 핸들에 해당하는 컨트롤에 `BM_CLICK` 메시지를 보내어 프로그래밍 방식으로 클릭.

