import { ref, onMounted, onUnmounted } from 'vue';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { defineStore } from 'pinia'
import { useChatStore } from '../stores/chat'

export const useWebSocket = defineStore('webSocket', () => {
    const stompClient = ref(null);
    const isConnected = ref(false);


    // 连接
    const connect = () => {
        // const socket = new SockJS('http://localhost:8888/ws');
        const socket = new SockJS('http://146.56.197.54:8888/ws');
        stompClient.value = new Client({
            webSocketFactory: () => socket,
            // 连接时传递用户标识
            connectHeaders: {
                // login: userId,
                Authorization: 'Bearer ' + localStorage.getItem('token')
                // 或通过其他方式传递 token
            },
            debug: (str) => console.log(str),
            onConnect: () => {
                isConnected.value = true;
                console.log('WebSocket 连接成功');
                
                // 1. 订阅群聊
                stompClient.value?.subscribe('/topic/group', (message) => {
                    const data = JSON.parse(message.body);
                    useChatStore().uploadGroupLatestMessage(data);  // Update the chat store with the latest message
                    console.log(data);
                });

                // 2. 订阅系统消息（广播）
                stompClient.value?.subscribe('/queue/system', (message) => {
                    const data = JSON.parse(message.body);
                    useChatStore().loadConversations();  // Update the chat store with the latest message
                    console.log(data);
                });

                // 3. 订阅私聊（/user 前缀会被自动解析为当前用户）
                stompClient.value?.subscribe('/user/queue/private', (message) => {
                    const data = JSON.parse(message.body);
                    useChatStore().uploadPrivateLatestMessage(data);  // Update the chat store with the latest message
                });

                // 4. 订阅定向系统消息
                // stompClient.value?.subscribe('/user/queue/system', (message) => {
                //     const data = JSON.parse(message.body);
                //     messages.value.push(data);
                // });

                // 5. 订阅心跳
                stompClient.value?.subscribe('/queue/pong', (message) => {
                    console.log('收到心跳响应:', message.body);
                });
            },
            onStompError: (frame) => {
                console.error('STOMP 错误:', frame);
            }
        });

        stompClient.value.activate();
    };

    const sendMessage = (message) => {
        if (!stompClient.value?.connected) return;
        stompClient.value.publish({
            destination: '/app/chat.send',
            body: JSON.stringify(message)
        });
    };

    const sendPing = () => {
        if (!stompClient.value?.connected) return;

        stompClient.value.publish({
            destination: '/app/chat.ping',
            body: JSON.stringify({ timestamp: Date.now() })
        });
    }

    // 断开连接[reference:10]
    const disconnect = () => {
        if (stompClient.value?.connected) {
            stompClient.value.deactivate();
            isConnected.value = false;
        }
    };

    return {
        isConnected,
        connect,
        disconnect,
        sendMessage,
        sendPing
    };
});
