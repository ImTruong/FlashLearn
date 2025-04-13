<script setup>
  import Header from '@/components/Header.vue';
  import { ref, onMounted } from 'vue';
  import { getStudySessionByTime, getStudySessionByWord, getStudySessionBySpecificWord, getStudySessionBySpecificTime} from "@/apis/studyApi.js";

  const activeTab = ref('By Time');



  let data = ref([]);
  const singleItemMode = ref(false);
  const singleItemHeader = ref('');
  const token = localStorage.getItem('token');

  const fetchStatisticByTime = async () => {
    try {
      const response = await getStudySessionByTime(token);
      data.value = response;
    }
    catch (error) {
      console.error("Error fetching statistics:", error);
      alert(error)
    }
  };

  const fetchStatisticByWord = async () => {
    try {
      const response = await getStudySessionByWord(token);
      data.value = response;
    }
    catch (error) {
      console.error("Error fetching statistics:", error);
      alert(error)
    }
  };

  const fetchStatisticBySpecificWord = async (wordId) => {
    try {
      const response = await getStudySessionBySpecificWord(token,wordId);
      data.value = response;
    } catch (error) {
      console.error("Error fetching statistics:", error);
      alert(error)
    }
  };

  const fetchStatisticBySpecificTime = async (time) => {
    try {
      const response = await getStudySessionBySpecificTime(token,time);
      const cleanedResponse = response.map(item => ({
        ...item,
        time: item.time.split('.')[0]
      }));

      data.value = cleanedResponse;
    } catch (error) {
      console.error("Error fetching statistics:", error);
      alert(error)
    }
  };

  onMounted(() => {
    fetchStatisticByTime();
  });

  const switchTab = () =>{
    if(activeTab.value == "By Word"){
      activeTab.value = "By Time";
      fetchStatisticByTime();
    }
    else{
      activeTab.value = "By Word";
      fetchStatisticByWord();
    }
  }

  function handleSelectAnItem(item){
    console.log(item)
    singleItemMode.value = true;
    if (activeTab.value == 'By Time') {
      singleItemHeader.value = item.date;
      fetchStatisticBySpecificTime(item.date);
    } else {
      singleItemHeader.value = item.word;
      fetchStatisticBySpecificWord(item.wordId);
    }
  }

  function gettingOutOfSingleItemMode(){
    singleItemMode.value = false;
    switchTab();
    switchTab();
  }

</script>

<template>
  <Header></Header>
  <div class="container">
    <div class="header">
      <h1 @click.stop="gettingOutOfSingleItemMode" >Statistic</h1>
    </div>
    <div class="tabs">
      <button v-if="!singleItemMode" :class="{ active: activeTab == 'By Time' }" @click="switchTab">By Time</button>
      <button v-if="!singleItemMode" :class="{ active: activeTab == 'By Word' }" @click="switchTab">By Word</button>
      <h2 v-if="singleItemMode" class="singleItemHeader">{{ singleItemHeader }}</h2>
    </div>
    <div class="line"></div>
    <div v-if="activeTab == 'By Time'&&!singleItemMode" v-for="d in data"  class="details" @click.stop="handleSelectAnItem(d)">
      <tr>
        <td>
          <div class="point"></div>
        </td>
        <td class="date">{{ d.date }}</td>
        <td class="words">{{ d.numberOfWords }} words</td>
        <td >
          <div class="bar" :style="{width: d.numberOfWords * 5 + 'px'}" > </div>
        </td>
      </tr>
    </div>
    <div v-if="activeTab == 'By Word'&&!singleItemMode" v-for="d in data"  class="details" @click.stop="handleSelectAnItem(d)">
      <tr>
        <td>
          <div class="point"></div>
        </td>
        <td class="hidden" >{{ d.wordId }}</td>
        <td class="date">{{ d.word }}</td>
        <td class="words">{{ d.count }} Time</td>
        <td >
          <div class="bar" :style="{width: d.count * 5 + 'px'}" > </div>
        </td>
      </tr>
    </div>
    <div v-if="activeTab == 'By Word'&&singleItemMode" v-for="d in data"  class="details" >
      <tr>
        <td>
          <div class="point"></div>
        </td>
        <td class="hidden" >{{ d.wordId }}</td>
        <td class="date">{{ d.word }}</td>
        <td class="words">{{ d.date }} </td>
        <td >
          <div class="words"> {{ d.choosenAnswer }} </div>
        </td>
      </tr>
    </div>
    <div v-if="activeTab == 'By Time'&&singleItemMode" v-for="d in data"  class="details" >
      <tr>
        <td>
          <div class="point"></div>
        </td>
        <td class="date">{{ d.time }}</td>
        <td class="words">{{ d.word }} </td>
        <td >
          <div class="words"> {{ d.choosenAnswer }} </div>
        </td>
      </tr>
    </div>
  </div>
</template>

<style scoped>
  .container {
    margin-top: 80px;
    margin-left: 100px;
  }
  .header{
    position: relative;
    margin-bottom: 30px;
  }

  .details{
    display: flex;
    margin-left: 30px;
    cursor: pointer;
  }

  tr{
    height: 50px;
  }

  .date{
    width: 150px;
    font-size: 20px;
    text-align: center;
  }
  .words{
    width: 150px;
    font-size: 20px;
  }
  .point{
    height: 10px;
    width: 10px;
    background-color: #BDEDF5;
    border-radius: 50%;
  }
  .bar{
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

  .header h1{
    cursor: pointer;
  }

  .singleItemHeader{
    flex-grow: 1;
    text-align: center;
  }

  .hidden{
    display: none;
  }
</style>