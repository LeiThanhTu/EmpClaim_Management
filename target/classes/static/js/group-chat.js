// Completely disable all notifications
// Override the native notification methods
(function() {
    // Use MutationObserver to detect and remove the notification as soon as it appears
    function setupNotificationObserver() {
        // Create a MutationObserver to watch for DOM changes
        const observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                // Check for added nodes
                if (mutation.addedNodes && mutation.addedNodes.length > 0) {
                    mutation.addedNodes.forEach(function(node) {
                        // Check if this is an element node
                        if (node.nodeType === 1) {
                            // Check if this is the notification or contains the notification text
                            if (node.textContent && node.textContent.includes('Đã kết nối đến máy chủ chat')) {
                                console.log('Found and removing notification via observer:', node);
                                node.remove();
                            }
                            
                            // Also check for elements with green background
                            if (window.getComputedStyle(node).backgroundColor === 'rgb(76, 175, 80)') {
                                console.log('Found and removing green element via observer:', node);
                                node.remove();
                            }
                            
                            // Check for toast or notification classes
                            if (node.classList && 
                                (node.classList.contains('toast') || 
                                node.classList.contains('notification') || 
                                node.classList.contains('alert'))) {
                                console.log('Found and removing notification element via observer:', node);
                                node.remove();
                            }
                        }
                    });
                }
            });
        });
        
        // Start observing the document with the configured parameters
        observer.observe(document.body, { 
            childList: true,     // Watch for changes to the direct children
            subtree: true,       // Watch the entire subtree
            attributes: true     // Watch for attribute changes
        });
        
        console.log('Notification observer set up');
        return observer;
    }
    
    // Set up the observer immediately
    const notificationObserver = setupNotificationObserver();
    
    // Direct removal of the specific notification element
    function removeConnectionNotification() {
        // Find all elements that might contain the notification
        const allElements = document.querySelectorAll('body > *');
        allElements.forEach(el => {
            // Check if this element contains the notification text
            if (el.textContent && el.textContent.includes('Đã kết nối đến máy chủ chat')) {
                console.log('Found and removing notification element:', el);
                el.remove();
                return;
            }
            
            // Also check for elements with green background which is likely the notification
            if (window.getComputedStyle(el).backgroundColor === 'rgb(76, 175, 80)') {
                console.log('Found and removing green notification element:', el);
                el.remove();
                return;
            }
        });
    }
    
    // Run this function immediately and repeatedly
    removeConnectionNotification();
    setInterval(removeConnectionNotification, 100);
    
    // Override the showNotification function
    window.showNotification = function() {
        console.log('Notification suppressed');
        return;
    };
    
    // Override any toast notifications that might be used
    if (window.toastr) {
        window.toastr.success = window.toastr.info = window.toastr.warning = window.toastr.error = function() {
            console.log('Toast notification suppressed');
            return;
        };
    }
    
    // Override any Bootstrap notifications
    if (window.bootstrap && window.bootstrap.Toast) {
        const originalToast = window.bootstrap.Toast;
        window.bootstrap.Toast = function(element, options) {
            console.log('Bootstrap toast suppressed');
            return {
                show: function() {},
                hide: function() {},
                dispose: function() {}
            };
        };
    }
    
    // Add CSS to hide any notifications
    const style = document.createElement('style');
    style.textContent = `
        .chat-notification, 
        .toast, 
        .toast-success, 
        .toast-info, 
        .toast-warning, 
        .toast-error,
        [class*="notification"],
        [class*="alert"],
        [class*="toast"] {
            display: none !important;
            visibility: hidden !important;
            opacity: 0 !important;
            height: 0 !important;
            width: 0 !important;
            position: absolute !important;
            z-index: -9999 !important;
            pointer-events: none !important;
        }
    `;
    document.head.appendChild(style);
    
    console.log('All notification systems have been disabled');
})();

// Biến toàn cục
let stompClient = null;
let username = '';
let userId = '';
let userRole = ''; 
let sentMessages = new Set(); // Track sent messages to prevent duplicates
let displayedMessageIds = new Set(); // Track displayed message IDs to prevent duplicates
let isConnected = false;
let reconnectAttempts = 0;
let maxReconnectAttempts = 5;
let groupSubscription = null; // Store the subscription to /topic/group
let processingMessage = false; // Flag to prevent duplicate message processing

// Kết nối đến WebSocket server khi trang được tải
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing chat...');
    
    // Lấy thông tin người dùng từ session (có thể được lưu trong một thẻ meta hoặc data attribute)
    fetchUserInfo();
    
    // Xử lý sự kiện khi nhấn nút gửi
    const sendButton = document.getElementById('send-group-message');
    if (sendButton) {
        sendButton.addEventListener('click', sendGroupMessage);
    } else {
        console.error('Send button not found!');
    }
    
    // Xử lý sự kiện khi nhấn Enter trong ô input
    const messageInput = document.getElementById('group-message-input');
    if (messageInput) {
        messageInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                sendGroupMessage();
            }
        });
    } else {
        console.error('Message input not found!');
    }
    
    // Tải lịch sử tin nhắn nhóm
    loadGroupChatHistory();
});

// Lấy thông tin người dùng hiện tại
function fetchUserInfo() {
    console.log('Fetching user info...');
    
    // Get user info from meta tags
    const userIdMeta = document.querySelector('meta[name="userId"]');
    const usernameMeta = document.querySelector('meta[name="username"]');
    const userRoleMeta = document.querySelector('meta[name="userRole"]');
    
    let userInfoValid = false;
    
    if (userIdMeta && usernameMeta) {
        userId = userIdMeta.getAttribute('content');
        username = usernameMeta.getAttribute('content');
        
        if (userRoleMeta) {
            userRole = userRoleMeta.getAttribute('content');
        }
        
        // Check if user info is valid
        if (userId && username && userId !== 'null' && username !== 'null') {
            console.log('User info from meta tags - ID:', userId, 'Name:', username, 'Role:', userRole);
            userInfoValid = true;
            
            // Initialize WebSocket connection after getting valid user info
            connect();
        } else {
            console.error('Invalid user information from meta tags');
        }
    }
    
    // If meta tags don't provide valid info, try API fallback
    if (!userInfoValid) {
        console.log('Fetching user info from API...');
        fetch('/api/current-user')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok: ' + response.status);
                }
                return response.json();
            })
            .then(data => {
                console.log('API response:', data);
                
                if (data && data.id && data.name) {
                    userId = data.id;
                    username = data.name;
                    userRole = data.designation;
                    console.log('User info from API - ID:', userId, 'Name:', username, 'Role:', userRole);
                    
                    // Initialize WebSocket connection after getting valid user info from API
                    connect();
                } else {
                    console.error('Invalid user data from API:', data);
                }
            })
            .catch(error => {
                console.error('Error fetching user info from API:', error);
                alert('Không thể lấy thông tin người dùng. Vui lòng làm mới trang.');
            });
    }
}

// Kết nối đến WebSocket server
function connect() {
    if (isConnected && stompClient && stompClient.connected) {
        console.log('Already connected to WebSocket server');
        return;
    }
    
    console.log('Connecting to WebSocket server...');
    
    try {
        // Check if there's an existing connection attempt
        if (stompClient) {
            try {
                stompClient.disconnect();
                console.log('Disconnected existing client');
            } catch (e) {
                console.warn('Error disconnecting existing client:', e);
            }
        }
        
        // Create a new SockJS connection with a timeout
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        
        // Disable debug logs
        stompClient.debug = null;
        
        // Set a connection timeout
        const connectionTimeout = setTimeout(() => {
            console.error('WebSocket connection timeout');
            if (stompClient && !stompClient.connected) {
                try {
                    socket.close();
                    isConnected = false;
                    showNotification('Kết nối đến máy chủ chat bị timeout. Đang thử lại...', 'error');
                    setTimeout(connect, 2000);
                } catch (e) {
                    console.error('Error during timeout handling:', e);
                }
            }
        }, 10000); // 10 second timeout
        
        stompClient.connect({}, 
            // Success callback
            function(frame) {
                console.log('Connected to WebSocket: ' + frame);
                clearTimeout(connectionTimeout);
                isConnected = true;
                reconnectAttempts = 0;
                
                // Subscribe to group messages
                subscribeToGroupMessages();
                
                // Thông báo tham gia
                sendJoinMessage();
                
                // Load chat history after connection
                loadGroupChatHistory();
            }, 
            // Error callback
            function(error) {
                console.error('Error connecting to WebSocket:', error);
                clearTimeout(connectionTimeout);
                isConnected = false;
                
                // Thử kết nối lại với số lần giới hạn
                reconnectAttempts++;
                if (reconnectAttempts <= maxReconnectAttempts) {
                    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000);
                    showNotification(`Không thể kết nối đến máy chủ chat. Đang thử lại lần ${reconnectAttempts}...`, 'warning');
                    setTimeout(connect, delay);
                } else {
                    showNotification('Không thể kết nối đến máy chủ chat sau nhiều lần thử. Vui lòng làm mới trang.', 'error');
                }
            }
        );
    } catch (error) {
        console.error('Exception during WebSocket connection:', error);
        showNotification('Lỗi kết nối: ' + error.message, 'error');
        
        // Try to reconnect after a delay
        setTimeout(connect, 3000);
    }
}

// Subscribe to group messages
function subscribeToGroupMessages() {
    if (stompClient && stompClient.connected) {
        // Store the subscription so we can unsubscribe later if needed
        groupSubscription = stompClient.subscribe('/topic/group', function(message) {
            if (processingMessage) {
                console.log('Already processing a message, skipping this one');
                return;
            }
            
            try {
                const chatMessage = JSON.parse(message.body);
                console.log('Received message:', chatMessage);
                displayGroupMessage(chatMessage);
            } catch (error) {
                console.error('Error processing received message:', error);
            }
        });
        
        console.log('Subscribed to group messages');
    } else {
        console.error('Cannot subscribe: WebSocket not connected');
    }
}

// Gửi thông báo tham gia khi kết nối thành công
// function sendJoinMessage() {
//     // Skip sending join message to avoid notification on page reload
//     console.log('Join message disabled to prevent notifications on page reload');
//     return;
    
    /* Original code commented out
    if (stompClient && stompClient.connected && userId && username) {
        console.log('Sending join message as:', username);
        
        const chatMessage = {
            senderId: userId,
            senderName: username,
            content: username + ' đã tham gia cuộc trò chuyện',
            type: 'JOIN',
            isGroupMessage: true
        };
        
        try {
            stompClient.send('/app/chat.sendGroupMessage', {}, JSON.stringify(chatMessage));
        } catch (error) {
            console.error('Error sending join message:', error);
        }
    } else {
        console.warn('Cannot send join message: Missing user info or WebSocket not connected');
    }
    */
// }

// Gửi tin nhắn nhóm
function sendGroupMessage() {
    const messageInput = document.getElementById('group-message-input');
    if (!messageInput) {
        console.error('Message input element not found');
        showNotification('Lỗi: Không tìm thấy ô nhập tin nhắn', 'error');
        return;
    }
    
    const messageContent = messageInput.value.trim();
    
    if (!messageContent) {
        return; // Don't send empty messages
    }
    
    // Prevent sending multiple messages at once
    if (processingMessage) {
        console.log('Already processing a message, ignoring this one');
        return;
    }
    
    processingMessage = true;
    
    console.log('Attempting to send message:', messageContent);
    console.log('Current user info:', { userId, username, userRole });
    console.log('WebSocket connection:', stompClient ? 'Exists' : 'Null', 
                stompClient ? (stompClient.connected ? 'Connected' : 'Disconnected') : '');
    
    // Check if we have user information
    if (!userId || !username) {
        console.error('Missing user information. Trying to fetch again...');
        showNotification('Không thể gửi tin nhắn: Thiếu thông tin người dùng', 'error');
        
        // Try to get user info from session
        fetch('/api/current-user')
            .then(response => response.json())
            .then(data => {
                console.log('User info from API:', data);
                if (data && data.id && data.name) {
                    userId = data.id;
                    username = data.name;
                    userRole = data.designation;
                    
                    // Try sending again
                    setTimeout(() => {
                        processingMessage = false;
                        // Restore the message
                        messageInput.value = messageContent;
                        sendGroupMessage();
                    }, 500);
                } else {
                    processingMessage = false;
                    showNotification('Không thể xác định thông tin người dùng. Vui lòng làm mới trang và thử lại.', 'error');
                }
            })
            .catch(error => {
                processingMessage = false;
                console.error('Error fetching user info:', error);
                showNotification('Lỗi khi lấy thông tin người dùng: ' + error.message, 'error');
            });
        return;
    }
    
    // Check WebSocket connection
    if (!stompClient || !stompClient.connected) {
        console.error('WebSocket not connected. Trying to reconnect...');
        showNotification('Đang kết nối lại...', 'warning');
        
        // Save the message to send after connection
        const savedMessage = messageContent;
        
        // Try to reconnect
        connect();
        
        // Try sending again after a delay
        setTimeout(() => {
            processingMessage = false;
            if (stompClient && stompClient.connected) {
                // Restore the message and try again
                messageInput.value = savedMessage;
                sendGroupMessage();
            } else {
                showNotification('Không thể kết nối đến máy chủ. Vui lòng thử lại sau.', 'error');
            }
        }, 2000);
        return;
    }
    
    // Clear input field immediately for better UX
    const savedMessage = messageContent;
    messageInput.value = '';
    
    const chatMessage = {
        senderId: userId,
        senderName: username,
        content: savedMessage,
        type: 'GROUP',
        isGroupMessage: true
    };
    
    // Create a unique ID for this message to track it
    const messageId = `${userId}-${savedMessage}-${new Date().toISOString()}`;
    
    try {
        console.log('Sending group message:', chatMessage);
        
        // Display the message locally first
        const tempMessage = {
            ...chatMessage,
            sentTime: formatDateTime(new Date())
        };
        
        // Add this message ID to our tracking set before displaying
        displayedMessageIds.add(messageId);
        
        // Display the message locally
        displayGroupMessage(tempMessage);
        
        // Then send it to the server
        stompClient.send('/app/chat.sendGroupMessage', {}, JSON.stringify(chatMessage));
        
        // Reset processing flag
        processingMessage = false;
        
    } catch (error) {
        console.error('Error sending message:', error);
        showNotification('Không thể gửi tin nhắn: ' + error.message, 'error');
        
        // Restore the message in the input field
        messageInput.value = savedMessage;
        
        // Reset processing flag
        processingMessage = false;
    }
}

// Hiển thị tin nhắn nhóm
function displayGroupMessage(message) {
    // Skip JOIN and LEAVE messages if needed
    if (message.type === 'JOIN' || message.type === 'LEAVE') {
        console.log('Skipping JOIN/LEAVE message:', message);
        return;
    }
    
    const chatContainer = document.getElementById('group-chat-container');
    if (!chatContainer) {
        console.error('Chat container element not found');
        return;
    }
    
    // Create a unique ID for this message
    const messageId = `${message.senderId}-${message.content}-${message.sentTime || new Date().toISOString()}`;
    
    // Check if we've already displayed this message
    if (displayedMessageIds.has(messageId)) {
        console.log('Message already displayed, skipping:', messageId);
        return;
    }
    
    // Mark this message as displayed
    displayedMessageIds.add(messageId);
    
    console.log('Displaying message:', message);
    
    // Xác định xem tin nhắn là của người dùng hiện tại hay người khác
    const isCurrentUser = message.senderId == userId;
    
    // Tạo phần tử HTML cho tin nhắn
    const messageElement = document.createElement('div');
    messageElement.className = isCurrentUser ? 'sent-message' : 'received-message';
    messageElement.dataset.messageId = messageId;
    
    if (!isCurrentUser) {
        messageElement.style.marginBottom = '15px';
    } else {
        messageElement.style.textAlign = 'right';
        messageElement.style.marginBottom = '15px';
    }
    
    // Thêm tên người gửi nếu không phải là người dùng hiện tại
    if (!isCurrentUser) {
        const senderNameElement = document.createElement('div');
        senderNameElement.className = 'sender-name';
        senderNameElement.textContent = message.senderName;
        senderNameElement.style.fontWeight = 'bold';
        senderNameElement.style.fontSize = '0.9em';
        messageElement.appendChild(senderNameElement);
    }
    
    // Tạo nội dung tin nhắn
    const contentElement = document.createElement('p');
    contentElement.className = 'message';
    contentElement.textContent = message.content;
    
    if (isCurrentUser) {
        contentElement.style.backgroundColor = '#007bff';
        contentElement.style.color = 'white';
    } else {
        contentElement.style.backgroundColor = '#e0e0e0';
    }
    
    contentElement.style.padding = '10px';
    contentElement.style.borderRadius = '10px';
    contentElement.style.maxWidth = '75%';
    contentElement.style.marginBottom = '5px';
    
    if (isCurrentUser) {
        contentElement.style.marginLeft = 'auto';
    } else {
        contentElement.style.marginRight = 'auto';
    }
    
    messageElement.appendChild(contentElement);
    
    // Thêm thởi gian
    const timeElement = document.createElement('small');
    timeElement.style.color = 'gray';
    timeElement.textContent = message.sentTime || formatDateTime(new Date());
    messageElement.appendChild(timeElement);
    
    // Thêm tin nhắn vào container
    chatContainer.appendChild(messageElement);
    
    // Cuộn xuống tin nhắn mới nhất
    chatContainer.scrollTop = chatContainer.scrollHeight;
}

// Tải lịch sử tin nhắn nhóm
function loadGroupChatHistory() {
    console.log('Loading group chat history...');
    
    // Reset the displayedMessageIds set to avoid skipping messages from history
    displayedMessageIds = new Set();
    
    fetch('/api/chat/group-history')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok: ' + response.status);
            }
            return response.json();
        })
        .then(messages => {
            console.log('Received chat history:', messages);
            
            // Find the correct chat container
            const chatContainer = document.getElementById('group-chat-container');
            if (!chatContainer) {
                console.error('Chat container element not found');
                return;
            }
            
            // Clear the chat container
            chatContainer.innerHTML = '';
            
            // Hiển thị tin nhắn từ lịch sử, từ cũ đến mới
            if (messages && messages.length > 0) {
                // Reverse để hiển thị từ cũ đến mới
                messages.reverse().forEach(message => {
                    displayGroupMessage(message);
                });
                
                // Cuộn xuống tin nhắn mới nhất
                scrollToBottom();
            } else {
                console.log('No chat history found');
            }
        })
        .catch(error => {
            console.error('Error loading chat history:', error);
        });
}

// Helper function to scroll to the bottom of the chat
function scrollToBottom() {
    const chatContainer = document.getElementById('group-chat-container');
    if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
    }
}

// Hàm định dạng thởi gian
function formatDateTime(dateTimeString) {
    if (!dateTimeString) return '';
    
    const date = new Date(dateTimeString);
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    
    return `${day}/${month}/${year} ${hours}:${minutes}`;
}

 // Ngắt kết nối khi rời trang
window.addEventListener('beforeunload', function() {
    if (stompClient && stompClient.connected) {
        console.log('Disconnecting from WebSocket server...');
        
        // try {
        //     // Gửi thông báo rời đi
        //     const chatMessage = {
        //         senderId: userId,
        //         senderName: username,
        //         content: username + ' đã rời khỏi cuộc trò chuyện',
        //         type: 'LEAVE',
        //         isGroupMessage: true
        //     };
        //
        //     stompClient.send('/app/chat.sendGroupMessage', {}, JSON.stringify(chatMessage));
        //
        //     // Ngắt kết nối
        //     stompClient.disconnect();
        //     isConnected = false;
        // } catch (error) {
        //     console.error('Error during disconnect:', error);
        // }
    }
});

// Hiển thị thông báo cho người dùng
function showNotification(message, type = 'info') {
    // Empty function to prevent any notifications
    console.log('Notification suppressed:', message, type);
    // Do nothing - this prevents errors when the function is called
}
