/*--权限管理--*/ 
var commonRouter = [{
  //权限管理--用户管理
  path: "user",
  name: "User",
  meta: {
    title: "权限管理"
  },
  component: () =>
    import( /*webpackChunkName: "auth"*/ "@/views/authManage/UserManage.vue")
},
{
  //权限管理
  path: "role",
  name: "Role",
  meta: {
    title: "权限管理"
  },
  component: () =>
    import( /*webpackChunkName: "auth"*/ "@/views/authManage/RoleManage.vue")
},
{
  //权限管理
  path: "reports",
  name: "Reports",
  meta: {
    title: "权限管理"
  },
  component: () =>
    import( /*webpackChunkName: "auth"*/ "@/views/authManage/Reports.vue")
},
{
  //权限管理
  path: "logs",
  name: "Logs",
  meta: {
    title: "权限管理"
  },
  component: () =>
    import( /*webpackChunkName: "auth"*/ "@/views/authManage/Logs.vue")
}
],authRouter={
  //权限管理
  path: "/authInfo",
  name: "AuthManage",
  component: () =>
    import(
      /*webpackChunkName: "join" */
      "@/views/authManage/AuthManage.vue"
    ),
  meta: {
    title: "权限管理"
  },
  children: commonRouter,
  beforeEnter: (to, from, next) => {
    next();
  }
};
export default authRouter;