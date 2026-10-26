import Vue from 'vue'
import * as Sentry from '@sentry/browser'//js 运行于浏览器，这里提供 js 相关sdk
import * as Integrations from '@sentry/integrations'//使得sentry可以配合vue进行问题定位，会更详细一些

Sentry.init({
    dsn: 'http://a7cadfc44d2947a5922ceca5bef6bba5@10.7.137.160:9000/3',//@TODO项目唯一标识，由 协议+秘钥+服务器地址+项目编号组成
    release: process.env.VUE_APP_RELEASE_VERSION, //版本号
    environment: process.env.NODE_ENV,//环境
    integrations: [
        //实例化时传递vue及一些选项
     new Integrations.Vue({
         Vue,
         attachProps: true,//代表是否需要暴露 propsData
         logErrors:true//是否需要在控制台打印错误
    }),
    new Integrations.RewriteFrames()
    ]
    });
    window.Sentry=Sentry;
    Vue.prototype.$Sentry = Sentry;