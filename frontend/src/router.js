import Vue from "vue";
import Router from "vue-router";
import store from "./store";
import util from "@/utils/util";
import authRouter from "@/router/authRouter.js";//权限管理
import routes from "@/router/generateRouter.js";//动态路由
import {
  aes
} from "./utils/crypto";

Vue.use(Router);

// var routes = [
//     //无权限
//     {
//       path: "/noAuthority",
//       name: "Authority",
//       //通过import引入，这样会所有组件一次全加载
//       //component: Home
//       //按需加载 有三种方法：
//       //第一：vue异步组件技术 ==== 异步加载
//       //vue-router配置路由 , 使用vue的异步组件技术 , 可以实现按需加载 .
//       //但是,这种情况下一个组件生成一个js文件
//       //component: resolve => require(['@/views/Home.vue'], resolve)
//       //第二:路由懒加载(使用import)，es提案的import()，指定了相同的webpackChunkName，会合并打包成一个js文件
//       // component: r => require.ensure([], () => r(require('@/views/Home')), 'demo')
//       //第三：webpack的require,ensure()
//       component: () =>
//         import(
//           /*webpackChunkName: "authority" */
//           "@/views/commons/NoAuthority.vue"
//         ),
//       meta: {
//         title: "无权限"
//       }
//     },
//     //session失效
//     {
//       path: "/sessionFail",
//       name: "SessionFail",
//       component: () =>
//         import(
//           /*webpackChunkName: "authority" */
//           "@/views/commons/SessionFail.vue"
//         ),
//       meta: {
//         title: "登录失效"
//       }
//     },
//     //首页//首页
//   {
//     path: "/",
//     name: "MainPage",
//     component: () =>
//       import(
//         /*webpackChunkName: "MainPage" */
//         "@/views/mainPage/MainPage.vue"
//       ),
//     meta: {
//       title: "首页"
//     },
//     //redirect:'/lineSafe',
//     children: routers,
//     beforeEnter: (to, from, next) => {
//       if (to.query.token) {
//         checkLogin(to, next);
//        } else if (!to.query.token) {
//         if (to.path == '/') {
//           next('*');
//         } else {
//           next();
//         }
//       }
//     }
//   },
//   {
//     path: '/404',
//     name: '404',
//     component: () => import('@/views/commons/404.vue')
//   },
//   {
//     path: '*', // 匹配所有路由
//     redirect: '/404'
//   }
//   ];
// var routers = routes.concat(authRouter);
console.log(routes);
export default new Router({
  mode: "history",
  //base: process.env.BASE_URL,
  routes: routes//routers
});
function checkLogin(to, next) {
  //后台重定向后，通过token返回权限信息
  //是否是第一次登陆
  localStorage.setItem("token", to.query.token);
  window.axios
    .post(process.env.VUE_APP_CONFIG + "/menu/queryUserMenuList")
    .then(res => {
      let data = res.data;
      if (data.status == "success") {
        //设置域账号
        localStorage.setItem("piwikCode", data.result.oprid);  
        localStorage.setItem("oprid", data.result.oprid);//域账号
        localStorage.setItem("emplName", data.result.emplName);//姓名 
        //无权限
        if (!data.result) {
          next("/noAuthority?title=" + to.meta.title);
          return false;
        }
        //加密信息处理
        // localStorage.setItem("pubkey", to.query.key);
        // let enCodeKey = generateKeySm4(),
        //   pubkey = to.query.key;
        // //根据后台返回的公钥进行加密
        // const sm2 = require("sm-crypto").sm2;
        // let encryptData = "04" + sm2.doEncrypt(enCodeKey, pubkey);
        // //权限，根据后台返回的token进行设置
        // localStorage.setItem("headCode", encryptData);
        // //信息加密--前端加密
        // localStorage.setItem("enCodeKey", enCodeKey);
        //菜单信息
        data.result.list = menuFun(data.result.menuList, "L38").menus;
        //后台返回的按钮信息
        let butData = butFun(data.result.operateList);
        let menuData = {
          menu: data.result.list,//菜单信息
          menuId:menuFun(data.result.menuList, "L38").menuId,//菜单id
          butData:butData//按钮
        };
        localStorage.setItem("menuData", JSON.stringify(menuData));//菜单信息
        if (data.result.list.length > 0) {
          //是否有共享的角色,有调年度，无调单位
          let currLinkTo = "/renewalManagement/newSigning";
          if (data.result.hasShareRole == 'N') {
            currLinkTo = "/renewalManagement/annualInformation";
          }
          next(currLinkTo);
        } else {
          next("/noAuthority?title=" + to.meta.title);
        }
      } else {
        next("/noAuthority?title=" + to.meta.title);
      }
    })
    .catch(error => {
      console.log(error);
      next("/noAuthority?title=" + to.meta.title);
    });
}
function menuFun(menuData,modeType) {
  //菜单数据处理
  let data = [],menuId=[];
  menuData.forEach(function (value, key) {
    if (value.parentSysMenuCode== modeType) {
      menuId.push(value.sysMenuCode);
      //有下一级菜单
      let temp = menuData.filter(function (item) {
        return item.parentSysMenuCode === value.sysMenuCode;
      });

      let nodes = [];
      temp.forEach(function (i, j) {
        if (i.isLeaf == "N") {
          i.children = menuNext(i, menuData,menuId).nodes;
          menuId=menuNext(i, menuData,menuId).menuId;
        }
        menuId.push(i.sysMenuCode);
        nodes.push({
          id: i.sysMenuCode,
          pid: i.parentSysMenuCode,
          name: i.menuName,
          linkTo: i.menuUrl,
          imgIcon: i.menuIcon || '',
          isCheck: false,
          isOpen: false
        });
      });
      data.push({
        id: value.sysMenuCode,
        pid: null,
        name: value.menuName,
        linkTo: value.menuUrl||'',
        imgIcon: value.menuIcon || '',
        isCheck: false,
        isOpen: false,
        children: nodes
      });
    }
  });
  return {menus:data,menuId:menuId};
}
function menuNext(i, menuData,menuId) {
  let tempNextT = menuData.filter(function (item) {
    return item.parentSysMenuCode === i.sysMenuCode;
  });
  let nodesNextT = [];
  tempNextT.forEach(function (p, q) {
    if (p.isLeaf == "N") {
      p.children = menuNext(p, menuData,menuId).nodes;
      menuId=menuNext(p, menuData,menuId).menuId;
    }
    menuId.push(p.sysMenuCode);
    nodesNextT.push({
      id: p.sysMenuCode,
      pid: p.parentSysMenuCode,
      name: p.menuName,
      linkTo: p.menuUrl,
      imgIcon: p.menuIcon || '',
      isCheck: false,
      isOpen: false
    });
  });
  return {nodes:nodesNextT,menuId:menuId};
}
//按钮处理
function butFun(butData) {
  let data = [];
  butData.forEach(function (value, key) {
    if (value)
      data.push(value.operateCode);
  });
  return data;
}