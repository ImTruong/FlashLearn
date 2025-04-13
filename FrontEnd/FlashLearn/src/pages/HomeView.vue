<script setup>
    import { ref, computed, onMounted } from "vue";
    import Header from "@/components/Header.vue";
    import SetBox from "@/components/SetBox.vue";
    import { useRouter } from "vue-router"; 
    import { useStore } from "vuex";
    import { getCurrentUser } from "@/apis/userApi";
    import {getRecentSet, getAllPublicSet, getLibrarySet} from "@/apis/setApi";
    
    const router = useRouter();
    const sets = ref([]);
    const displayPublicSets = ref([]);
    const displayRecentSets = ref([]);
    const recentSets = ref([]);
    const publicSets = ref([]);

    const showAllSetsRecent = () => {
        displayRecentSets.value = recentSets.value;
    };

    const showAllCommunity = () => {
        displayPublicSets.value = publicSets.value;
    }
    const fetchUserInfo = async (token) => {
        const user = await getCurrentUser(token);
        localStorage.setItem('user', JSON.stringify(user));                          
    };

    const fetchRecentSet = async (token) => {
        try {
            const response = await getRecentSet(token);
            recentSets.value = response;
            displayRecentSets.value = recentSets.value.slice(0, 3);
        } catch (error) {
            alert(error)
        }
    }
    const fetchPublicSet = async (token) => {
        try {
            const response = await getAllPublicSet(token);
            publicSets.value = response;
            displayPublicSets.value = publicSets.value.slice(0, 3);
        } catch (error) {
            alert(error)
        }
    }
    onMounted(async () => {
        const token = localStorage.getItem('token');
        sets.value = await getLibrarySet(token);
        await fetchUserInfo(token);
        await fetchRecentSet(token);
        await fetchPublicSet(token);
    });
    const goToStudy = () => {
        router.push('/review');
    }

    const goToLibrary = () =>{
        console.log(localStorage.getItem('libraryTab'));
        localStorage.setItem('libraryTab', "FlashSetBox sets");
        router.push('/library');
    }
</script>

<template>
    <Header
    :recentSets="recentSets"
    :ownerSets="sets"
    :publicSets="publicSets"
     />
    <div class="home">
        <div class="review-box" @click="goToStudy">
            <p>It's time to review...</p>
        </div>
        <!-- Section Recent -->
        <h1 class="section-header">
            <span class="section-title">Recent</span>
            <span v-if="recentSets.length > 3" class="more-link" @click="showAllSetsRecent">More...</span>
        </h1>
        <div class="set-container">
            <SetBox 
                v-for="set in displayRecentSets" 
                :key="set.id" 
                :set="set" />
        </div>

        <h1 class="section-header">
            <span class="section-title-library" @click="goToLibrary">Your Library</span>
            <span v-if="sets.length > 3" class="more-link" @click="goToLibrary">More...</span>
        </h1>
        <div class="set-container">
            <SetBox 
                v-for="set in sets"
                :key="set.id" 
                :set="set" />
        </div>
        <h1 class="section-header">
            <span class="section-title">Community</span>
            <span v-if="publicSets.length > 3" class="more-link" @click="showAllCommunity">More...</span>
        </h1>
        <div class="set-container">
            <SetBox 
                v-for="set in displayPublicSets"
                :key="set.id" 
                :set="set" />
        </div>
    </div>

    
</template>

<style scoped>
    .home {
        padding: 50px;
        height: 100vh; 
        margin-left: 20px;
        margin-right: 30px;
    }
    .review-box {
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: #dff1f9; /* Màu nền */
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
        height: 40%;
    }

    .review-box:hover {
        background-color: #cce7f0; /* Màu nền khi hover */
    }
    .section-title {
        font-weight: bold;
        margin-bottom: 20px;
    }

    .section-title-library{
        font-weight: bold;
        margin-bottom: 20px;
        cursor: pointer;
    }

    .section-title-library:hover{
        transform: scale(1.05);
    }

    .section-header {
        font-weight: bold;
        margin-bottom: 20px;
        display: flex;
        justify-content:space-between;
        align-items: center;
    }

    .set-container {
        display: flex;
        flex-wrap: wrap;
        gap: 20px;
    }

    .set-container .SetBox{
        flex: 1 1 calc(20% - 20px);
        box-sizing: border-box;
        height: 350px;
    }

    .more-link {
        font-size: 0.9rem;
        color: gray;
        cursor: pointer;
        margin-left: 20px;
    }



</style>