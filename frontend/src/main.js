import 'babel-polyfill'
//import "lib-flexible-computer";//分辨率调整
import Vue from 'vue'
import './plugins/iview'
import './plugins/axios'
import App from './App.vue'
import router from './router'
import store from './store'
import echarts from 'echarts'
import $ from 'jquery'
import Es6Promise from 'es6-promise'
import './utils/directives'
import util from "@/utils/util";
import './assets/fonts/iconfont.css';
import "./assets/common.scss"
import './plugins/table'//vxe-table
if(process.env.NODE_ENV !== 'development'){
    import('./plugins/sentry')//sentry.js
}

import {aes} from './utils/crypto';
//懒加载
import VueLazyLoad from 'vue-lazyload'
Vue.use(VueLazyLoad,{
    error:'/assets/img/common/man.png',
    loading:'/assets/img/common/loading.gif'
})

import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

import Vtip from 'vtip'
import 'vtip/lib/index.min.css'
// 注册指令使用
Vue.use(Vtip.directive)
// 工具函数调用
Vue.prototype.$tip = Vtip.tip
//图片放大缩小
import VuePreview  from 'vue-preview'; 
Vue.use(VuePreview )

require('es6-promise').polyfill()
Es6Promise.polyfill()
Vue.config.productionTip = false
Vue.prototype.$echarts = echarts
Vue.prototype.$aes = aes

/* router.beforeEach((to, from, next) => {
  next();
  if(to.name==from.name){
      location.reload()
  }
}) */
//切换更改title
router.beforeEach((to, from, next) => {
  NProgress.start()
  let { meta } = to;
  if (meta && meta.title) document.title = meta.title; 
    next();
}); 
//路由切换，返回顶端
router.afterEach((to, from, next) => {
  NProgress.done()
      window.scrollTo(0,0)
});

(function () {
  var lastTime = 0;
  var vendors = ['ms', 'moz', 'webkit', 'o'];
  for (var x = 0; x < vendors.length && !window.requestAnimationFrame; ++x) {
      window.requestAnimationFrame = window[vendors[x] + 'RequestAnimationFrame'];
      window.cancelAnimationFrame = window[vendors[x] + 'CancelAnimationFrame'] ||
          window[vendors[x] + 'CancelRequestAnimationFrame'];
  }

  if (!window.requestAnimationFrame) {
      window.requestAnimationFrame = function (callback, element) {
          var currTime = new Date().getTime();
          var timeToCall = Math.max(0, 16 - (currTime - lastTime));
          var id = window.setTimeout(function () { callback(currTime + timeToCall); },
              timeToCall);
          lastTime = currTime + timeToCall;
          return id;
      };
  }

  if (!window.cancelAnimationFrame) {
      window.cancelAnimationFrame = function (id) {
          clearTimeout(id);
      };
  }
}());
if (!('classList' in document.documentElement)) {
  Object.defineProperty(HTMLElement.prototype, 'classList', {
      get: function () {
          var self = this;
          function update(fn) {
              return function (value) {
                  var classes = self.className.split(/\s+/g);
                  var index = classes.indexOf(value);

                  fn(classes, index, value);
                  self.className = classes.join(' ');
              };
          }

          return {
              add: update(function (classes, index, value) {
                  if (!~index) classes.push(value);
              }),

              remove: update(function (classes, index) {
                  if (~index) classes.splice(index, 1);
              }),

              toggle: update(function (classes, index, value) {
                  if (~index) { classes.splice(index, 1); } else { classes.push(value); }
              }),

              contains: function (value) {
                  return !!~self.className.split(/\s+/g).indexOf(value);
              },

              item: function (i) {
                  return self.className.split(/\s+/g)[i] || null;
              },
          };
      },
  });
}

var vm=new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app');
window.onbeforeunload = function (e) {
  vm.$destroy()
  vm.$el.remove()
}