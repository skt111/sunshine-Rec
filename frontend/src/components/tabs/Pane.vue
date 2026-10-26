<template>
    <div class="pane" v-show="show">        
        <slot></slot>
    </div>
</template>
<script>
export default {
  name:"Pane",
  props: {
    label: {
      type: String,
      deaful: ""
    },
    name: {
      type: String
    }
  },
  data() {
    return {
      show: true
    };
  },
  methods: {
    updateTabs() {
      //调用父组件中的方法，更新tabs
      this.$parent.updateTabs();
    }
  },
  watch: {
    label() {
      this.updateTabs();
    }
  },
  mounted() {
    //初始化tab
    this.updateTabs();
  }
};
</script>
<style lang="scss" scoped>
.pane{
      float: left;
    width: 100%;
}

</style>


