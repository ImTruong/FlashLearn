<script setup>
import Header from '@/components/Header.vue';
import { ref, onMounted, computed } from 'vue';
import { getStudySessionByTime, getStudySessionByWord, getStudySessionBySpecificWord, getStudySessionBySpecificTime} from "@/apis/studyApi.js";

const activeTab = ref('By Time');
const data = ref([]);
const singleItemMode = ref(false);
const singleItemHeader = ref('');
const token = localStorage.getItem('token');

// Pagination state
const currentPage = ref(0);
const pageSize = ref(20); // Default page size
const totalPages = ref(0);
const totalElements = ref(0);

const fetchStatisticByTime = async (page = 0) => {
  try {
    const response = await getStudySessionByTime(token, page, pageSize.value);
    data.value = response.content;
    totalPages.value = response.totalPages;
    totalElements.value = response.totalElements;
    currentPage.value = page;
  }
  catch (error) {
    console.error("Error fetching statistics:", error);
    alert(error)
  }
};

const fetchStatisticByWord = async (page = 0) => {
  try {
    const response = await getStudySessionByWord(token, page, pageSize.value);
    data.value = response.content;
    totalPages.value = response.totalPages;
    totalElements.value = response.totalElements;
    currentPage.value = page;
  }
  catch (error) {
    console.error("Error fetching statistics:", error);
    alert(error)
  }
};

const fetchStatisticBySpecificWord = async (wordId, page = 0) => {
  try {
    const response = await getStudySessionBySpecificWord(token, wordId, page, pageSize.value);
    data.value = response.content;
    totalPages.value = response.totalPages;
    totalElements.value = response.totalElements;
    currentPage.value = page;
  } catch (error) {
    console.error("Error fetching statistics:", error);
    alert(error)
  }
};

const fetchStatisticBySpecificTime = async (time, page = 0) => {
  try {
    const response = await getStudySessionBySpecificTime(token, time, page, pageSize.value);
    const cleanedResponse = response.content.map(item => ({
      ...item,
      time: item.time.split('.')[0]
    }));

    data.value = cleanedResponse;
    totalPages.value = response.totalPages;
    totalElements.value = response.totalElements;
    currentPage.value = page;
  } catch (error) {
    console.error("Error fetching statistics:", error);
    alert(error)
  }
};

// Computed property for pagination
const paginationInfo = computed(() => {
  if (totalElements.value === 0) return '0 of 0';

  const start = currentPage.value * pageSize.value + 1;
  const end = Math.min((currentPage.value + 1) * pageSize.value, totalElements.value);
  return `${start}-${end} of ${totalElements.value}`;
});

// Pagination methods
const goToNextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    loadPage(currentPage.value + 1);
  }
};

const goToPreviousPage = () => {
  if (currentPage.value > 0) {
    loadPage(currentPage.value - 1);
  }
};

const loadPage = (page) => {
  if (singleItemMode.value) {
    if (activeTab.value === 'By Time') {
      fetchStatisticBySpecificTime(singleItemHeader.value, page);
    } else {
      // Assuming you have the wordId when in single item mode for 'By Word'
      const wordId = data.value.length > 0 ? data.value[0].wordId : null;
      if (wordId) {
        fetchStatisticBySpecificWord(wordId, page);
      }
    }
  } else {
    if (activeTab.value === 'By Time') {
      fetchStatisticByTime(page);
    } else {
      fetchStatisticByWord(page);
    }
  }
};

onMounted(() => {
  if (token == null) {
    window.location.href = '/login';
    alert('Login for to use this feature');
    return;
  }
  fetchStatisticByTime();
});

const switchTab = () => {
  if (activeTab.value === "By Word") {
    activeTab.value = "By Time";
    fetchStatisticByTime();
  } else {
    activeTab.value = "By Word";
    fetchStatisticByWord();
  }
}

function handleSelectAnItem(item) {
  singleItemMode.value = true;
  if (activeTab.value === 'By Time') {
    singleItemHeader.value = item.date;
    fetchStatisticBySpecificTime(item.date);
  } else {
    singleItemHeader.value = item.word;
    fetchStatisticBySpecificWord(item.wordId);
  }
}

function gettingOutOfSingleItemMode() {
  singleItemMode.value = false;
  switchTab();
}
</script>

<template>
  <Header></Header>
  <div class="container">
    <div class="header">
      <h1 @click.stop="gettingOutOfSingleItemMode">Statistic</h1>
    </div>
    <div class="tabs">
      <button v-if="!singleItemMode" :class="{ active: activeTab === 'By Time' }" @click="switchTab">By Time</button>
      <button v-if="!singleItemMode" :class="{ active: activeTab === 'By Word' }" @click="switchTab">By Word</button>
      <h2 v-if="singleItemMode" class="singleItemHeader">{{ singleItemHeader }}</h2>
    </div>
    <div class="line"></div>

    <!-- By Time - List Mode -->
    <div v-if="activeTab === 'By Time' && !singleItemMode" v-for="d in data" :key="d.date" class="details" @click.stop="handleSelectAnItem(d)">
      <tr>
        <td>
          <div class="point"></div>
        </td>
        <td class="date">{{ d.date }}</td>
        <td class="words">{{ d.numberOfWords }} words</td>
        <td>
          <div class="bar" :style="{width: d.numberOfWords * 5 + 'px'}"></div>
        </td>
      </tr>
    </div>

    <!-- By Word - List Mode -->
    <div v-if="activeTab === 'By Word' && !singleItemMode" v-for="d in data" :key="d.wordId" class="details" @click.stop="handleSelectAnItem(d)">
      <tr>
        <td>
          <div class="point"></div>
        </td>
        <td class="hidden">{{ d.wordId }}</td>
        <td class="date">{{ d.word }}</td>
        <td class="words">{{ d.count }} Time</td>
        <td>
          <div class="bar" :style="{width: d.count * 5 + 'px'}"></div>
        </td>
      </tr>
    </div>

    <!-- By Word - Single Item Mode -->
    <div v-if="activeTab === 'By Word' && singleItemMode" v-for="d in data" :key="d.wordId" class="details">
      <tr>
        <td>
          <div class="point"></div>
        </td>
        <td class="hidden">{{ d.wordId }}</td>
        <td class="date">{{ d.word }}</td>
        <td class="words">{{ d.date }}</td>
        <td>
          <div class="words">{{ d.choosenAnswer }}</div>
        </td>
      </tr>
    </div>

    <!-- By Time - Single Item Mode -->
    <div v-if="activeTab === 'By Time' && singleItemMode" v-for="d in data" :key="d.time" class="details">
      <tr>
        <td>
          <div class="point"></div>
        </td>
        <td class="date">{{ d.time }}</td>
        <td class="words">{{ d.word }}</td>
        <td>
          <div class="words">{{ d.choosenAnswer }}</div>
        </td>
      </tr>
    </div>

    <!-- Pagination Controls - Always visible -->
    <div class="pagination-container">
      <div class="pagination">
        <button
            @click="goToPreviousPage"
            :disabled="currentPage === 0"
            class="pagination-btn"
        >
          &lt; Previous
        </button>

        <div class="pagination-info">
          <span>{{ paginationInfo }}</span>
          <span class="page-number">
            {{ totalPages > 0 ? `Page ${currentPage + 1} of ${totalPages}` : 'No data' }}
          </span>
        </div>

        <button
            @click="goToNextPage"
            :disabled="currentPage >= totalPages - 1"
            class="pagination-btn"
        >
          Next &gt;
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  margin-top: 80px;
  margin-left: 100px;
  margin-right: 100px;
}
.header {
  position: relative;
  margin-bottom: 30px;
}

.details {
  display: flex;
  margin-left: 30px;
  cursor: pointer;
}

tr {
  height: 50px;
}

.date {
  width: 150px;
  font-size: 20px;
  text-align: center;
}
.words {
  width: 150px;
  font-size: 20px;
}
.point {
  height: 10px;
  width: 10px;
  background-color: #BDEDF5;
  border-radius: 50%;
}
.bar {
  border-radius: 20px;
  height: 8px;
  width: 200px;
  background-color: #94e6f5;
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

.line {
  display: flex;
  bottom: 0;
  left: 0;
  right: 0;
  width: 100%;
  height: 1px;
  background-color: rgba(14, 14, 14, 0.1);
  margin-bottom: 30px;
}

.header h1 {
  cursor: pointer;
}

.singleItemHeader {
  flex-grow: 1;
  text-align: center;
}

.hidden {
  display: none;
}

/* Pagination Styles */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 20px;
}

.pagination-btn {
  padding: 8px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.pagination-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.pagination-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 14px;
}

.page-number {
  color: #6c757d;
  margin-top: 5px;
}
</style>