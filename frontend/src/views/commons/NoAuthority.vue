<template>
  <div class="content">
    <div class="contBorder">
    <nav>
      <h1>{{title}}</h1>
    </nav>
    <div class="row nevNo" style="width:100%;text-align: center;border-top: 1px solid #ccc; padding-top: 18px;">
        <p style="font-size: 16px;">抱歉，您暂没有此功能的访问权限。</p>
      <img src="../../assets/img/common/noData.png">
    </div>
    </div>
  </div>
</template>
<script>
export default {
  name: "Authority",
  components: {
  },
  data() {
    return {
      title:this.$route.query.title?this.$route.query.title:'当前系统'
    };
  },
  created() {
  },
  computed: {
    
  },
  mounted() {
  },
  methods: {}
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
html{
  background: #eeeeee;
}
nav{
      width: 100%;
    background: transparent;
    margin: 0px;
    height: auto;
}
.contBorder{
      margin: 0 56px;
    background: #fff;
    padding: 40px;
    border-radius: 12px;
    box-shadow: 0px 8px 48px rgba(0,0,0,.2);
}
</style>