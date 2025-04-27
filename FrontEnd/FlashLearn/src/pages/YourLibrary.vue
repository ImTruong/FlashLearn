<script setup>
  import Header from '@/components/Header.vue';
  import LibraryBody from "@/components/LibraryBody.vue";
  import { getLibrarySet } from "@/apis/setApi.js";
  import { getCurrentUserClasses } from "@/apis/classApi.js";
  import {onMounted, onUnmounted} from "vue";
  import { ref } from "vue";

  const token = localStorage.getItem('token');
  const classes = ref(null);
  const sets = ref(null);
  const page = ref(0);
  const size = ref(30);

  let intervalId = null;

  const fetchData = async () => {
    try {
      const [setsData, classesData] = await Promise.all([
        getLibrarySet(token, page.value, size.value),
        getCurrentUserClasses(token, page.value, size.value),
      ]);

      sets.value = setsData;
      classes.value = classesData;
    } catch (error) {
      console.error("Error fetching data:", error);
    }
  };

  onMounted(() => {
    fetchData(); // fetch ban đầu
    intervalId = setInterval(fetchData, 3000); // fetch lại mỗi 3s
  });

  onUnmounted(() => {
    clearInterval(intervalId); // dọn dẹp interval
  });

</script>

<template>
    <div id="app">
        <Header></Header>
        <main>
            <h2>Your library</h2>
            <LibraryBody
                :sets="sets"
                :classes="classes"
            />
        </main>
    </div>
</template>

<style scoped>
  main {
      padding: 100px;
  }

  h2 {
      font-weight: bold;
      margin-bottom: 30px;
  }
</style>
