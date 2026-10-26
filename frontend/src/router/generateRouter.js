//routes.js
//递归获取 views或某个文件夹下的所有.vue文件
const files = require.context('@/views/pages', true, /\.vue$/, 'lazy');//懒加载，非懒加载去掉lazy
let pages = {};
files.keys().forEach(key => {
    //懒加载
    pages[key.replace(/(\.\/|\.vue)/g, '')] = () => files(key).default;//files(key).default;非懒加载
})
//生成路由规则
let generator = [];
Object.keys(pages).forEach(item => {
    generator.push({
        path: `/pages/${pages[item].name.replace(/-/g, '/')}`,
        name: pages[item].name,
        component: pages[item]
    })
});
//合并公共路由以及重定向规则
const routes = [{
    path: '/',
    name: "MainPage",
    component: () =>
        import(
            /*webpackChunkName: "MainPage" */
            "@/views/mainPage/MainPage.vue"
        ),
    meta: {
        title: "首页"
    },
    beforeEnter: (to, from, next) => {
        next();
    }
},
...generator,
{
    path: '/404',
    name: '404',
    component: () => import('@/views/commons/404.vue')
},
{
    path: '*', // 匹配所有路由
    redirect: '/404'
}
];

export default routes;