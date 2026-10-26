<template>
  <div class="header">
    <div class="home-top">
      <img src="../assets/img/titleLogo.png"></img>
      <div class="header-top">
        <ul>
          <li class="lastLi"><a @click="updatePwd">修改密码</a></li>
          <li><a @click="quit">
              <Icon
                type="md-exit"
                size="18"
                color="#333"
              /></a></li>
          <li>
            {{station}}
          </li>
          <li>
            用户：{{userInfo.userName}}
          </li>
        </ul>
      </div>
    </div>
    <updatePWD
      ref="updatePwd"
      :userInfo="userInfo"
      :title="title"
    ></updatePWD>
  </div>
</template>
<script>
import updatePWD from "@/components/updatePWD.vue";

export default {
  name: "Header",
  components: {
    updatePWD
  },
  props: ["userInfo", "station"],
  data() {
    return {
      title: "修改密码"
    };
  },
  mounted() {},
  methods: {
    quit() {
      //localStorage.clear();
      //清空localStorage中所有信息
      let that=this;
      this.axios
        .get("/logHom/logout")
        .then(res => {
          if (res.data.status == "success") {
            //跳转到登录
            localStorage.clear();
            this.$store.commit("savePostData", { postData:{} });
            this.$router.push({ path: "/" });
          } else{
            this.$Modal.error({
              title: "错误",
              content: res.data.message
            });
          }
        })
        .catch(error => {
          //弹框提示：请求失败
            this.$Message.error({
              title: "错误",
              content: '请求失败！'
            });
        });
    },
    updatePwd() {
      this.$refs.updatePwd.modal = true;
    }
  }
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
$vendors: webkit, moz, o;
@mixin prix($type, $val) {
  #{$type}: #{$val};
  @each $v in $vendors {
    #{"-" + $v + "-" + $type}: #{$val};
  }
}
.header {
  width: 100%;
  float: left;
  border-bottom: 5px solid #ff774f;
  @include prix(box-shadow, 0 0 3px #bbb);
  box-shadow: 0 0 3px #bbb;
  @include prix(linear-gradient, (#fbfbfb, #ddd));
  background: linear-gradient(#fbfbfb, #ddd);
  .home-top {
    img {
      float: left;
      margin-left: 20px;
      margin-top: 10px;
      margin-bottom: 10px;
      height: 48px;
    }
  }
  .header-top {
    ul {
      li {
        list-style: none;
        float: right;
        margin: 10px;
        height: 48px;
        line-height: 48px;
      }
      .lastLi{
        margin-right:20px;
      }
    }
  }
}
</style>