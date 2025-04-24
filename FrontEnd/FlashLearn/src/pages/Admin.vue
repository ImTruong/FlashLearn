<script setup>
  import LibraryBody from "@/components/LibraryBody.vue";
  import { getAllSets } from "@/apis/setApi.js";
  import { getAllClasses } from "@/apis/classApi.js";
  import {onMounted, onUnmounted} from "vue";
  import { ref } from "vue";

  const classes = ref(null);
  const sets = ref(null);
  const search = ref("");
  const page = ref(0);
  const size = ref(10);

  let intervalId = null;

  const fetchData = async (token) => {
    try {
      const [setsData, classesData] = await Promise.all([
        getAllSets(token,search.value, page.value, size.value),
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
