self.addEventListener('push', event => {
    let data;
    try {
        data = event.data.json();
    } catch (e) {
        console.error('Failed to parse push message data as JSON:', e);
        data = { title: 'Default Title', body: 'Default body text', icon: 'icon.png' };
    }
    self.registration.showNotification(data.title, {
        body: data.body,
        icon: data.icon
    });
});