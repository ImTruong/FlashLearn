<script setup>
import ClassModal from '@/components/ClassModal.vue'
import SetBox from "@/components/SetBox.vue"
import ClassTable from '@/components/ClassTable.vue';
import UserManagement from '@/components/UserManagement.vue';
import { getAllClasses }  from "@/apis/classApi.js";
import { getAllSets }  from "@/apis/setApi.js";
import { ref } from "vue";
import { onMounted, onUnmounted } from "vue";


const classModalMode = ref(false);
const classTableMode = ref(false);
const activeTab = ref("Flashcard sets");
const classes = ref(null);
const sets = ref(null);
const search = ref("");
const page = ref(0);
const size = ref(10)

let intervalId = null;

const fetchData = async (token) => {
  try {
    const [setsData, classesData] = await Promise.all([
      getAllSets(token, search.value, page.value, size.value),
      getAllClasses(token, search.value, page.value, size.value),
    ]);

    sets.value = setsData;
    classes.value = classesData;
  } catch (error) {
    console.error("Error fetching data:", error);
  }
};

onMounted(() => {
  const token = localStorage.getItem("token");
  fetchData(token); // fetch ban đầu

  intervalId = setInterval(() => {
    fetchData(token); // truyền đúng token vào
  }, 3000);
});

onUnmounted(() => {
  clearInterval(intervalId); // dọn dẹp interval
});


const showClassModal = (classItem) => {
  classModalMode.value = true;
  localStorage.setItem('classId', classItem.classId);
  localStorage.setItem('className', classItem.className);
}

function closeOverlay() {
  emit('close');
  isMember.value = false;
}

const switchTab = (tabName) => {
  activeTab.value = tabName;
  // Nếu chuyển tab, xóa giá trị tìm kiếm và làm mới dữ liệu
  if (tabName === "User Management") {
    search.value = "";
  } else {
    const token = localStorage.getItem("token");
    page.value = 0;
    fetchData(token);
  }
}

// Hàm tìm kiếm mới
const handleSearch = () => {
  const token = localStorage.getItem("token");
  // Reset về trang đầu tiên khi tìm kiếm
  page.value = 0;
  fetchData(token);
}

// Hàm dùng khi nhấn Enter trong input tìm kiếm
const handleKeyDown = (event) => {
  if (event.key === 'Enter') {
    handleSearch();
  }
}

// Hàm kiểm tra xem có hiển thị thanh tìm kiếm hay không
const showSearchBar = () => {
  return activeTab.value !== "User Management";
}
</script>

<template>
  <main>
    <div class="header-container">
      <div class="tabs">
        <button :class="{ active: activeTab == 'Flashcard sets' }" @click="switchTab('Flashcard sets')">Flashcard sets</button>
        <button :class="{ active: activeTab == 'Classes' }" @click="switchTab('Classes')">Classes</button>
        <button :class="{ active: activeTab == 'User Management' }" @click="switchTab('User Management')">User Management</button>
      </div>

      <!-- Thanh tìm kiếm - ẩn khi ở tab User Management -->
      <div v-if="showSearchBar()" class="search-container">
        <input
            type="text"
            v-model="search"
            placeholder="Search..."
            class="search-input"
            @keydown="handleKeyDown"
        />
        <button class="search-button" @click="handleSearch">
          <img src="../assets/search-icon.svg" alt="Search" class="search-icon" />
        </button>
      </div>
    </div>

    <div class="line"></div>

    <!-- Tab Flashcard sets -->
    <div class="sets-list" v-if="activeTab == 'Flashcard sets'">
      <div class="sets-container">
        <div v-for="set in sets" :key="set.id" class="card-wrapper">
          <SetBox :set="set" />
        </div>
      </div>
    </div>

    <!-- Tab Classes -->
    <div class="class-list" v-if="activeTab == 'Classes'">
      <div v-for="classItem in classes" :key="classItem.classId" class="class-card" @click="showClassModal(classItem)">
        <img src="../assets/class.svg" alt="Icon" class="class-icon">
        <div class="class-info">
          <h3>{{ classItem.className }}</h3>
          <div class="class-detail" >
            <p>{{ classItem.numberOfSets }} {{ classItem.numberOfSets <= 1 ? 'set' : 'sets' }} |</p>
            <p> {{ classItem.numberOfMembers }} {{ classItem.numberOfMembers <= 1 ? 'member' : 'members' }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab User Management -->
    <div v-if="activeTab == 'User Management'">
      <UserManagement />
    </div>

    <ClassModal
        v-if="classModalMode"
        :Overlay_background ="classModalMode"
        @close ="classModalMode = false"
    />
    <ClassTable
        v-if="classTableMode"
        :isEditMode=true
        @close="classTableMode = false"
    />
  </main>
</template>


<style scoped>
main {
  position: absolute;
  top: 40px;
  width: 100%;
  padding: 50px;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.tabs {
  display: flex;
  gap: 20px;
}

.tabs button {
  background: none;
  border: none;
  padding: 10px;
  cursor: pointer;
  font-weight: bold;
  font-size: 16px;
  color: #333;
}

.tabs button.active {
  border-bottom: 2px solid black;
}

/* Kiểu dáng của thanh tìm kiếm */
.search-container {
  display: flex;
  align-items: center;
  border: 1px solid rgba(0, 0, 0, 0.2);
  border-radius: 20px;
  padding: 5px 10px;
  background-color: white;
}

.search-input {
  border: none;
  outline: none;
  padding: 8px;
  width: 200px;
  font-size: 14px;
}

.search-button {
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-icon {
  width: 16px;
  height: 16px;
}

.line {
  width: 100%;
  height: 1px;
  background-color: rgba(14, 14, 14, 0.1);
  margin-bottom: 30px;
}

.class-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.class-card {
  display: flex;
  align-items: center;
  background-color: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  cursor: pointer;
}

.class-card:hover{
  transform: scale(1.05);
}

.class-icon {
  font-size: 30px;
  margin-right: 20px;
}

.class-info h3 {
  margin: 0;
  font-weight: bold;
  font-size: 18px;
}

.class-list .class-detail{
  display: flex;
}

.class-info p {
  margin: 0;
  font-size: 14px;
  color: gray;
  margin-left: 5px;
  cursor: pointer;
}

.class-info p:hover{
  transform: scale(1.1);
}

.sets-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.card-wrapper {
  min-width: 250px;
}
</style>