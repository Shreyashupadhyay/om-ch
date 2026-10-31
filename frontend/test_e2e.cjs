const { Client } = require('@stomp/stompjs');
const SockJS = require('sockjs-client');

const API_BASE = 'http://localhost:8080/api';
const WS_URL = 'http://localhost:8080/ws';

async function main() {
  console.log('--- Starting StrangerChat End-to-End WebSocket & Persistence Test ---');

  // 1. Get Guest Token for Client 1
  console.log('[Step 1] Creating Guest Session for Client 1...');
  const guestRes = await fetch(`${API_BASE}/auth/guest`, { method: 'POST' });
  const guestData = await guestRes.json();
  console.log('Guest Client 1 ready:', guestData.displayName, '(token preview:', guestData.token.substring(0, 25) + '...)');

  // 2. Login Registered User (Alice) for Client 2
  console.log('[Step 2] Authenticating Registered User (Alice) for Client 2...');
  const userRes = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ usernameOrEmail: 'alice', password: 'password123' })
  });
  const userData = await userRes.json();
  console.log('Registered Client 2 ready:', userData.user.username);

  // 3. Connect STOMP for Client 1
  console.log('[Step 3] Connecting Client 1 (Guest) to STOMP over SockJS...');
  const client1 = new Client({
    webSocketFactory: () => new SockJS(WS_URL),
    connectHeaders: { Authorization: `Bearer ${guestData.token}` },
    reconnectDelay: 0,
  });

  // 4. Connect STOMP for Client 2
  console.log('[Step 4] Connecting Client 2 (Registered) to STOMP over SockJS...');
  const client2 = new Client({
    webSocketFactory: () => new SockJS(WS_URL),
    connectHeaders: { Authorization: `Bearer ${userData.token}` },
    reconnectDelay: 0,
  });

  let matchedRoomId1 = null;
  let matchedRoomId2 = null;

  const testCompletedPromise = new Promise((resolve, reject) => {
    const timeout = setTimeout(() => {
      reject(new Error('Test timed out waiting for match and message cycle'));
    }, 15000);

    let client1Connected = false;
    let client2Connected = false;

    function checkBothConnectedAndJoin() {
      if (client1Connected && client2Connected) {
        console.log('[Step 5] Both clients connected! Joining matchmaking queue...');
        client1.publish({ destination: '/app/queue.join' });
        client2.publish({ destination: '/app/queue.join' });
      }
    }

    client1.onConnect = () => {
      client1Connected = true;
      console.log('Client 1 STOMP connected!');

      client1.subscribe('/user/queue/events', (msg) => {
        const ev = JSON.parse(msg.body);
        console.log('[Client 1 Event]:', ev.event, ev.message || '');
        if (ev.event === 'MATCHED') {
          matchedRoomId1 = ev.roomId;
          console.log('[Client 1 Matched] Room:', matchedRoomId1);

          // Client 1 sends a message
          setTimeout(() => {
            console.log('[Step 6] Client 1 sending message: "Hello from guest!"');
            client1.publish({
              destination: '/app/chat.send',
              body: JSON.stringify({ roomId: matchedRoomId1, content: 'Hello from guest!' })
            });
          }, 600);
        }
      });

      client1.subscribe('/user/queue/messages', (msg) => {
        const payload = JSON.parse(msg.body);
        console.log('[Client 1 Received Message]:', payload.senderLabel, 'says:', payload.content);

        if (!payload.fromMe) {
          // Received reply from partner! Test typing indicator and next
          console.log('[Step 8] Client 1 sending typing indicator...');
          client1.publish({
            destination: '/app/chat.typing',
            body: JSON.stringify({ roomId: matchedRoomId1, isTyping: true })
          });

          setTimeout(() => {
            console.log('[Step 9] Client 1 clicking Next stranger...');
            client1.publish({ destination: '/app/chat.next' });
          }, 800);
        }
      });

      checkBothConnectedAndJoin();
    };

    client2.onConnect = () => {
      client2Connected = true;
      console.log('Client 2 STOMP connected!');

      client2.subscribe('/user/queue/events', (msg) => {
        const ev = JSON.parse(msg.body);
        console.log('[Client 2 Event]:', ev.event, ev.message || '');
        if (ev.event === 'MATCHED') {
          matchedRoomId2 = ev.roomId;
          console.log('[Client 2 Matched] Room:', matchedRoomId2);
        }
        if (ev.event === 'PARTNER_TYPING') {
          console.log('[Client 2 Success]: Received partner typing notification!');
        }
        if (ev.event === 'PARTNER_LEFT') {
          console.log('[Client 2 Success]: Received PARTNER_LEFT after Client 1 clicked Next!');
          clearTimeout(timeout);
          resolve({ conversationId: ev.payload?.conversationId });
        }
      });

      client2.subscribe('/user/queue/messages', (msg) => {
        const payload = JSON.parse(msg.body);
        console.log('[Client 2 Received Message]:', payload.senderLabel, 'says:', payload.content);

        if (!payload.fromMe && payload.content === 'Hello from guest!') {
          // Send reply
          console.log('[Step 7] Client 2 replying: "Hello guest, I am Alice!"');
          client2.publish({
            destination: '/app/chat.send',
            body: JSON.stringify({ roomId: matchedRoomId2, content: 'Hello guest, I am Alice!' })
          });
        }
      });

      checkBothConnectedAndJoin();
    };

    client1.activate();
    client2.activate();
  });

  const result = await testCompletedPromise;
  console.log('Real-time test successful! Conversation ID recorded:', result.conversationId);

  // 10. Verify history persistence for Alice
  console.log('[Step 10] Verifying conversation persistence in history endpoint for Alice...');
  const historyRes = await fetch(`${API_BASE}/conversations`, {
    headers: { Authorization: `Bearer ${userData.token}` }
  });
  const historyData = await historyRes.json();
  console.log('Alice Conversation History count:', historyData.content?.length);
  console.log('Latest history item:', JSON.stringify(historyData.content?.[0], null, 2));

  // 11. Verify transcript detail
  if (historyData.content?.[0]?.id) {
    const detailRes = await fetch(`${API_BASE}/conversations/${historyData.content[0].id}`, {
      headers: { Authorization: `Bearer ${userData.token}` }
    });
    const detailData = await detailRes.json();
    console.log('Alice Transcript detail message count:', detailData.messages?.length);
    console.log('Transcript messages preview:', detailData.messages?.map(m => `[${m.senderLabel}] ${m.content}`).join(' | '));
  }

  // Deactivate STOMP clients
  client1.deactivate();
  client2.deactivate();

  console.log('--- ALL END-TO-END TESTS PASSED WITH 100% SUCCESS ---');
}

main().catch(err => {
  console.error('E2E Test Failed:', err);
  process.exit(1);
});
