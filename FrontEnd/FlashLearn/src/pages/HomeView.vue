<script setup>
import { ref, onMounted, computed } from "vue";
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
const isGuestMode = ref(false); // Thêm trạng thái chế độ khách

// Kiểm tra token và đặt chế độ khách nếu cần
const checkAuthStatus = () => {
  const token = localStorage.getItem("token");
  if (!token) {
    isGuestMode.value = true;
    console.log("Guest mode activated: No token found");
  }
  return token;
};

const fetchUserInfo = async (token) => {
  try {
    const user = await getCurrentUser(token);
    localStorage.setItem("user", JSON.stringify(user));
    isGuestMode.value = false; // Đặt lại chế độ khách khi lấy thông tin user thành công
  } catch (error) {
    console.error("Error fetching user info:", error);
    isGuestMode.value = true; // Kích hoạt chế độ khách khi có lỗi
    console.log("Guest mode activated: User API error");
  }
};

const fetchRecentSet = async (token, page) => {
  if (isGuestMode.value) return; // Không gọi API nếu đang ở chế độ khách

  try {
    isLoadingRecent.value = true;
    const response = await getRecentSet(token, page, recentSetsSize.value);
    recentSetsData.value = response;
    recentSetsPage.value = page;
  } catch (error) {
    console.error("Error fetching recent sets:", error);
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      isGuestMode.value = true; // Kích hoạt chế độ khách khi token hết hạn
      console.log("Guest mode activated: Recent sets API unauthorized");
    }
  } finally {
    isLoadingRecent.value = false;
  }
};

const fetchPublicSet = async (token, page) => {
  try {
    isLoadingPublic.value = true;
    const response = await getAllPublicSet(page, publicSetsSize.value);
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
  if (isGuestMode.value) return; // Không gọi API nếu đang ở chế độ khách

  try {
    const response = await getLibrarySet(token, librarySetsPage.value, librarySetsSize.value);
    sets.value = response.content;
  } catch (error) {
    console.error("Error fetching library sets:", error);
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      isGuestMode.value = true; // Kích hoạt chế độ khách khi token hết hạn
      console.log("Guest mode activated: Library API unauthorized");
    }
  }
};

const fetchAllData = async () => {
  const token = checkAuthStatus();
  if (!token) {
    // Nếu không có token, chỉ tải dữ liệu public
    await fetchPublicSet(null, publicSetsPage.value);
    return;
  }

  try {
    await Promise.all([
      fetchUserInfo(token),
      fetchPublicSet(token, publicSetsPage.value)
    ]);

    // Chỉ tải dữ liệu riêng tư nếu không ở chế độ khách
    if (!isGuestMode.value) {
      await Promise.all([
        fetchLibrarySet(token),
        fetchRecentSet(token, recentSetsPage.value)
      ]);
    }
  } catch (error) {
    console.error("Error in fetchAllData:", error);
    // Nếu có lỗi chung, đặt chế độ khách
    isGuestMode.value = true;
    console.log("Guest mode activated: General error in data fetching");
    // Đảm bảo vẫn tải dữ liệu công cộng
    await fetchPublicSet(null, publicSetsPage.value);
  }
};

// Tính toán message hiển thị cho khách
const guestMessage = computed(() => {
  return isGuestMode.value ? "You are browsing as a guest. Sign in to access all features." : "";
});

onMounted(() => {
  fetchAllData();
});

const goToStudy = () => {
  if (isGuestMode.value) {
    router.push("/login");
    return;
  }
  router.push("/review");
};

const goToLibrary = () => {
  if (isGuestMode.value) {
    router.push("/login");
    return;
  }
  localStorage.setItem("libraryTab", "FlashSetBox sets");
  router.push("/library");
};

// Hàm xử lý chuyển trang
const changePage = async (type, page) => {
  if (page < 0 || page >= (type === "recent" ? recentSetsData.value.totalPages : publicSetsData.value.totalPages)) return;

  const token = localStorage.getItem("token");
  if (type === "recent" && !isGuestMode.value) {
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
      :recentSets="isGuestMode ? [] : recentSetsData.content"
      :ownerSets="isGuestMode ? [] : sets"
      :publicSets="publicSetsData.content"
      :isGuestMode="isGuestMode"
      @reload="fetchAllData"
  />
  <div class="home">
    <!-- Guest mode message -->
    <div v-if="isGuestMode" class="guest-message">
      <p>{{ guestMessage }}</p>
      <div class="guest-buttons">
        <button @click="router.push('/login')" class="login-btn">Login</button>
        <button @click="router.push('/signup')" class="register-btn">Register</button>
      </div>
    </div>

    <!-- Review box - visible only for logged-in users -->
    <div v-if="!isGuestMode" class="review-box" @click="goToStudy">
      <p>It's time to review...</p>
    </div>

    <!-- Section Recent - visible only for logged-in users -->
    <div v-if="!isGuestMode">
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
    </div>

    <!-- Section Library - visible only for logged-in users -->
    <div v-if="!isGuestMode">
      <h1 class="section-header">
        <span class="section-title-library" @click="goToLibrary">Your Library</span>
        <span v-if="sets.length > 3" class="more-link" @click="goToLibrary">More...</span>
      </h1>
      <div class="set-container">
        <SetBox v-for="set in sets" :key="set.id" :set="set"
                @reload="fetchAllData"
        />
      </div>
    </div>

    <!-- Section Community - visible for all users -->
    <h1 class="section-header">
      <span class="section-title">Community</span>
    </h1>
    <div class="set-container">
      <SetBox v-for="set in publicSetsData.content" :key="set.id" :set="set"
              @reload="fetchAllData"
              :isGuestMode="isGuestMode"
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

.guest-message {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  margin-top: 30px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.guest-message p {
  font-size: 1.2rem;
  margin-bottom: 15px;
  color: #555;
}

.guest-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.login-btn, .register-btn {
  padding: 8px 20px;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.login-btn {
  background-color: #4a6fa5;
  color: white;
  border: none;
}

.login-btn:hover {
  background-color: #3a5a8c;
}

.register-btn {
  background-color: white;
  color: #4a6fa5;
  border: 1px solid #4a6fa5;
}

.register-btn:hover {
  background-color: #f0f5fb;
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