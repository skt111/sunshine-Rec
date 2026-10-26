<template>
  <div class="tipInfo">
    <span>哎呀，页面走丢了~</span>
    <img src="../../assets/img/common/error_txt.png" />
    <a>将在 <label>{{seconds}}</label> 秒钟后关闭此页面~</a>
  </div>
</template>

<script>
export default {
  name: "404",
  data() {
    return {
      timer: null,
      seconds: 5
    };
  },
  computed: {},
  created() {},
  mounted() {
    let that = this;
    //this.timer == setInterval(that.jumpTo, 1000);
  },
  methods: {
    jumpTo() {
      let that = this;
      if (that.seconds == 1) {
        //关闭当前页面
        window.opener = null;
        window.open("about:blank", "_top").close();
        //跳转到外部链接页面
        // window.location.href = 'http://eoa.sinosig.com'; 
        // clearInterval(that.timer);
        return false;
      }
      that.seconds -= 1;
    }
  }
};
</script>

<style lang="scss" scoped>
.tipInfo {
  text-align: center;
  position: relative;
    font-size: 16px;
  span {
    display: block;
    position: relative;
    top: 270px;
    left:0px;
  }
  a {
    display: block;
    position: absolute;
    top: 300px;
    text-align: center;
    width: 100%;
    lable {
      color: red;
      font-weight: bold;
    }
  }
}
</style>