function sendPushNotification(result) {
    Push.create('Push 通知だよ！', {
        body: '対象スレが落ちるのは ' + result
    });
}

function fetchAndNotify() {
    const url = embeddedUrl; // サーバーサイドからのURL値を使用
    fetch('/api/push/get-endtime', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
            'url': url
        })
    })
    .then(response => response.text())
    .then(data => {
        sendPushNotification(data);
    })
    .catch(error => console.error('Error fetching data:', error));
}

// 6000000ミリ秒（6000秒）ごとにfetchAndNotify関数を実行
setInterval(fetchAndNotify, 3600000);

// ページ読み込み時にfetchAndNotify関数を一度実行
document.addEventListener("DOMContentLoaded", function() {
    if (result !== 'defaultResult') {
        fetch('/AlertBBS/api/push/save-result', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ result: result, email: email, url: embeddedUrl })
        })
        .then(response => response.json())
        .then(data => console.log('Success:', data))
        .catch((error) => console.error('Error:', error));
    }
});