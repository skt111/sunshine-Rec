<!--
 * @Descripttion: 页面底部
 * @version: 
 * @Author: wuxiaoling-ghq
 * @LastEditors: wuxiaoling-ghq
-->
<template>
  <div class="footerMange">
    <div class="footerCenter">
      <div class="footerText">
        为了获得最佳操作体验，建议您使用最新版本的浏览器，支持1024 X
        768以上分辨率
      </div>
      <div class="footerText">阳光保险集团股份有限公司版权所有 V12</div>
      <div class="footerText">
        ©{{ year }} Sunshine Insurance Group Inc. All Rights Reserved.
      </div>
    </div>
  </div>
</template>
  
  <script>
import util from "@/utils/util";

export default {
  name: "MainFooter",
  components: {},
  data() {
    return {
      year: util.getCurrYear(), //当前年
    };
  },
  computed: {},
  watch: {},
  created() {},
  mounted() {},
  methods: {},
  beforeDestroy() {},
};
</script>  
  <style lang="scss" scoped>
.footerMange {
  text-align: center;
  width: 100%;
  clear: both;
  background: #f4f6fa;
  .footerCenter {
    padding: 5px;
    .footerText {
      font-size: 12px;
      line-height: 22px;
      color: #333333;
    }
  }
}
</style>
