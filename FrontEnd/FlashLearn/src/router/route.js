import {createRouter, createWebHistory} from "vue-router";
import App from "@/App.vue";
import Login from "@/pages/Login.vue";
import SignUp from "@/pages/SignUp.vue";
import User from "@/pages/User.vue";


const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes:[
        {
            path: "/login",
            name: "login",
            component: Login
        },
        {
            path: "/signup",
            name: "signup",
            component: SignUp
        },
        {
            path: "/profile",
            name: "profile",
            component: User
        }
    ]
})

export default router