'use strict'
// Template version: 1.3.1
// see http://vuejs-templates.github.io/webpack for documentation.

const path = require('path')
const webpack = require('webpack')
//打包去除console和debugger调试
const TerserPlugin = require('terser-webpack-plugin');
//构建速度（hard-source 在新版 Node 下易卡死，已禁用）
// const HardSourceWebpackPlugin = require('hard-source-webpack-plugin');
//抽离 css 支持按需加载
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
//gzip压缩代码
const CompressionWebpackPlugin = require('compression-webpack-plugin');
//sentry配置
const SentryCliPlugin = require("@sentry/webpack-plugin");
const timeStamp = new Date().getTime();
module.exports = {
    productionSourceMap: true,
    configureWebpack: config => {
        config.devtool = 'source-map';
        config.output= {
            ...config.output,
            filename: `js/[name].[hash].${timeStamp}.js`,
            chunkFilename: `js/[name].[hash].${timeStamp}.js`,
          };
        config.plugins.push(
            new webpack.ProvidePlugin({
                $: "jquery",
                jQuery: "jquery",
                "windows.jQuery": "jquery"
            })
        );
        //构建速度（hard-source 已禁用，见上方注释）
        // config.plugins.push(new HardSourceWebpackPlugin()); // <- 直接加入这行代码就行
        //非开发才有的
        if (process.env.NODE_ENV != 'development') {
            //打包去除console和debugger调试
            config.plugins.push(new TerserPlugin({
                terserOptions: {
                    ecma: undefined,
                    warnings: false,
                    parse: {},
                    compress: {
                        drop_console: true,
                        drop_debugger: false,
                        pure_funcs: ['console.log'], // 移除console
                    },
                },
            }));
            //抽离 css 支持按需加载
            config.plugins.push(new MiniCssExtractPlugin({
                filename: 'assets/[name].[hash:8].css',
                chunkFilename: 'assets/[name].[hash:8].css'
            }));
            //gzip压缩代码
            config.plugins.push(new CompressionWebpackPlugin({
                algorithm: 'gzip',
                threshold: 10240, // 只有大小大于该值的资源会被处理 10240
                test: new RegExp('\\.(' + ['js'].join('|') + ')$'),
                minRatio: 0.8, // 只有压缩率小于这个值的资源才会被处理
                deleteOriginalAssets: false // 删除原文件
            }));
            config.plugins.push(//sentry配置
                new SentryCliPlugin({
                    include: "./dist",//作用的文件夹
                    release: process.env.VUE_APP_RELEASE_VERSION,//一致的版本号
                    configFile: "./sentryclirc",
                    ignore: ["node_modules", "webpack.config.js","static"],
                    urlPrefix: "~/"//注意这个，同js存放位置，上传后可以查看
                }));
        }
    },
    transpileDependencies: ['vuex-persist'],
    lintOnSave: false,
    // css: {
    //     loaderOptions: {
    //       css: {},
    //       postcss: {
    //         plugins: [
    //           require("postcss-px2rem")({
    //             remUnit: 192///设计图宽度/10
    //           })
    //         ]
    //       }
    //     }
    // },
    devServer: {
        proxy: {
            '/ehr': {
                target: 'http://10.63.200.20:8083', //曾
                changeOrigin: true, //是否跨域
                bypass: function (req, res, proxyOptions) {
                    if (req.headers.accept.indexOf("html") !== -1) {
                        console.log("Skipping proxy for browser request.");
                        return "/index.html";
                    }
                },
                pathRewrite: {
                    '^/ehr': '/'
                }
            },
            // 供给端雷达后端（本地 java_bff）
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        },
        // Various Dev Server settings
        host: '0.0.0.0', // can be overwritten by process.env.HOST
        port: 8088, // can be overwritten by process.env.PORT, if port is in use, a free one will be determined
        open: true,
        overlay: {
            warnings: false,
            errors: false
        },
        watchOptions: {
            aggregateTimeout: 300,
            poll: 1000
        }
    }
}
