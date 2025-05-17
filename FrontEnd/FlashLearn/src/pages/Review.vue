<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import Header from "../components/Header.vue";
import { useRouter } from 'vue-router';
import { userRemindWord } from '@/apis/wordApi';
import { createStudySession } from "@/apis/studyApi.js";

// Constants
const BATCH_SIZE = 20; // Số lượng thẻ tải mỗi lần
const LOAD_THRESHOLD = 5; // Khi còn bao nhiêu thẻ thì tải thêm

// Reactive state
const cards = ref([]);
const currentCardIndex = ref(0);
const isFlipped = ref(false);
const isCompleted = ref(false);
const isLoading = ref(true);
const loadingMore = ref(false);
const hasMoreCards = ref(true);
const totalCardCount = ref(0);
const currentPage = ref(0);

// Router và token
const router = useRouter();
const token = localStorage.getItem('token');

// Thông tin thẻ hiện tại
const currentCard = computed(() => cards.value[currentCardIndex.value] || {});

// Hiển thị tiến độ
const cardStatus = computed(() => {
  if (totalCardCount.value > 0) {
    return `${currentCardIndex.value + 1}/${totalCardCount.value || '?'}`;
  }
  return '0/0';
});

// Kiểm tra xem có cần tải thêm thẻ không
const shouldLoadMore = computed(() => {
  return hasMoreCards.value &&
      cards.value.length - currentCardIndex.value <= LOAD_THRESHOLD &&
      !loadingMore.value;
});

// Lấy dữ liệu thẻ từ API
const fetchCards = async (page = 0, append = false) => {
  try {
    loadingMore.value = true;
    const response = await userRemindWord(token, page, BATCH_SIZE);
    const cardsData = response.content || response; // Hỗ trợ cả hai kiểu dữ liệu trả về

    // Lấy tổng số phần tử nếu API trả về
    if (response.totalElements !== undefined) {
      totalCardCount.value = response.totalElements;
    }

    // Kiểm tra còn trang tiếp theo không
    const noMoreData = response.hasNext === false || (cardsData && cardsData.length < BATCH_SIZE);
    if (noMoreData) {
      hasMoreCards.value = false;

      // Nếu không biết tổng số thẻ, tính từ số đã tải
      if (response.totalElements === undefined) {
        totalCardCount.value = (append ? cards.value.length : 0) + cardsData.length;
      }
    } else {
      hasMoreCards.value = true;
    }

    if (append) {
      cards.value = [...cards.value, ...cardsData];
    } else {
      cards.value = cardsData;
      // Khôi phục tiến độ học nếu có lưu trong localStorage
      restoreProgress();
    }

    currentPage.value = page;

    // Debug info
    console.log(`Loaded page ${page}, items: ${cardsData.length}, hasMore: ${hasMoreCards.value}`);

  } catch (error) {
    console.error("Error fetching flashcards:", error);
    alert("Failed to load flashcards. Please try again.");
  } finally {
    isLoading.value = false;
    loadingMore.value = false;
  }
};

// Theo dõi khi cần tải thêm thẻ
watch(shouldLoadMore, (shouldLoad) => {
  if (shouldLoad) {
    fetchCards(currentPage.value + 1, true);
  }
});

// Lưu tiến độ hiện tại vào localStorage
const saveProgress = () => {
  localStorage.setItem('flashcard-progress', JSON.stringify({
    currentIndex: currentCardIndex.value,
    totalSeen: cards.value.length,
    lastUpdate: new Date().toISOString()
  }));
};

// Khôi phục tiến độ từ localStorage
const restoreProgress = () => {
  const savedProgress = localStorage.getItem('flashcard-progress');
  if (savedProgress) {
    const progress = JSON.parse(savedProgress);
    const lastUpdate = new Date(progress.lastUpdate);
    const now = new Date();
    const hoursDiff = (now - lastUpdate) / (1000 * 60 * 60);

    // Chỉ khôi phục nếu tiến độ được lưu trong 24h gần đây
    if (hoursDiff < 24) {
      currentCardIndex.value = progress.currentIndex;
    } else {
      // Xóa tiến độ cũ nếu quá lâu
      localStorage.removeItem('flashcard-progress');
    }
  }
};

// Chuyển sang thẻ tiếp theo
const nextCard = () => {
  // Nếu đã ở thẻ cuối cùng VÀ không còn thẻ nào để tải nữa
  if (currentCardIndex.value + 1 >= cards.value.length && !hasMoreCards.value) {
    isCompleted.value = true;  // Đã hoàn thành tất cả thẻ
  } else if (currentCardIndex.value + 1 >= cards.value.length && hasMoreCards.value) {
    // Nếu đã hết thẻ trong lô hiện tại nhưng vẫn còn thẻ để tải
    // Chờ tải thêm thẻ mới rồi mới tiếp tục
    loadMoreCards().then(() => {
      isFlipped.value = false;
      currentCardIndex.value += 1;
      saveProgress();
    });
  } else {
    // Trường hợp bình thường: vẫn còn thẻ trong lô hiện tại
    isFlipped.value = false;
    currentCardIndex.value += 1;
    saveProgress();
  }
};

// Tách hàm tải thêm thẻ để có thể gọi trực tiếp
const loadMoreCards = async () => {
  if (!hasMoreCards.value || loadingMore.value) return;

  try {
    loadingMore.value = true;
    const nextPage = currentPage.value + 1;
    const response = await userRemindWord(token, nextPage, BATCH_SIZE);
    const cardsData = response.content || [];
    const totalElements = response.totalElements;

    if (totalElements !== undefined) {
      totalCardCount.value = totalElements;
    }

    if (!response.hasNext || cardsData.length < BATCH_SIZE) {
      hasMoreCards.value = false;

      if (totalElements === undefined) {
        totalCardCount.value = cards.value.length + cardsData.length;
      }
    }

    cards.value = [...cards.value, ...cardsData];
    currentPage.value = nextPage;
  } catch (error) {
    console.error("Error fetching more flashcards:", error);
  } finally {
    loadingMore.value = false;
  }
};

// Lật thẻ
const toggleFlip = () => {
  isFlipped.value = !isFlipped.value;
};

// Phát âm thanh
const playAudio = (event) => {
  event.stopPropagation();
  if (currentCard.value?.audio) {
    const audio = new Audio(currentCard.value.audio);
    audio.play();
  }
};

// Gửi đánh giá và chuyển sang thẻ tiếp theo
const submitRating = async (rating) => {
  try {
    const studySessionData = {
      wordId: currentCard.value.id,
      difficulty: rating
    };
    await createStudySession(token, studySessionData);
  } catch (error) {
    console.error("Error creating study session:", error);
  }
  nextCard();
};

// Khi hoàn thành, quay về trang chủ
const handleComplete = () => {
  localStorage.removeItem('flashcard-progress');
  router.push('/');
};

// Khởi tạo
onMounted(() => {
  if (!token) {
    alert('Login to use this feature');
    window.location.href = '/login';
    return;
  }
  fetchCards();
});
</script>

<template>
  <Header></Header>

  <div class="flashcard-container" v-if="cards.length > 0">
    <div class="flashcard-header">
      <h2>Review</h2>
      <span>{{ cardStatus }}</span>
      <span v-if="loadingMore" class="loading-text">(Loading more...)</span>
    </div>

    <div class="flashcard-content" @click="toggleFlip">
      <div class="flashcard">
        <div v-if="!isFlipped" class="flashcard-front">
          <img v-if="currentCard?.image" :src="currentCard.image" alt="Flashcard Image" />
          <p v-if="currentCard?.image">{{ currentCard.word }}</p>
          <h1 v-else>{{ currentCard.word }}</h1>
        </div>
        <div v-else class="flashcard-back">
          <p class="ipa">{{ currentCard.ipa }}</p>
          <p class="definition">{{ currentCard.definition }}</p>
          <div v-if="currentCard.audio" class="audio-icon" @click.stop="playAudio">
            <img src="../assets/speaker-icon.svg" alt="Speaker Icon" />
          </div>
          <p class="example">{{ currentCard.example }}</p>
        </div>
      </div>
    </div>

    <!-- Debug info -->
    <div class="debug-info" v-if="false">
      <p>Cards: {{ cards.length }}, Current: {{ currentCardIndex + 1 }}, HasMore: {{ hasMoreCards }}</p>
      <p>Page: {{ currentPage }}, Loading: {{ loadingMore }}</p>
    </div>

    <div class="flashcard-rating">
      <button @click="submitRating('Very Difficult')" class="rating-btn">Very Difficult</button>
      <button @click="submitRating('Difficult')" class="rating-btn">Difficult</button>
      <button @click="submitRating('Easy')" class="rating-btn">Easy</button>
      <button @click="submitRating('Very Easy')" class="rating-btn">Very Easy</button>
    </div>
  </div>

  <!-- Loading state -->
  <div v-else-if="isLoading" class="loading-container">
    <div class="spinner"></div>
    <p>Loading flashcards...</p>
  </div>

  <!-- No cards available -->
  <div v-else class="alert no-cards">
    <div class="message">
      <p>No cards available</p>
      <button @click="handleComplete">Go to Home</button>
    </div>
  </div>

  <!-- Completion dialog -->
  <div class="alert completed" v-if="isCompleted">
    <div class="message">
      <p>Completed!</p>
      <button @click="handleComplete">Go to Home</button>
    </div>
  </div>
</template>

<style scoped>
.flashcard-container {
  width: 600px;
  margin: 40px auto;
  padding: 20px;
  text-align: center;
}

.flashcard-header {
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  font-size: 1.2em;
  position: relative;
}

.loading-text {
  color: #69cee0;
  font-size: 0.8em;
  margin-top: 5px;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { opacity: 0.6; }
  50% { opacity: 1; }
  100% { opacity: 0.6; }
}

.debug-info {
  background-color: #f5f5f5;
  padding: 10px;
  margin: 10px 0;
  border-radius: 5px;
  font-size: 0.8em;
  text-align: left;
}

.flashcard-content {
  width: 100%;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
  background-color: #f9f9f9;
  border: none;
  cursor: pointer;
  perspective: 1000px; /* Tạo hiệu ứng 3D */
}

.flashcard-front {
  width: 100%;
  max-width: 350px;
  min-height: 200px;
  margin: 0 auto;
  text-align: center;
  align-content: center;
}

.flashcard img {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
  border-radius: 8px;
}

.flashcard p {
  margin: 20px;
  font-size: 20px;
}

.flashcard-rating {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  gap: 10px;
}

.rating-btn {
  background-color: #DBF3F7;
  border: 1px solid #d3d3d3;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  padding: 10px 0;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
  width: 100%;
  text-align: center;
  font-size: 1em;
}

.rating-btn:hover {
  background-color: #91e3df;
}

.flashcard-back {
  max-width: 500px;
  min-height: 200px;
  align-content: center;
  text-align: center;
}

.flashcard-back .ipa {
  font-size: 20px;
}

.flashcard-back img {
  height: 25px;
  width: 30px;
}

.flashcard .definition {
  font-size: 30px;
}

.audio-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 40px;
  height: 40px;
  margin: 0 auto;
  cursor: pointer;
}

.flashcard .example {
  font-size: 20px;
}

.alert {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
}

.alert .message {
  background-color: white;
  padding: 40px 50px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
}

.alert .message p {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 20px;
}

.alert .message button {
  padding: 15px 30px;
  background-color: #BDEDF5;
  color: rgb(0, 0, 0);
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 18px;
}

.alert .message button:hover {
  background-color: #91e3df;
}

/* Tùy chỉnh cho thông báo "No cards available" */
.no-cards .message p {
  color: #e66355; /* Màu đỏ */
}

/* Tùy chỉnh cho thông báo "Completed" */
.completed .message p {
  color: #69cee0; /* Màu xanh */
}

/* Loading spinner */
.loading-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border-left-color: #69cee0;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>