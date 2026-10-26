<template>
  <div class="approveDiv">
    <div class="liucheng_warp">
      <table>
        <tbody>
          <tr>
            <td>
              <img
                src="../../assets/img/start.png"
                width="88"
                class="startImg"
              />
            </td>
            <td>
              <table class="liucheng_table">
                <tbody>
                  <tr>
                    <td v-for="(appro, idx) in apprList" :key="'appro' + idx">
                      <img
                        class="step_jia"
                        src="../../assets/img/riseJiantou.png"
                        width="24"
                        height="24"
                      />
                      <div
                        class="blue_liucheng"
                        :class="
                          appro.approvalStatus == '未提交'
                            ? 'blue_innerBgGreyLine'
                            : 'blue_liucheng'
                        "
                      >
                        <div
                          class="blue_inner"
                          :class="
                            appro.approvalStatus == '已提交'
                              ? 'blue_innerBg'
                              : appro.approvalStatus == '未提交'
                              ? 'blue_innerBgGrey'
                              : appro.approvalStatus == '已审批'
                              ? 'blue_innerBgGreen'
                              : ''
                          "
                        >
                          {{ appro.approvalStatus }}
                          <a class="shanchu_jianhao" style="display: none"></a>
                        </div>

                        <div class="blue_dowm">
                          <div>
                            <a class="spr_a">
                              <span>{{ appro.name }}</span></a
                            >
                          </div>
                          <div>{{ appro.approvalDt }}</div>
                        </div>
                      </div>
                      <div class="blue_opinion" :title="appro.msg">
                        {{ appro.msg }}
                      </div>
                    </td>

                    <td>
                      <a id="right_add" class="right_add liu_add">
                        <img
                          src="../../assets/img/riseJiantou.png"
                          width="24"
                          height="24"
                      /></a>
                    </td>
                  </tr>
                </tbody>
              </table>
            </td>
            <td>
              <img
                src="../../assets/img/endOff.png"
                width="88"
                class="endImg"
              />
            </td>
          </tr>
        </tbody>
      </table>
      <div class="searchLine" v-if="isButton">
        <div class="searchCol1">
          <a class="but primary mr10" @click="savePreview">提交</a>
          <a class="but" @click="cancel">取消</a>
        </div>
      </div>
    </div>
    <Loading ref="loadings"></Loading>
    <Dialog ref="dialog" :content="content" :type="dialogType"></Dialog>
  </div>
</template>
<script>
import Loading from "@/components/Loading";
import Dialog from "@/components/Dialog";
import util from "@/utils/util";

export default {
  name: "ApproveFlow",
  components: {
    Loading,
    Dialog,
  },
  data() {
    return {
      content: "", //弹框展示内容
      dialogType: "confirm", //什么类型的弹框
      nodeList: [],
    };
  },
  props: {
    apprList: {
      type: Array,
      default: () => {
        return [];
      },
    },
    open: {
      type: Boolean,
      default: true,
    },
    type: {
      type: String,
      default: "submit",
    },
    isButton: {
      type: Boolean,
      default: true,
    },
  },
  watch: {},
  computed: {},
  created() {},
  mounted() {},
  methods: {
    //提交
    savePreview() {
      this.$emit("saveClick", this.apprList);
    },
    //取消
    cancel() {
      this.$emit("cancelClick");
    },
  },
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
@mixin fw($val) {
  float: left;
  width: #{$val + "%"};
}
.approveDiv {
  @include fw(96);
  margin-left: 20px;
}
.liucheng_warp {
  display: block;
  overflow: auto;
}
.liucheng_table {
  margin-bottom: 20px;
  margin-left: 20px;
  float: left;
  td {
    height: 130px;
    position: relative;
  }
}
.startImg {
  float: left;
  position: relative;
  top: -25px;
  left: 20px;
}
.endImg {
  float: left;
  position: relative;
  top: -25px;
}
.step_jia {
  position: relative;
  top: 65px;
}
.addBtn {
  width: 35px;
  position: absolute;
  left: 30px;
  top: 60px;
  z-index: 2;
  cursor: pointer;
}
.deleBtn {
  width: 35px;
  position: absolute;
  right: 0;
  top: 0;
  cursor: pointer;
}
.blue_liucheng {
  width: 194px;
  overflow: hidden;
  border-radius: 2px;
  background: #ffffff;
  border: 1px solid #bae7ff;
  margin-left: 30px;
  position: relative;
  top: -20px;
  .blue_inner {
    height: 32px;
    line-height: 29px;
    width: 100%;
    color: #fff;
    background: #ffae00;
    text-align: center;
    font-size: 15px;
    color: #000;
    position: relative;
    a {
      top: 7px;
      right: 15px;
      width: 20px;
      height: 15px;
      display: block;
      background: url("../../assets/img/shareHr/check.png") no-repeat;
      position: absolute;
    }
  }
  .blue_dowm {
    height: 100px;
    overflow: auto;
    width: 100%;
    padding: 4px;
    div {
      line-height: 22px;
      text-align: center;
    }
    .spr_a {
      text-decoration: underline;
      color: #2e81d7;
    }
    .mt_5 {
      margin-top: 5px;
    }
    .conappr {
      line-height: 15px;
      word-break: break-all;
      text-align: left;
    }
  }
}
.blue_opinion {
  width: 194px;
  padding: 4px 8px;
  background: #f0f5ff;
  margin-left: 30px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  margin-top: -10px;
  min-height: 30px;
}
.sureBtn {
  width: 90px;
  height: 25px;
  text-align: center;
  background: #2d8cf0;
  color: #fff;
  padding: 5px 30px;
  line-height: 30px;
  font-size: 14px;
  border-radius: 4px;
  border: 1px solid #2d8cf0;
}
.cancleBtn {
  margin-right: 20px;
  width: 90px;
  height: 25px;
  text-align: center;
  padding: 5px 30px;
  line-height: 30px;
  font-size: 14px;
  border-radius: 4px;
  color: #2d8cf0;
  background: #fff;
  border: 1px solid #2d8cf0;
}
.input {
  height: 32px;
  border: 1px solid #dcdee2;
  border-radius: 5px;
  outline: none;
  padding-left: 10px;
}
.addDialog {
  width: 800px;
  /deep/.ivu-modal {
    width: 800px !important;
  }
}
.blue_innerBg {
  background: #1890ff !important;
  color: #fff !important;
  box-shadow: 0px 4px 4px 0px rgba(24, 144, 255, 0.2);
}
.blue_innerBgGrey {
  background: #838a9d !important;
  color: #fff !important;
  box-shadow: 0px 4px 4px 0px rgba(131, 138, 157, 0.2);
}
.blue_innerBgGreyLine {
  border: 1px solid #e9eaf1 !important;
  background: #fff !important;
}
.blue_innerBgGreen {
  background: #52c41a !important;
  color: #fff !important;
  box-shadow: 0px 4px 4px 0px rgba(82, 196, 26, 0.2);
}
.liu_add {
  margin-top: -25px;
  display: block;
}
</style>
