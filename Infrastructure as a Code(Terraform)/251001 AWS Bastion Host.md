## 1. Bastion Host란?

Bastion Host는 **점프 서버(Jump Server)** 라고도 불리며, 네트워크 보안을 위해 특별히 설계된 관리 전용 서버입니다.  
공격에 대비해 최소한의 서비스만 실행되며, 관리자가 **프라이빗 서브넷에 위치한 인스턴스(Web, WAS 등)에 접근할 수 있는 관문** 역할을 합니다.

즉, Bastion Host는 **퍼블릭 네트워크와 프라이빗 네트워크 사이의 보안 게이트웨이**로 동작합니다.

---

## 2. 동작 방식

일반적으로 프라이빗 서브넷의 애플리케이션 서버는 보안상 이유로 **직접 인터넷에 노출되지 않습니다**.  
이때 Bastion Host를 퍼블릭 서브넷에 두어 관리자가 Bastion을 거쳐 프라이빗 서버에 접근할 수 있습니다.

흐름은 다음과 같습니다:

1. 관리자는 로컬 PC에서 **개인 키(private-key.pem)** 를 사용해 Bastion Host에 SSH 접속.
2. Bastion Host에 로그인한 뒤, 같은 개인 키를 이용해 프라이빗 서브넷의 애플리케이션 서버에 접근.
3. 애플리케이션 서버는 외부에서 직접 접속이 차단되므로 보안성이 강화됨.

---

## 3. 네트워크 구성 예시

- **Bastion Host**
  - Public Subnet에 위치
  - 퍼블릭 IP 보유
  - Bastion 전용 보안 그룹(Bastion-SG) 연결  
    - 인바운드: SSH(22번 포트) → 관리자 PC의 IP만 허용  

- **Application 서버**
  - Private Subnet에 위치
  - 퍼블릭 IP 없음
  - Application 보안 그룹(Application-SG) 연결  
    - 인바운드: SSH(22번 포트) → Bastion-SG만 허용  

---

## 4. 보안 그룹 설정

- **Bastion-SG**
  - Inbound: `22` 포트 ← **관리자 PC의 고정 IP**
  - Outbound: Private Subnet의 Application 서버로 SSH 허용  

- **Application-SG**
  - Inbound: `22` 포트 ← **Bastion-SG**에서만 허용
  - Outbound: 필요에 따라 제한  

---

## 5. 접근 절차

1. 로컬 PC에서 개인 키(`private-key.pem`)를 이용해 Bastion 서버 접속  
```bash
ssh -i private-key.pem ec2-user@<Bastion-Server-Public-IP>
```
2. Bastion 서버에 로그인 후, 개인 키를 ~/.ssh/ 경로에 저장하고 권한 설정
```bash
chmod 400 private-key.pem
```
3. Bastion 서버에서 Application 서버 접속
```bash
ssh -i private-key.pem ec2-user@<Application-Private-IP>
```

---

## 6. Bastion 서버 운영 시 주의사항

- **최소 권한 원칙**: 관리자 PC의 IP만 허용하고, 0.0.0.0/0 설정은 피해야 함.
- **로그 관리**: Bastion 서버에 접속한 기록을 반드시 모니터링해야 함.
- **자동화 도구 사용**: Terraform, Ansible 등을 활용해 접근 제어를 코드로 관리하면 운영 안정성이 향상됨.
- **다중 인증(MFA)**: SSH 키 외에 MFA를 추가하면 보안 강화 가능.
