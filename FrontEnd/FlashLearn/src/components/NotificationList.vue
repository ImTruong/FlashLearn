<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from "vue-router";
import NotificationItem from './NotificationItem.vue';
import InvitationBox from './InvitationBox.vue';
import ViewOnlySet from './OnlyViewSet.vue';
import { fetchNotifications, editReadMode } from '@/apis/notificationApi';

const notifications = ref([]);
const notiMode = ref(true);
const notification = ref("");
const requestId = ref("");
const router = useRouter();
const setRequest = ref(false);
const classRequest = ref(false);

let intervalId = null; // để lưu id của setInterval

const fetchNotificationsData = async () => {
  try {
    const token = localStorage.getItem("token");
    const response = await fetchNotifications(token);
    notifications.value = response.data;
  } catch (error) {
    if (error.response) {
      alert('Error:', error.response.data.message);
    } else {
      alert('Network or Axios error:', error.message);
    }
  }
};

onMounted(() => {
  fetchNotificationsData();
  intervalId = setInterval(fetchNotificationsData, 3000); // gọi mỗi 3s
});

onUnmounted(() => {
  clearInterval(intervalId); // dọn dẹp khi component bị huỷ
});

const markAsRead = async (notificationId) => {
    try {
        const token = localStorage.getItem("token");
        await editReadMode(notificationId, token);
        notifications.value = notifications.value.map(notification => {
            if (notification.id === notificationId) {
                notification.isRead = true;
            }
            return notification;
        });
    } catch (error) {
        alert(error)
    }
};

// REMINDER_STUDY_SESSION 
// CLASS_SET_REJECT 
// CLASS_SET_ACCEPT 
// CLASS_SET_REQUEST 
// REJECT_CLASS_INVITATION 
// ACCEPT_CLASS_INVITATION 
// REJECT_CLASS_JOIN_REQUEST 
// ACCEPT_CLASS_JOIN_REQUEST 
// CLASS_INVITATION 
// CLASS_JOIN_REQUEST
const openModal = (notificationItem) => {
    notification.value = notificationItem;
    const type = notificationItem.type;
    markAsRead(notificationItem.id);
    if (type === 'CLASS_JOIN_REQUEST') {
        requestId.value = notificationItem.additionalInfo.classJoinRequestId;
        notiMode.value = false;
        classRequest.value = true;
    } else if (type === "CLASS_INVITATION") {
        requestId.value = notificationItem.additionalInfo.classInvitationId;
        notiMode.value = false;
        classRequest.value = true;
    } else if (notificationItem.type === "CLASS_SET_REQUEST") {
        requestId.value = notificationItem.additionalInfo.classSetRequestId;
        setRequest.value = true;
        notiMode.value = false;
    } else if (notificationItem.type === "REMINDER_STUDY_SESSION") {
        router.push('/review');
    }
};

const closeModal = () => {
    notiMode.value = true;
    classRequest.value = false;
    setRequest.value = false; 
};
</script>

<template>
    <div class="notification-list" v-if="notiMode">
        <div class="notification-header">
            <h3>Notifications</h3>
        </div>
        <div class="notification-body">
            <div class="notification" v-for="notificationItem in notifications" @click="openModal(notificationItem)">
                <NotificationItem
                    :key="notificationItem.id"
                    :notification="notificationItem"
                />
            </div>
        </div>
    </div>

    <InvitationBox v-if="classRequest"
        :requestId="requestId" 
        :Overlay_background="classRequest" 
        :requestType="notification.type"
        @close="closeModal"
    ></InvitationBox>
    <ViewOnlySet v-if="setRequest" :requestId="requestId" @close="closeModal"></ViewOnlySet>
</template>

<style scoped>
.notification-list {
    position: fixed;
    top: 30%;
    left: 240px;
    width: 300px;
    height: 400px;
    padding: 16px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0px 4px 12px rgba(0, 0, 0, 0.5);
    overflow: auto;
    z-index: 25;
}



.notification-body {
    top: 240px;
    width: 280px;
    height: 330px;
    overflow: auto;
}

.notification-list h3 {
    font-size: 20px;
    font-weight: 400;
    margin-bottom: 12px;
}
</style>