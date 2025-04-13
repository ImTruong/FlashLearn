<script setup>
  import Header from '@/components/Header.vue';
  import LibraryBody from "@/components/LibraryBody.vue";
  import { getLibrarySet } from "@/apis/setApi.js";
  import { getCurrentUserClasses } from "@/apis/classApi.js";
  import {onMounted} from "vue";
  import { ref } from "vue";

  const token = localStorage.getItem('token');
  const classes = ref(null);
  const sets = ref(null);

  onMounted(async () => {
    try {
      const [setsData, classesData] = await Promise.all([
        getLibrarySet(token),
        getCurrentUserClasses(token),
      ]);

      sets.value = setsData;
      classes.value = classesData;
    } catch (error) {
      console.error("Error fetching data:", error);
    }
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
