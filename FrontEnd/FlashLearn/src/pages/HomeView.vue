<script setup>
import { ref, onMounted } from "vue";
import Header from "@/components/Header.vue";
import SetBox from "@/components/SetBox.vue";
import { useRouter } from "vue-router";
import { getCurrentUser } from "@/apis/userApi";
import { getRecentSet, getAllPublicSet, getLibrarySet } from "@/apis/setApi";

const router = useRouter();
const sets = ref([]);
const recentSetsData = ref({
  content: [],
  totalPages: 0,
  totalElements: 0,
  number: 0,
});
const publicSetsData = ref({
  content: [],
  totalPages: 0,
  totalElements: 0,
  number: 0,
});
const librarySetsPage = ref(0);
const recentSetsPage = ref(0);
const publicSetsPage = ref(0);
const recentSetsSize = ref(6);
const publicSetsSize = ref(6);
const librarySetsSize = ref(6);
const isLoadingRecent = ref(false);
const isLoadingPublic = ref(false);

const fetchUserInfo = async (token) => {
  try {
    const user = await getCurrentUser(token);
    localStorage.setItem("user", JSON.stringify(user));
  } catch (error) {
    console.error("Error fetching user info:", error);
  }
};

const fetchRecentSet = async (token, page) => {
  try {
    isLoadingRecent.value = true;
    const response = await getRecentSet(token, page, recentSetsSize.value);
    recentSetsData.value = response;
    recentSetsPage.value = page;
  } catch (error) {
    console.error("Error fetching recent sets:", error);
    alert("Failed to load recent sets. Please try again.");
  } finally {
    isLoadingRecent.value = false;
  }
};

const fetchPublicSet = async (token, page) => {
  try {
    isLoadingPublic.value = true;
    const response = await getAllPublicSet(token, page, publicSetsSize.value);
    publicSetsData.value = response;
    publicSetsPage.value = page;
  } catch (error) {
    console.error("Error fetching public sets:", error);
    alert("Failed to load public sets. Please try again.");
  } finally {
    isLoadingPublic.value = false;
  }
};

const fetchLibrarySet = async (token) => {
  try {
    const response = await getLibrarySet(token, librarySetsPage.value, librarySetsSize.value);
    sets.value = response.content;
  } catch (error) {
    console.error("Error fetching library sets:", error);
    alert("Failed to load library sets. Please try again.");
  }
};

const fetchAllData = async () => {
  const token = localStorage.getItem("token");
  if (!token) return;

  await Promise.all([
    fetchLibrarySet(token),
    fetchUserInfo(token),
    fetchRecentSet(token, recentSetsPage.value),
    fetchPublicSet(token, publicSetsPage.value),
  ]);
};

onMounted(() => {
  fetchAllData();
});

const goToStudy = () => {
  router.push("/review");
};

const goToLibrary = () => {
  localStorage.setItem("libraryTab", "FlashSetBox sets");
  router.push("/library");
};

// Hàm xử lý chuyển trang
const changePage = async (type, page) => {
  if (page < 0 || page >= (type === "recent" ? recentSetsData.value.totalPages : publicSetsData.value.totalPages)) return;

  const token = localStorage.getItem("token");
  if (type === "recent") {
    await fetchRecentSet(token, page);
  } else if (type === "public") {
    await fetchPublicSet(token, page);
  }
};

// Tạo danh sách số trang
const getPageNumbers = (totalPages, currentPage) => {
  const maxPagesToShow = 5;
  const pages = [];
  let startPage = Math.max(0, currentPage - Math.floor(maxPagesToShow / 2));
  let endPage = Math.min(totalPages - 1, startPage + maxPagesToShow - 1);

  if (endPage - startPage + 1 < maxPagesToShow) {
    startPage = Math.max(0, endPage - maxPagesToShow + 1);
  }

  for (let i = startPage; i <= endPage; i++) {
    pages.push(i);
  }
  return pages;
};
</script>

<template>
  <Header
      :recentSets="recentSetsData.content"
      :ownerSets="sets"
      :publicSets="publicSetsData.content"
      @reload="fetchAllData"
  />
  <div class="home">
    <div class="review-box" @click="goToStudy">
      <p>It's time to review...</p>
    </div>

    <!-- Section Recent -->
    <h1 class="section-header">
      <span class="section-title">Recent</span>
    </h1>
    <div class="set-container">
      <SetBox v-for="set in recentSetsData.content" :key="set.id" :set="set"
        @reload="fetchAllData"
      />
    </div>

    <!-- Recent Pagination -->
    <div v-if="recentSetsData.totalPages > 0" class="pagination-container">
      <div class="pagination-info">
        Page {{ recentSetsData.number + 1 }} of {{ recentSetsData.totalPages }}
        (Total {{ recentSetsData.totalElements }} items)
      </div>
      <div class="pagination-controls">
        <button
            @click="changePage('recent', 0)"
            :disabled="recentSetsPage === 0 || isLoadingRecent"
            class="pagination-btn"
        >
          First
        </button>
        <button
            @click="changePage('recent', recentSetsPage - 1)"
            :disabled="recentSetsPage === 0 || isLoadingRecent"
            class="pagination-btn"
        >
          Previous
        </button>
        <button
            v-for="page in getPageNumbers(recentSetsData.totalPages, recentSetsPage)"
            :key="page"
            @click="changePage('recent', page)"
            :class="['pagination-btn', { active: page === recentSetsPage }]"
            :disabled="isLoadingRecent"
        >
          {{ page + 1 }}
        </button>
        <button
            @click="changePage('recent', recentSetsPage + 1)"
            :disabled="recentSetsPage + 1 >= recentSetsData.totalPages || isLoadingRecent"
            class="pagination-btn"
        >
          Next
        </button>
        <button
            @click="changePage('recent', recentSetsData.totalPages - 1)"
            :disabled="recentSetsPage + 1 >= recentSetsData.totalPages || isLoadingRecent"
            class="pagination-btn"
        >
          Last
        </button>
      </div>
      <div v-if="isLoadingRecent" class="loading">Loading...</div>
    </div>

    <!-- Section Library -->
    <h1 class="section-header">
      <span class="section-title-library" @click="goToLibrary">Your Library</span>
      <span v-if="sets.length > 3" class="more-link" @click="goToLibrary">More...</span>
    </h1>
    <div class="set-container">
      <SetBox v-for="set in sets" :key="set.id" :set="set"
              @reload = "fetchAllData"
      />
    </div>

    <!-- Section Community -->
    <h1 class="section-header">
      <span class="section-title">Community</span>
    </h1>
    <div class="set-container">
      <SetBox v-for="set in publicSetsData.content" :key="set.id" :set="set"
              @reload = "fetchAllData"
      />
    </div>

    <!-- Public Sets Pagination -->
    <div v-if="publicSetsData.totalPages > 0" class="pagination-container">
      <div class="pagination-info">
        Page {{ publicSetsData.number + 1 }} of {{ publicSetsData.totalPages }}
        (Total {{ publicSetsData.totalElements }} items)
      </div>
      <div class="pagination-controls">
        <button
            @click="changePage('public', 0)"
            :disabled="publicSetsPage === 0 || isLoadingPublic"
            class="pagination-btn"
        >
          First
        </button>
        <button
            @click="changePage('public', publicSetsPage - 1)"
            :disabled="publicSetsPage === 0 || isLoadingPublic"
            class="pagination-btn"
        >
          Previous
        </button>
        <button
            v-for="page in getPageNumbers(publicSetsData.totalPages, publicSetsPage)"
            :key="page"
            @click="changePage('public', page)"
            :class="['pagination-btn', { active: page === publicSetsPage }]"
            :disabled="isLoadingPublic"
        >
          {{ page + 1 }}
        </button>
        <button
            @click="changePage('public', publicSetsPage + 1)"
            :disabled="publicSetsPage + 1 >= publicSetsData.totalPages || isLoadingPublic"
            class="pagination-btn"
        >
          Next
        </button>
        <button
            @click="changePage('public', publicSetsData.totalPages - 1)"
            :disabled="publicSetsPage + 1 >= publicSetsData.totalPages || isLoadingPublic"
            class="pagination-btn"
        >
          Last
        </button>
      </div>
      <div v-if="isLoadingPublic" class="loading">Loading...</div>
    </div>
  </div>
</template>

<style scoped>
.home {
  padding: 50px;
  margin-left: 20px;
  margin-right: 30px;
}

.review-box {
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #dff1f9;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 20px;
  margin-top: 30px;
  cursor: pointer;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  font-size: 2.2rem;
  font-weight: bold;
  color: #333;
  text-align: center;
  transition: background-color 0.3s;
}

.review-box:hover {
  background-color: #cce7f0;
}

.section-title,
.section-title-library {
  font-weight: bold;
  margin-bottom: 20px;
  cursor: pointer;
}

.section-title-library:hover {
  transform: scale(1.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.set-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.set-container .SetBox {
  flex: 1 1 calc(20% - 20px);
  box-sizing: border-box;
  height: 350px;
}

.more-link {
  font-size: 0.9rem;
  color: #666;
  cursor: pointer;
  transition: color 0.3s, transform 0.2s;
}

.more-link:hover {
  color: #333;
  transform: scale(1.05);
}

.pagination-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 20px;
}

.pagination-info {
  margin-bottom: 10px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #333;
}

.pagination-controls {
  display: flex;
  gap: 8px;
}

.pagination-btn {
  font-size: 0.9rem;
  color: #666;
  background: none;
  border: none;
  cursor: pointer;
  transition: color 0.3s, transform 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  color: #333;
  transform: scale(1.05);
}

.pagination-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.pagination-btn.active {
  color: #333;
  font-weight: bold;
  text-decoration: underline;
}

.loading {
  margin-top: 10px;
  font-size: 0.9rem;
  color: #666;
}
</style>