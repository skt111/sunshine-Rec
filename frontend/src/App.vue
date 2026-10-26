<template>
  <div id="app">
    <!-- 路由出口 -->
    <!-- 路由匹配到的组件将渲染在这里 -->
    <!-- 抽象的组件，用来对组件进行缓存，节省性能 -->
    <transition name="fade">
      <keep-alive><router-view /></keep-alive>
    </transition>
  </div>
</template>

<script>
import { generateKeySm4, enCodeSm4 } from "./utils/smjs/sm4";
import { postDataList,getList } from "./api/dataRequst"; //请求接口
import util from "./utils/util";
export default {
  name: "app",
  components: {

  },
  data() {
    return {
     
    };
  },
  computed: {
    
  },
  created() {
    //加密处理
    //this.initCode();
  },
  mounted(){
    //添加水印
    util.setUserImgTalent({
      content: "测试",
    });
  },
  methods: {
    async initCode() {
      //生成加密串，获取后台返回的公钥
      let enCodeKey = generateKeySm4(),
        pubkey = localStorage.getItem('pubkey');//'04464D16DAB94B436D26EED32CF27ABD86070D9F0F33C3C4A12EC7EEE9C3C066DC5F36154CD82C90C93C9889F7E9D34B7C0560BDD606730E7BA49BCDF88EF28EB6';
      //根据后台返回的公钥进行加密
      const sm2 = require("sm-crypto").sm2;
      let encryptData = "04" + sm2.doEncrypt(enCodeKey, pubkey);
      //权限，根据后台返回的token进行设置
      localStorage.setItem("token","");//"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySW5mbyI6IntcImVtcGxJZFwiOlwiQTAwMjAxNzAxMDdcIixcImlwXCI6XCIxMC42My4yMDAuMTExXCIsXCJvcHJJZFwiOlwic2hpemhpcWlhbmctZ2hxXCJ9Iiwic3ViIjoidHNjJ3MgdG9rZW4iLCJhdWQiOiJBUFAiLCJpc3MiOiJ0c2NDb21tb24iLCJleHAiOjE2NTAzOTA4NDcsImlhdCI6MTY1MDM0NzY0N30.uGSHLymAlAYDPy0U9d_BBFs_P1VLRmE6fDh7crVXTMo");
      //信息加密--后台需要
      localStorage.setItem("headCode", encryptData);
      //信息加密--前端加密
      localStorage.setItem("enCodeKey", enCodeKey);
      //测试前后加密数据
      // let postData ={
      //     oprid: "01说明"
      //   };
      // try {
      //   //post请求
      //   let res = await postDataList("/tscDemo/demo/getToken", postData);
      //   //get请求
      //   //let res =await getList("/tscDemo/demo/hello",postData);
      //   console.log(res);
      // } catch (e) {
      //   console.log("error");
      // }
    },
  },
  watch: {
   
  }
};
</script>

<style lang="scss">
#app {
  font-family: Arial,"Microsoft Yahei";
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  //text-align: center;
  font-size:14px;
  color: #2c3e50;
  position:relative;
  float:left;
  width:100%;
}
#nprogress .bar {
      background: red !important; //自定义颜色
    }
</style>
