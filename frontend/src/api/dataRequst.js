/*
 * @Descripttion: 请求加解密处理
 * 环境变量(VUE_APP_ENCODE)是否加解密，Y加密，N解密
 * 默认根据环境变量分辨是否加解密,noEncode:true不加密
 * @version: 
 * @Author: wuxiaoling-ghq
 * @LastEditors: wuxiaoling-ghq
 */
import request from '@/plugins/axios'
import {
    enCodeSm4
} from "../utils/smjs/sm4";
//环境变量是否加解密，Y加密，N解密
//默认根据环境变量分辨是否加解密,noEncode:true不加密
let enCode = process.env.VUE_APP_ENCODE;
//处理数据的加解密
function dealData(data, noEncode){
    let postData = data,
        enCodeKey = localStorage.getItem("enCodeKey");
    //有数据加密处理
    if (data && enCode == 'Y')
        postData = {
            encrypt: enCodeSm4(JSON.stringify(data), enCodeKey)
        };
    //不加密处理，高于全局
    if (noEncode || enCode == 'N') {
        postData = data;
    }
    return postData;
}
//post传参接口
export function postDataList(url, data, noEncode) {
    let postData=dealData(data, noEncode);
    return request.axios({
        url: url,
        method: "post",
        data: postData
    });
}
//gett传参接口
export function getList(url, data, noEncode) {
    let enCodeKey = localStorage.getItem("enCodeKey"),
        postData = "",
        postUrl = url;
    if (data) {
        //有数据加密处理
        postData = encodeURIComponent(enCodeSm4(JSON.stringify(data), enCodeKey)),
            postUrl = url + "?param=" + postData;
        //不加密处理
        if (noEncode || enCode == 'N') {
            postUrl = '?' + Object.keys(data).reduce(function (a, k) {
                a.push(k + '=' + encodeURIComponent(data[k]));
                return a
            }, []).join('&');
        }
    }

    return request.axios({
        url: postUrl,
        method: "get"
    });
}

//delete传参接口
export function deleteList(url) {
    return request.axios({
        url: url,
        method: "delete"
    });
}
//post下载文件流
export function postFileList(url, data, noEncode) {
    let postData=dealData(data, noEncode);
    return request.axios({
        method: 'post',
        url: url,
        responseType: "blob",
        data: postData
    });
}

//get下载文件流
export function getFileList(url, data, noEncode) {
    let enCodeKey = localStorage.getItem("enCodeKey"),
        postData = "",
        postUrl = url;
    if (data) {
        //有数据加密处理
        postData = encodeURIComponent(enCodeSm4(JSON.stringify(data), enCodeKey)),
            postUrl = url + "?param=" + postData;
        //不加密处理
        if (noEncode || enCode == 'N') {
            postUrl = '?' + Object.keys(data).reduce(function (a, k) {
                a.push(k + '=' + encodeURIComponent(data[k]));
                return a
            }, []).join('&');
        }
    }
    return request.axios({
        method: 'get',
        url: postUrl,
        responseType: "blob"
    });
}