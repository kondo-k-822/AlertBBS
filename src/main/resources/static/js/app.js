document.getElementById('subscribe').addEventListener('click', async () => {
    try {
        const registration = await navigator.serviceWorker.register('/js/service-worker.js');
        console.log('Service Worker registered:', registration);

        // Service Workerがアクティブ化されるのを確認
        if (registration.waiting) {
            console.log('Service Worker is waiting');
            registration.waiting.postMessage({ type: 'SKIP_WAITING' });
        } else if (registration.installing) {
            console.log('Service Worker is installing');
            registration.installing.addEventListener('statechange', (event) => {
                if (event.target.state === 'installed') {
                    console.log('Service Worker installed');
                    event.target.postMessage({ type: 'SKIP_WAITING' });
                }
            });
        }

        // Service Workerが準備完了するのを待つ
        const readyRegistration = await new Promise((resolve, reject) => {
            if (navigator.serviceWorker.controller) {
                resolve(navigator.serviceWorker);
            } else {
                console.log('controllerchangeイベントを待っています...');
                navigator.serviceWorker.addEventListener('controllerchange', () => {
                    console.log('controllerchangeイベントがトリガーされました');
                    resolve(navigator.serviceWorker);
                });
            }
        });
        console.log('Service Worker is ready:', readyRegistration);

        const subscription = await readyRegistration.pushManager.subscribe({
            userVisibleOnly: true,
            applicationServerKey: urlBase64ToUint8Array(publicKey)
        });
        console.log('Push subscription:', subscription);

        const response = await fetch('/api/push/subscribe', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(subscription)
        });
        console.log('Subscription response:', response);
    } catch (error) {
        console.error('Service Workerの登録またはサブスクリプションに失敗しました:', error);
    }
});

function urlBase64ToUint8Array(base64String) {
    const padding = '='.repeat((4 - base64String.length % 4) % 4);
    const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);
    for (let i = 0; i < rawData.length; ++i) {
        outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
}