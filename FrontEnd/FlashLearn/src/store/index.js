import { createStore } from "vuex";
import userModule from "./modules/userModule";
import classModule from "./modules/classModule";
import setModule from "./modules/setModule";

const store = createStore({
    modules: {
        userModule,
        classModule,
        setModule
    }
});

export default store;
