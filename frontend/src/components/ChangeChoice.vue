<template>
  <div class="shrCon">
    <div class="choiceDiv">
      <div class="leftCon">
        <p class="pTitle">当前配置合同材料列表</p>
        <p
          class="pCon"
          v-for="(sele,idx) in checkedList"
          :key="'checked'+idx"
        >
          <Checkbox
            :value="sele.status=='Y'?true:false"
            @click.native="seleBtn(sele)"
          ></Checkbox>
          <span class="nameDiv" v-tip="{ customClass: 'tipShow', content: sele.dataName }">{{sele.dataName}}</span>
          <span class="tip">
            <Icon
              size="18"
              color="#1890ff"
              type="md-information-circle"
              v-tip="{'customClass':'tipShow','content':sele.dataDesc}"
            /></span>
        </p>
      </div>
      <div class="btnCon">
        <a @click="toRight"><img
            class=""
            src="../assets/img/common/double-arrow-right.png"
          /></a>
        <a @click="toLeft"><img
            class=""
            src="../assets/img/common/double-arrow-left.png"
          /></a>
      </div>
      <div class="rightCon">
        <p class="pTitle">可添加合同材料列表</p>
        <p
          class="pCon"
          v-for="(sele,idx) in uncheckedList"
          :key="'unchecked'+idx"
        >
          <Checkbox
            :value="sele.status=='Y'?true:false"
            @click.native="seleBtn(sele)"
          ></Checkbox>
          <span class="nameDiv" v-tip="{ customClass: 'tipShow', content: sele.dataName }">{{sele.dataName}}</span>
          <!--<span class="tip">
            <Icon
              size="18"
              color="#1890ff"
              type="md-information-circle"
              v-tip="{'customClass':'tipShow','content':sele.dataDesc}"
            /></span>-->
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
      uncheckedList: [] //未选中
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
      this.uncheckedList = [];
      this.conData.forEach((item, idx) => {
        if (item.status === "Y") {
          that.checkedList.push(item);
        } else {
          that.uncheckedList.push(item);
        }
      });
    },
    //是否选中切换
    seleBtn(data) {
      data.status = data.status === "Y" ? "N" : "Y";
    },
    dealList(type){
      let checkedsl = [],
        checkedsr = [],
        uncheckedsl = [],
        uncheckedsr = [];
      //左侧列表被选中的
      checkedsl = this.checkedList.filter(item => {
        return item.status == "Y";
      });
      //左侧未被选中的
      checkedsr = this.checkedList.filter(item => {
        return item.status == "N";
      });
      //右侧被选中的
      uncheckedsl = this.uncheckedList.filter(item => {
        return item.status == "Y";
      });
      //右侧未被选中的
      uncheckedsr = this.uncheckedList.filter(item => {
        return item.status == "N";
      });
      if(type=='toRight'){
      	this.checkedList = checkedsr;
      	this.uncheckedList = this.uncheckedList.concat(checkedsl);
      }else{
      	this.checkedList = this.checkedList.concat(uncheckedsl);
      	this.uncheckedList = uncheckedsr;
      }
      /*this.checkedList = checkedsl.concat(uncheckedsl);
      this.uncheckedList = checkedsr.concat(uncheckedsr);*/
    },
    toRight() {
      this.dealList('toRight');
    },
    toLeft() {
      this.dealList('toLeft');
    },
    ok() {
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
  @include fw(40);
  border: 1px solid #eee;
  height: 260px;
  overflow:scroll;
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
    .tip {
      position: absolute;
      right: 20px;
    }
    .nameDiv{
    width: 150px;
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
</style>