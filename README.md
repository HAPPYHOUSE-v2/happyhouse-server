# HAAPYHOUSE
공공 데이터를 활용한 주택 실거래정보 커뮤니티

http://43.201.50.190:8089

- 서비스 기간 : ~2024.8.7
- 관리자 계정 : admin@gmail.com (비밀번호 qwe123!@#)

## 💡 프로젝트 개요

- 이스트소프트 오르미 백엔드 개발자 양성과정 5기
- 주택거래 커뮤니티 서비스

## 📆 프로젝트 기간 & 팀원

- 2024년 7월 22일 ~ 2024년 8월 7일 (13일, 5 → 2명)
- 이상윤 : 커뮤니티 게시판  BE & FE, 테스트 및 배포
- 조성윤 : 회원가입, 로그인, 마이페이지, 관리자 BE&FE, 테스트 및 배포

## ⚙️ 기술 스택

<img src="https://img.shields.io/badge/JAVA-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=Spring&logoColor=white">
<img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=SpringBoot&logoColor=white">
<img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=Spring-Security&logoColor=white">
<img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white">
<img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
<img src="https://img.shields.io/badge/redis-%23DD0031.svg?&style=for-the-badge&logo=redis&logoColor=white">
</br>
<img src="https://img.shields.io/badge/html-E34F26?style=for-the-badge&logo=html5&logoColor=white">
<img src="https://img.shields.io/badge/javascript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black">
<img src="https://img.shields.io/badge/Thymeleaf-%23005C0F.svg?style=for-the-badge&logo=Thymeleaf&logoColor=white">
<img src="https://img.shields.io/badge/bootstrap-%23563D7C.svg?style=for-the-badge&logo=bootstrap&logoColor=white">
</br>
<img src="https://img.shields.io/badge/AWS_S3-569A31?style=for-the-badge&logo=amazon-aws&logoColor=white">
<img src="https://img.shields.io/badge/AWS_Lightsail-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white">


## ⚙️ 프로젝트 구성
![image](https://github.com/user-attachments/assets/aad13bd5-114b-4ce9-ad29-1074adbbfea4)


## ⚙️ ERD

![image](https://github.com/user-attachments/assets/0925c5d8-dc4c-45a5-b2b2-cfc8586bb490)


## 🖥 기능

### 회원관리
- **회원가입 & 이메일 인증**
   - JavaMailSender, Redis를 사용하여 이메일 인증해야 회원가입 가능
   - 닉네임 중복 확인 (Client fetchAPI로 비동기 통신)
     
     <img src="https://github.com/user-attachments/assets/9f809607-a109-493a-8d93-023259bb29b6" width="500" height="400" alt="회원가입 이미지">
     <img src="https://github.com/user-attachments/assets/6351bd2f-8a73-412e-80b8-56fe13c6c7e4" width="400" height="300" alt="이메일 인증 이미지">

- **로그인 & 비밀번호 초기화**
  - JWT Access & Refresh Token으로 인증 및 인가 처리(Refresh Token은 Redis에 저장)
  - Access Token 유효기간(15분)이 지나면 Refresh Token을 통해 Access Token 새로 발급
  - 비밀번호 초기화 버튼 클릭 시 입력한 이메일 주소로 초기화된 이메일 전송(마이페이지에서 수정 가능)
    
    <img src="https://github.com/user-attachments/assets/7920768b-3d45-4648-9d43-785eb78c7893" width="300" height="300" alt="로그인 이미지">
    <img src="https://github.com/user-attachments/assets/4613a16c-4b1b-499a-b2c7-ffb2c0518705" width="400" height="300" alt="비밀번호 초기화 이미지">

- **마이페이지 (개인정보 수정 & 회원 탈퇴)**
  - 닉네임, 비밀번호 수정
    - 닉네임 중복 확인
    -  비밀번호, 새 비밀번호 입력 시 검증
  - 회원 탈퇴
    - 비밀번호 입력 후 탈퇴 처리(status 값 변경, 해당 이메일로 다시 회원가입 불가)
  
  <img src="https://github.com/user-attachments/assets/2f55a230-6cc9-4f50-906d-fe5eb67e00c5" width="500" height="300" alt="개인정보 수정 이미지">
  <img src="https://github.com/user-attachments/assets/71438889-89c1-4349-9e15-52830f9d6920" width="500" height="300" alt="탈퇴 화면 이미지">

- **관리자 권한 인가 처리**
  - ADMIN 권한을 가진 User만 관리자 페이지 접속 가능

  <img src="https://github.com/user-attachments/assets/a51d62da-eba4-407a-90a8-489d009b90d3" width="500" height="300" alt="개인정보 수정 이미지">
  <img src="https://github.com/user-attachments/assets/4118e47c-d7c3-475a-98b7-058405d4cdfc" width="500" height="300" alt="탈퇴 화면 이미지">

- **커뮤니티 게시판**
   - **게시글 목록**
   
     <img src="https://github.com/user-attachments/assets/1f814a7d-c9ab-476b-8d43-b23d276a3e0d" width="700" alt="게시글 목록 이미지">

     
   - **검색 & 페이징**
  
     <img src="https://github.com/user-attachments/assets/595a6f0e-fe83-4850-8b0b-8ffa32a94df7" width="700" alt="게시글 검색 이미지">
     <img src="https://github.com/user-attachments/assets/557f8fce-dc44-4598-9338-25a317e4ce6d" width="700" alt="게시글 페이징 이미지">
 
   - **게시글 상세 조회**

     <img src="https://github.com/user-attachments/assets/0732ffe2-9aa4-42c1-93d9-a46dd86a3719" width="500" alt="게시글 상세 이미지">

 
   - **등록 & 수정 & 삭제**

     <img src="https://github.com/user-attachments/assets/4a93c38a-171f-4f73-b2df-bbd026fa80e6" width="500" alt="게시글 등록 이미지">
     <img src="https://github.com/user-attachments/assets/c2a06c97-4332-427e-9f93-806b6aa40103" width="500" alt="게시글 삭제 이미지">

 
   - **첨부파일 등록**
  
     <img src="https://github.com/user-attachments/assets/f292bec4-21d1-4195-a511-d9b7f6bb951d" width="500" alt="게시글 첨부 이미지">

 
   - **댓글 등록 & 삭제**

     <img src="https://github.com/user-attachments/assets/feaf07bc-d187-4894-b6b3-dabfd5a6af3b" width="500" alt="게시글 첨부 이미지">
