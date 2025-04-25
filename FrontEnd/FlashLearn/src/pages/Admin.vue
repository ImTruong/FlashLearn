<script setup>
    import ClassModal from '@/components/ClassModal.vue'
    import SetBox from "@/components/SetBox.vue"
    import ClassTable from '@/components/ClassTable.vue';
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


    const showClassModal = (classItem) => {
          classModalMode.value = true;
        localStorage.setItem('classId', classItem.classId);
        localStorage.setItem('className', classItem.className);
    }

    function closeOverlay() {
        emit('close');
        isMember.value = false;
    }

    const switchTab = () =>{
        if(activeTab.value == "Flashcard sets"){
            activeTab.value = "Classes";
        }
        else{
            activeTab.value = "Flashcard sets"
        }
    }


</script>

<template>
    <main>
        <div class="tabs">
          <button :class="{ active: activeTab == 'Flashcard sets' }" @click="switchTab">Flashcard sets</button>
          <button :class="{ active: activeTab == 'Classes' }" @click="switchTab">Classes</button>
        </div>
        <div class="line"></div>
        <div class="sets-list"  v-if="activeTab == 'Flashcard sets'">
            <div class="sets-container">
                <div v-for="set in sets" :key="set.id" class="card-wrapper">
                    <SetBox :set="set" />
                </div>
            </div>
        </div>
        <div class="class-list"  v-if="activeTab == 'Classes'">
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
        left: 0px;
        width: 100%;
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
        /* position: absolute; */
        display: flex;
        bottom: 0;
        left: 0;
        right: 0;
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
        /* position: relative; */
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

