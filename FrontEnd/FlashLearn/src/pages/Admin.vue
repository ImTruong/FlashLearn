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

// Sets pagination
const setsPage = ref(0);
const setsSize = ref(10);
const setsTotalPages = ref(0);
const setsTotalElements = ref(0);

// Classes pagination
const classesPage = ref(0);
const classesSize = ref(10);
const classesTotalPages = ref(0);
const classesTotalElements = ref(0);

let intervalId = null;

// Updated fetchData to handle pagination
const fetchData = async (token) => {
  try {
    // Fetch sets with pagination
    if (activeTab.value === "Flashcard sets" || activeTab.value === "Classes") {
      const setsData = await getAllSets(token, search.value, setsPage.value, setsSize.value);
      sets.value = setsData.content;
      setsTotalPages.value = setsData.totalPages;
      setsTotalElements.value = setsData.totalElements;
    }

    // Fetch classes with pagination
    if (activeTab.value === "Flashcard sets" || activeTab.value === "Classes") {
      const classesData = await getAllClasses(token, search.value, classesPage.value, classesSize.value);
      classes.value = classesData.content;
      classesTotalPages.value = classesData.totalPages;
      classesTotalElements.value = classesData.totalElements;
    }
  } catch (error) {
    console.error("Error fetching data:", error);
  }
};

// Pagination handlers
const changeSetsPage = (newPage) => {
  setsPage.value = newPage;
  const token = localStorage.getItem("token");
  fetchData(token);
};

const changeClassesPage = (newPage) => {
  classesPage.value = newPage;
  const token = localStorage.getItem("token");
  fetchData(token);
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
    // Reset to page 0 when switching tabs
    setsPage.value = 0;
    classesPage.value = 0;
    fetchData(token);
  }
}

// Hàm tìm kiếm mới
const handleSearch = () => {
  const token = localStorage.getItem("token");
  // Reset về trang đầu tiên khi tìm kiếm
  setsPage.value = 0;
  classesPage.value = 0;
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

      <!-- Pagination for Sets -->
      <div class="pagination" v-if="setsTotalPages > 1">
        <button
            class="pagination-button"
            :disabled="setsPage === 0"
            @click="changeSetsPage(setsPage - 1)"
        >
          &lt;
        </button>
        <span class="pagination-info">
          Page {{ setsPage + 1 }} of {{ setsTotalPages }}
        </span>
        <button
            class="pagination-button"
            :disabled="setsPage >= setsTotalPages - 1"
            @click="changeSetsPage(setsPage + 1)"
        >
          &gt;
        </button>
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

      <!-- Pagination for Classes -->
      <div class="pagination" v-if="classesTotalPages > 1">
        <button
            class="pagination-button"
            :disabled="classesPage === 0"
            @click="changeClassesPage(classesPage - 1)"
        >
          &lt;
        </button>
        <span class="pagination-info">
          Page {{ classesPage + 1 }} of {{ classesTotalPages }}
        </span>
        <button
            class="pagination-button"
            :disabled="classesPage >= classesTotalPages - 1"
            @click="changeClassesPage(classesPage + 1)"
        >
          &gt;
        </button>
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

/* Pagination Styles */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  gap: 10px;
}

.pagination-button {
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 5px 10px;
  cursor: pointer;
  font-size: 14px;
}

.pagination-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  font-size: 14px;
  color: #666;
}
</style>