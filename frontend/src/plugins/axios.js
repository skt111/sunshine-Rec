"use strict";

import Vue from 'vue';
import axios from "axios";
import router from '../router'
import utils from '../utils/util'
import store from '../store';
const http = require('http');
const https = require('https');

// Full config:  https://github.com/axios/axios#request-config
// axios.defaults.baseURL = process.env.baseURL || process.env.apiUrl || '';
axios.defaults.baseURL = '/'
// axios.defaults.headers.common['Authorization'] = AUTH_TOKEN;
// axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';

let config = {
    // baseURL: process.env.baseURL || process.env.apiUrl || ""
    timeout: 180 * 10000, // Timeout
    httpAgent: new http.Agent({ keepAlive: true }),
    httpsAgent: new https.Agent({ keepAlive: true }),
    // withCredentials: true, // Check cross-site Access-Control
}, requestUrl = "";

const _axios = axios.create(config);

_axios.interceptors.request.use(
    function (config) {
        // Do something before request is sent
        // encodeURIComponent
        if (localStorage.getItem('userToken'))
            config.headers.userToken = localStorage.getItem('userToken');
        if (localStorage.getItem('token'))
            config.headers.token = localStorage.getItem('token');
        // config.headers.deviceId = utils.getCookie('deviceId')
        requestUrl = config.url;
        if (config.url.indexOf('?') > -1) {
            config.url = config.url + '&timestamp=' + Date.parse(new Date());
        } else {
            config.url = config.url + '?timestamp=' + Date.parse(new Date());
        }
        if (config.method == 'post') {
            // config.data = {
            //     ...config.data,
            //     _t: Date.parse(new Date())/1000,
            // }
        } else if (config.method == 'get') {
            // config.params = {
            //     _t: Date.parse(new Date())/1000,
            //     ...config.params
            // }
        }
        return config;
    },
    function (error) {
        // Do something with request error
        return Promise.reject(error);
    }
);

// Add a response interceptor
_axios.interceptors.response.use(
    function (response) {
        // Do something with response data
        if (response.data.resultCode === '2') {
            router.replace({
                name: 'unAuth'
            })
        }
        if (response.data.status == 304) {
            router.replace({
                name: "Authority"
            });
        }
        if (response.data.status == 403 || response.data.status == 401) {
            router.replace({
                name: "sessionFail"
            });
        } 
        if (window.Sentry&&!response.data.type&&response.data.status != 'success')
            window.Sentry.captureException(new Error('response错误异常：' + response.data.status+'；接口地址：'+requestUrl));
        return response;
    },
    function (error) {
        // Do something with response error
        if (error.response) {
            switch (error.response.status) {
                case 304:
                    router.replace({
                        name: 'Authority'
                    });
                case 404:
                    router.replace({
                        name: 'Authority'
                    });
                case 403:
                    router.replace({
                        name: 'sessionFail'
                    });
            }
            if (window.Sentry) window.Sentry.captureException(new Error('error错误异常：'+error.response.status+'；接口地址：'+requestUrl));
        }
        // 返回接口返回的错误信息
        return Promise.reject(error);
    }
);
const Plugin = {
    axios: _axios
}

Plugin.install = function (Vue, options) {
    Vue.axios = _axios;
    window.axios = _axios;
    Object.defineProperties(Vue.prototype, {
        axios: {
            get() {
                return _axios;
            }
        },
        $axios: {
            get() {
                return _axios;
            }
        },
    });
};

Vue.use(Plugin)
export default Plugin;