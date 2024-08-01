let isEmailVerified = false;
function sendVerificationEmail() {
    var email = document.getElementById('email').value;
    fetch('/member/send-verification-email', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'email=' + encodeURIComponent(email)
    })
        .then(response => {
            if (response.ok) {
                alert('인증 메일이 발송되었습니다. 이메일을 확인해주세요.');
                document.getElementById('verificationCodeSection').style.display = 'block';
            } else {
                throw new Error('인증 메일 발송에 실패했습니다.');
            }
        })
        .catch(error => {
            alert(error.message);
        });
}

function verifyEmail() {
    var email = document.getElementById('email').value;
    var code = document.getElementById('verificationCode').value;
    var emailResultSpan = document.getElementById('emailResult');
    console.log("인증코드", code);
    fetch('/member/verify-email', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({email: email, code: code})
    })
        .then(response => {
            if (response.ok) {
                return response.text();
            } else {
                throw new Error('이메일 인증에 실패했습니다.');
            }
        })
        .then(data => {
            console.log(data);  // 서버 응답 로그
            alert('이메일이 성공적으로 인증되었습니다.');
            isEmailVerified = true;
            emailResultSpan.textContent = '이메일 인증 완료됐습니다.';
            emailResultSpan.style.color = 'green';
        })
        .catch(error => {
            alert(error.message);
            isEmailVerified = false;
            emailResultSpan.textContent = '이메일 인증 실패했습니다.';
            emailResultSpan.style.color = 'red';
        });
}
function checkNickname() {
    var nickname = document.getElementById('nickname').value;
    var resultSpan = document.getElementById('nicknameResult');

    fetch(`/member/duplicateNickname?nickname=${encodeURIComponent(nickname)}`)
        .then(response => {
            if (response.ok) {
                resultSpan.textContent = '사용 가능한 닉네임입니다.';
                resultSpan.style.color = 'green';
            } else if (response.status === 409) {
                resultSpan.textContent = '이미 사용 중인 닉네임입니다.';
                resultSpan.style.color = 'red';
            } else {
                throw new Error('닉네임 확인 중 오류가 발생했습니다.');
            }
        })
        .catch(error => {
            resultSpan.textContent = error.message;
            resultSpan.style.color = 'red';
            console.error('Error:', error);
        });
}
//비밀번호 유효성 검사
function checkPasswordStrength(password) {
    const minLength = 8;
    const minLength2Types = 10;
    const regex = {
        lowerCase: /[a-z]/,
        upperCase: /[A-Z]/,
        numbers: /[0-9]/,
        special: /[^A-Za-z0-9]/
    };

    let typesCount = 0;
    Object.values(regex).forEach(re => {
        if (re.test(password)) typesCount++;
    });

    if ((password.length >= minLength && typesCount >= 3) ||
        (password.length >= minLength2Types && typesCount >= 2)) {
        return true;
    }
    return false;
}
document.getElementById('password').addEventListener('input', function() {
    const password = this.value;
    const strengthDisplay = document.getElementById('passwordStrength');

    if (checkPasswordStrength(password)) {
        strengthDisplay.textContent = '비밀번호 규칙을 만족합니다.';
        strengthDisplay.style.color = 'green';
    } else {
        strengthDisplay.textContent = '비밀번호 규칙을 만족하지 않습니다.';
        strengthDisplay.style.color = 'red';
    }
});

//회원가입 시 이메일 인증, 닉네임 중복, 비밀번호 유효성 검사
document.querySelector('form').onsubmit = function(e) {
    var resultSpan = document.getElementById('nicknameResult');
    if (resultSpan.textContent !== '사용 가능한 닉네임입니다.') {
        e.preventDefault();
        alert('닉네임 중복 여부를 확인해주세요.');
    }
    if (!isEmailVerified) {
        e.preventDefault();
        alert('이메일 인증을 완료해주세요.');
        return;
    }
    if (!checkPasswordStrength(document.getElementById('password').value)) {
        e.preventDefault();
        alert('비밀번호 규칙을 만족하지 않습니다.');
        return;
    }
};