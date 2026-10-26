<template>
  <div class="shrCon">
    <div class="choiceDiv">
      <div class="leftCon">
        <p class="pTitle">当前合同材料列表</p>
        <p
          class="pCon"
          v-for="(sele,idx) in conData"
          :key="'checked'+idx"
        >
          <Checkbox
            :value="sele.status=='Y'||sele.isRequired?true:false"
            :disabled="sele.isRequired"
            @click.native="seleBtn(sele)"
          ></Checkbox>
          <span class="nameDiv" v-tip="{ customClass: 'tipShow', content: sele.dataName }">{{sele.dataName}}</span>
          <span class="tip">
            <Icon
              size="18"
              color="#1890ff"
              type="md-information-circle"
              v-tip.left="{'customClass':'tipShow','content':sele.dataDesc}"
            /></span>
        </p>
      </div>
    </div>
    <div
      class="shareLine"
      style="text-align:center;"
    >
      <a
        class="but saveBtn mr10"
        @click="ok"
      >确认</a>
      <a
        class="but butr"
        @click="cancel"
      >取消</a>
    </div>
    <Loading ref="loadings"></Loading>
  </div>
</template>
<script>
import Loading from "@/components/Loading";
import Dialog from "@/components/Dialog";
import util from "@/utils/util";

export default {
  name: "ChangeChoice",
  components: {
    Loading,
    Dialog
  },
  data() {
    return {
      checkedList: [], //选中
    };
  },
  props: {
    lists: {
      type: Array,
      default: () => {
        return [];
      }
    }
  },
  computed: {
    conData() {
      return JSON.parse(JSON.stringify(this.lists));
    }
  },
  watch: {
    conData() {
      this.initData();
    }
  },
  created() {},
  mounted() {
    if (this.conData.length > 0) {
      this.initData();
    }
  },
  methods: {
    initData() {
      let that = this;
      this.checkedList = [];
      this.conData.forEach((item, idx) => {
        if (item.status === "Y") {
          that.checkedList.push(item);
        } 
      });
    },
    //是否选中切换
    seleBtn(data) {
      data.status = data.status === "Y" ? "N" : "Y";
    },
    ok() {
    	debugger;
      this.checkedList = this.conData.filter(item => {
        return item.status == "Y";
      });
      this.$emit('okDialog',this.checkedList);
    },
    cancel() {
      this.$emit('closeDialog',false);
    },
    sure(content) {
      this.dialogType = "sure";
      this.content = content;
      if (this.$refs.dialog) {
        this.$refs.dialog.show();
      }
    }
  }
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
@mixin fw($val) {
  float: left;
  width: #{$val + "%"};
}
.mr10 {
  margin-right: 20px;
}
.choiceDiv {
  padding: 0px 20px;
}
.leftCon,
.rightCon {
  @include fw(100);
  border: 1px solid #eee;
    height: 300px;
    overflow: auto;
  .pTitle {
    border-bottom: 1px solid #eee;
    line-height: 36px;
    height: 36px;
    text-align: center;
  }
  .pCon {
    line-height: 32px;
    height: 32px;
    padding-left: 20px;
    position: relative;
    width: 50%;
    float: left;
    .tip {
      position: absolute;
      right: 20px;
    display: none;
    }
    .nameDiv{
    width: 75%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    display: inline-block;
    }
  }
}
.btnCon {
  @include fw(20);
  padding: 0 20px;
  margin: 15% 0;
  a {
    @include fw(100);
    text-align: center;
    border: 1px solid #1890ff;
    border-radius: 5px;
    margin-bottom: 20px;
  }
  img {
    width: 28px;
    vertical-align: middle;
  }
}
/deep/ .pCon label{
	position: relative;
	top: -12px;
}
/deep/.ivu-checkbox-disabled.ivu-checkbox-checked .ivu-checkbox-inner{
  border-color: #333;
}
/deep/.ivu-checkbox-disabled.ivu-checkbox-checked .ivu-checkbox-inner:after{
  border-color: #333;
}
</style>