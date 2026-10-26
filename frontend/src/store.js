import Vue from 'vue'
import Vuex from 'vuex'
import VuexPersistence from 'vuex-persist'
Vue.use(Vuex)
const vuexLocal = new VuexPersistence({
    storage: window.localStorage
})
export default new Vuex.Store({
    state: {
        isShow: false, //菜单展示
        focusId: "", //聚焦菜单
        menuId:"",//菜单id
        postData: {}, //列表筛选项
        breadData: [], //面包屑导航信息
    },
    mutations: {
        changeInfo(state, payload) {
            state.isShow = payload.isShow;
        },
        focusId(state, payload) {
            state.focusId = payload.focusId;
        },        
        menuId(state, payload) {
            state.menuId = payload.menuId;
        },
        savePostData(state, payload) {
            state.postData = payload.postData;
        },
        setBreadData(state, payload) {
            state.breadData = payload.breadData;
        }
    },
    actions: {
        
    },

    plugins: [vuexLocal.plugin]
})