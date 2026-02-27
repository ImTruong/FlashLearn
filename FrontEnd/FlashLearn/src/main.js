import './assets/main.css'
import { createApp } from 'vue'
import axios from 'axios'
import App from './App.vue'
import store from "./store";
import router from "./router/route";

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
axios.defaults.baseURL = apiBaseUrl


const app= createApp(App)
app.use(store)
app.use(router)
app.mount('#app')

