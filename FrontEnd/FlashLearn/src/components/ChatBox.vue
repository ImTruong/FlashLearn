<template>
  <div class="chat-overlay" v-if="isVisible">
    <div class="chat-container">
      <!-- Chat Header -->
      <div class="chat-header">
        <h3>{{ className }} - Chat</h3>
        <button @click="closeChat" class="close-button">×</button>
      </div>

      <!-- Messages Container -->
      <div class="messages-container" ref="messagesContainer">
        <div v-if="loading" class="loading">Loading messages...</div>

        <div v-for="message in messages" :key="message.id" class="message-item">
          <!-- Reply to message display - Updated for better visibility -->
          <div v-if="message.replyToMessage" class="reply-to-container">
            <div class="reply-to-header">
              <span class="reply-indicator">↳</span>
              <span class="reply-to-sender">{{ message.replyToMessage.senderName }}</span>
              <span class="reply-to-role" :class="message.replyToMessage.senderRole?.toLowerCase()">                {{ message.replyToMessage.senderRole }}
              </span>
              <span class="reply-to-time">{{ formatTime(message.replyToMessage.createdAt) }}</span>
            </div>
            <div class="reply-to-content">
              <span v-if="message.replyToMessage.deleted" class="deleted-message">
                [Tin nhắn đã bị xóa]
              </span>
              <span v-else class="reply-message-text">{{ message.replyToMessage.message }}</span>
            </div>
          </div>

          <!-- Message Header -->
          <div class="message-header">
            <span class="sender-name">{{ message.senderName }}</span>
            <span class="sender-role" :class="message.senderRole?.toLowerCase()">              {{ message.senderRole }}
            </span>
            <span class="message-time">{{ formatTime(message.createdAt) }}</span>
            <button
                v-if="canDeleteMessage(message) && !message.deleted"
                @click="deleteMessage(message.id)"
                class="delete-button"
            >
              🗑️
            </button>
          </div>

          <!-- Message content -->
          <div class="message-content">
            <span v-if="message.deleted" class="deleted-message">
              [Tin nhắn đã bị xóa]
            </span>
            <span v-else>{{ message.message }}</span>
          </div>

          <div class="message-actions">
            <button
                v-if="!message.deleted"
                @click="setReplyTo(message)"
                class="reply-button"
            >
              Trả lời
            </button>
          </div>
        </div>

        <div v-if="messages.length === 0 && !loading" class="no-messages">
          Chưa có tin nhắn nào. Hãy bắt đầu cuộc trò chuyện!
        </div>
      </div>

      <!-- Reply Preview -->
      <div v-if="replyToMessage" class="reply-preview">
        <div class="reply-preview-header">
          <span class="reply-preview-label">Đang trả lời</span>
          <span class="reply-preview-sender">{{ replyToMessage.senderName }}</span>
        </div>
        <div class="reply-preview-content">
          <span v-if="replyToMessage.deleted" class="deleted-message">
            [Tin nhắn đã bị xóa]
          </span>
          <span v-else class="reply-preview-text">
            {{ replyToMessage.message.length > 100 ? replyToMessage.message.substring(0, 100) + '...' : replyToMessage.message }}
          </span>
        </div>
        <button @click="cancelReply" class="cancel-reply">×</button>
      </div>

      <!-- Message Input -->
      <div class="message-input-container">
        <textarea
            v-model="newMessage"
            @keydown.enter.prevent="sendMessage"
            placeholder="Nhập tin nhắn của bạn... (Enter để gửi)"
            class="message-input"
            :disabled="sending"
        ></textarea>
        <button @click="sendMessage" :disabled="!newMessage.trim() || sending" class="send-button">
          {{ sending ? 'Đang gửi...' : 'Gửi' }}
        </button>
      </div>

      <!-- Pagination -->
      <div class="pagination-container">
        <button @click="loadPreviousPage" :disabled="page === 0" class="pagination-button">
          Trước
        </button>
        <span class="pagination-info">Trang {{ page + 1 }} / {{ totalPages }}</span>
        <button @click="loadNextPage" :disabled="page >= totalPages - 1" class="pagination-button">
          Tiếp
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue';
import { getClassMessages, sendClassMessage, deleteClassMessage } from '@/apis/classApi.js';

const props = defineProps({
  isVisible: {
    type: Boolean,
    default: false
  },
  classId: {
    type: String,
    required: true
  },
  className: {
    type: String,
    required: true
  }
});

const emit = defineEmits(['close']);

// Reactive variables
const messages = ref([]);
const newMessage = ref('');
const loading = ref(false);
const sending = ref(false);
const page = ref(0);
const size = ref(20);
const totalPages = ref(1);
const replyToMessage = ref(null);
const messagesContainer = ref(null);
const currentUserRole = ref(localStorage.getItem('userRole') || 'STUDENT');

// Methods
const closeChat = () => {
  emit('close');
};

const formatTime = (dateTime) => {
  return new Date(dateTime).toLocaleString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
};

const canDeleteMessage = (message) => {
  return message.senderName == "You";
};

const loadMessages = async (pageNumber = 0) => {
  loading.value = true;
  try {
    const token = localStorage.getItem('token');
    const response = await getClassMessages(props.classId, token, pageNumber, size.value);
    console.log(response.data.content)
    if (response && response.data) {
      messages.value = response.data.content || [];
      totalPages.value = response.data.totalPages || 1;
      page.value = response.data.number || 0;
    }
  } catch (error) {
    console.error('Error loading messages:', error);
    alert('Không thể tải tin nhắn');
  } finally {
    loading.value = false;
  }
};

const sendMessage = async () => {
  if (!newMessage.value.trim()) return;

  sending.value = true;
  try {
    const token = localStorage.getItem('token');
    const messageData = {
      classId: parseInt(props.classId),
      message: newMessage.value.trim(),
      replyToMessageId: replyToMessage.value ? replyToMessage.value.id : null
    };

    await sendClassMessage(messageData, token);

    newMessage.value = '';
    replyToMessage.value = null;

    // Reload messages to show the new message
    await loadMessages(page.value);

    // Scroll to bottom
    await nextTick();
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  } catch (error) {
    console.error('Error sending message:', error);
    alert('Không thể gửi tin nhắn');
  } finally {
    sending.value = false;
  }
};

const deleteMessage = async (messageId) => {
  if (!confirm('Bạn có chắc chắn muốn xóa tin nhắn này?')) return;

  try {
    const token = localStorage.getItem('token');
    await deleteClassMessage(messageId, token);

    // Reload messages
    await loadMessages(page.value);
  } catch (error) {
    console.error('Error deleting message:', error);
    alert('Không thể xóa tin nhắn');
  }
};

const setReplyTo = (message) => {
  replyToMessage.value = message;
};

const cancelReply = () => {
  replyToMessage.value = null;
};

const loadPreviousPage = () => {
  if (page.value > 0) {
    loadMessages(page.value - 1);
  }
};

const loadNextPage = () => {
  if (page.value < totalPages.value - 1) {
    loadMessages(page.value + 1);
  }
};

// Watch for visibility changes
watch(() => props.isVisible, (newVal) => {
  if (newVal) {
    loadMessages();
  }
});

onMounted(() => {
  if (props.isVisible) {
    loadMessages();
  }
});
</script>

<style scoped>
.chat-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.chat-container {
  width: 600px;
  height: 700px;
  background-color: white;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.chat-header {
  padding: 15px 20px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.close-button {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-button:hover {
  color: #333;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background-color: #f8f9fa;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #666;
}

.message-item {
  margin-bottom: 15px;
  padding: 12px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* Reply to message styles - Updated for better visibility */
.reply-to-container {
  margin-bottom: 12px;
  padding: 10px;
  background-color: #f8f9fa;
  border-left: 4px solid #007bff;
  border-radius: 6px;
  position: relative;
}

.reply-to-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.reply-indicator {
  color: #007bff;
  font-weight: bold;
  font-size: 14px;
}

.reply-to-sender {
  font-weight: bold;
  font-size: 13px;
  color: #495057;
}

.reply-to-role {
  font-size: 10px;
  padding: 2px 4px;
  border-radius: 3px;
  text-transform: uppercase;
  font-weight: bold;
}

.reply-to-role.teacher {
  background-color: #e3f2fd;
  color: #1976d2;
}

.reply-to-role.student {
  background-color: #f3e5f5;
  color: #7b1fa2;
}

.reply-to-time {
  font-size: 11px;
  color: #6c757d;
  margin-left: auto;
}

.reply-to-content {
  padding: 8px 10px;
  background-color: white;
  border-radius: 4px;
  border: 1px solid #e9ecef;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.reply-message-text {
  font-size: 13px;
  color: #495057;
  line-height: 1.4;
  display: block;
  word-wrap: break-word;
}

.message-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  gap: 10px;
}

.sender-name {
  font-weight: bold;
  color: #333;
  font-size: 14px;
}

.sender-role {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 3px;
  text-transform: uppercase;
  font-weight: bold;
}

.sender-role.teacher {
  background-color: #e3f2fd;
  color: #1976d2;
}

.sender-role.student {
  background-color: #f3e5f5;
  color: #7b1fa2;
}

.message-time {
  font-size: 12px;
  color: #666;
  margin-left: auto;
}

.delete-button {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  padding: 4px;
  opacity: 0.7;
  border-radius: 3px;
}

.delete-button:hover {
  opacity: 1;
  background-color: #f8f9fa;
}

.deleted-message {
  color: #6c757d;
  font-style: italic;
  font-size: 13px;
}

.message-content {
  color: #333;
  line-height: 1.5;
  margin-bottom: 8px;
  font-size: 14px;
  word-wrap: break-word;
}

.message-actions {
  display: flex;
  justify-content: flex-end;
}

.reply-button {
  background: none;
  border: none;
  color: #007bff;
  cursor: pointer;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 3px;
  transition: background-color 0.2s;
}

.reply-button:hover {
  background-color: #f8f9fa;
}

.no-messages {
  text-align: center;
  padding: 40px;
  color: #666;
  font-style: italic;
}

/* Reply preview styles - Updated */
.reply-preview {
  padding: 12px;
  background-color: #e9ecef;
  border-top: 1px solid #dee2e6;
  display: flex;
  flex-direction: column;
  gap: 6px;
  position: relative;
}

.reply-preview-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.reply-preview-label {
  font-size: 12px;
  color: #666;
  font-weight: bold;
}

.reply-preview-sender {
  font-size: 12px;
  color: #007bff;
  font-weight: bold;
}

.reply-preview-content {
  font-size: 13px;
  color: #495057;
  line-height: 1.4;
  padding: 6px 8px;
  background-color: white;
  border-radius: 4px;
  border-left: 3px solid #007bff;
}

.reply-preview-text {
  display: block;
  word-wrap: break-word;
}

.cancel-reply {
  position: absolute;
  top: 8px;
  right: 8px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  color: #666;
  padding: 4px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.cancel-reply:hover {
  color: #333;
  background-color: #f8f9fa;
}

.message-input-container {
  padding: 15px;
  border-top: 1px solid #dee2e6;
  background-color: white;
  display: flex;
  gap: 10px;
}

.message-input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 20px;
  resize: none;
  max-height: 100px;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.4;
}

.message-input:focus {
  outline: none;
  border-color: #007bff;
}

.send-button {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-weight: bold;
  font-size: 14px;
  transition: background-color 0.2s;
}

.send-button:hover:not(:disabled) {
  background-color: #0056b3;
}

.send-button:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.pagination-container {
  padding: 10px;
  background-color: #f8f9fa;
  border-top: 1px solid #dee2e6;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
}

.pagination-button {
  padding: 6px 12px;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.2s;
}

.pagination-button:hover:not(:disabled) {
  background-color: #f8f9fa;
}

.pagination-button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.pagination-info {
  font-size: 12px;
  color: #666;
  font-weight: bold;
}
</style>