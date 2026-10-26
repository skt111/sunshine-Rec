<template>
  <div class="region">
    <!-- 没有数据等有数据再展示 -->
    <!-- <div class="title">
      <span class="icon"></span>
      <span class="titleSpan">分公司部门级人员职务异动</span>
    </div> -->
    <!--  nodeInfo.setApprovalStatus("已提交");
          nodeInfo.setApprovalCode("submited");
          nodeInfo.setApprovalStatus("已审批");
          nodeInfo.setApprovalCode("approved");
          nodeInfo.setApprovalStatus("待决");
          nodeInfo.setApprovalCode("estimated");
          nodeInfo.setApprovalStatus("未发送");
          nodeInfo.setApprovalCode("notSend");
          nodeInfo.setApprovalStatus("已撤回");
          nodeInfo.setApprovalCode("withdraw");
          nodeInfo.setApprovalStatus("已拒绝");
          nodeInfo.setApprovalCode("rejected");
          draftNode.setApprovalStatus("未提交");
          draftNode.setApprovalCode("unSubmit");
          nodeInfo.setApprovalStatus("已跳过");
          nodeInfo.setApprovalCode("repeat_skip"); -->
    <div class="approveDiv">
      <div class="liucheng_warp">
        <div class="titleText">
          <span class="horizontalLine" style="margin-right: 24px"></span
          >流程审批路径<span
            class="horizontalLine"
            style="margin-left: 24px"
          ></span>
        </div>
        <table class="tableAppr">
          <tbody class="tbodyAppr">
            <tr style="width: 100%">
              <!-- 开始按钮 -对应开始节点-->
              <td>
                <div class="startText"><span>流程开始</span></div>
              </td>
              <td>
                <table class="liucheng_table">
                  <tbody>
                    <tr>
                      <td
                        v-for="(appro, idx) in approveList"
                        :key="'appro' + idx"
                      >
                        <div
                          v-if="
                            appro.nodeId != 'N1' &&
                            appro.nodeId != 'N3' &&
                            !appro.delete
                          "
                        >
                          <i
                            v-if="appro.nodeId == 'N2'"
                            class="icon-xiangyou step_jia"
                          >
                          </i>
                          <div
                            class="blue_liucheng"
                            :class="[
                              appro.nodeId != 'N2' ? 'top15' : '',
                              appro.approvalCode == 'estimated' ||
                              appro.approvalCode == 'submited' ||
                              appro.approvalCode == 'approved' ||
                              appro.approvalCode == 'unSubmit'
                                ? ''
                                : appro.approvalCode == 'notSend' ||
                                  appro.approvalCode == 'repeat_skip' ||
                                  appro.approvalCode == 'withdraw' ||
                                  !appro.approvalCode
                                ? 'backgroundHui'
                                : 'backgroundHong',
                              appro.nodeId == 'N2' ? 'marginLeft32' : '',
                            ]"
                            v-if="appro.nodeId != 'N1' && appro.nodeId != 'N3'"
                          >
                            <div class="blue_inner">
                              <div
                                class="stateText"
                                :class="
                                  appro.approvalCode == 'estimated' ||
                                  appro.approvalCode == 'submited' ||
                                  appro.approvalCode == 'approved' ||
                                  appro.approvalCode == 'unSubmit'
                                    ? 'themeColor'
                                    : appro.approvalCode == 'notSend' ||
                                      appro.approvalCode == 'repeat_skip' ||
                                      appro.approvalCode == 'withdraw' ||
                                      !appro.approvalCode
                                    ? 'grayColor'
                                    : 'redColor'
                                "
                              >
                                {{ appro.approvalStatus }}
                              </div>
                              <div class="roleText">
                                {{ appro.nodeDescr }}
                              </div>
                              <div
                                :class="appro.time ? 'marTop7' : 'marTop15'"
                                v-if="
                                  appro.handlerList &&
                                  appro.handlerList.length > 1
                                "
                              >
                                <a
                                  class="spr_a"
                                  @click="handlerModalShow(appro.handlerList)"
                                >
                                  <div class="handlerModalBtn">多个审批人</div>
                                </a>
                              </div>
                              <div
                                :class="appro.time ? 'marTop7' : 'marTop15'"
                                v-if="
                                  appro.handlerList &&
                                  appro.handlerList.length == 1
                                "
                              >
                                <a
                                  class="spr_a"
                                  v-for="(user, idx) in appro.handlerList"
                                  :key="'user' + idx"
                                >
                                  <div>{{ user.name }}</div>
                                  <div>
                                    {{
                                      "(" +
                                      user.oprId +
                                      ")" +
                                      (idx == appro.handlerList.length - 1
                                        ? ""
                                        : ",")
                                    }}
                                  </div></a
                                >
                              </div>
                              <div class="tiemText">{{ appro.time }}</div>
                            </div>
                          </div>
                          <!-- 新增:editable 用于标识该节点的位置是否位于当前待决节点之后。 
                          如果为true则标识该节点位于待决节点之后，该节点前后可以插入新节点。 -->
                          <!-- 加号 -->
                          <i
                            v-if="appro.editable && operatable == '1'"
                            class="icon-jiahao2fill addBtnBlue"
                            style="font-size: 32px"
                            @click="addPer(appro, idx)"
                          >
                          </i>
                          <!-- 箭头 -->
                          <i
                            v-if="!appro.editable || operatable == '0'"
                            :class="
                              appro.approvalCode == 'notSend' ||
                              appro.approvalCode == 'repeat_skip' ||
                              appro.approvalCode == 'rejected' ||
                              !appro.approvalCode
                                ? 'colorgray'
                                : ''
                            "
                            class="icon-xiangyou step_xia"
                          >
                          </i>
                          <!-- 删除:add为true（是用户手动添加的节点）、editable为true(在当前节点及以后)、current为false（不是当前节点）-->
                          <i
                            v-if="
                              (appro.editable &&
                                appro.add &&
                                !appro.current &&
                                operatable == '1') ||
                              (appro.isNewAdd && operatable == '1')
                            "
                            class="icon-delete-filling deleBtn"
                            @click="delePer(appro, idx)"
                          >
                          </i>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </td>
              <!-- 结束按钮-对应结束节点 -->
              <td>
                <div class="endText">
                  <span>流程结束</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <div class="approveDivLeft" v-if="!preview">
      <div class="liucheng_warp">
        <div class="titleText">
          <span class="horizontalLine" style="margin-right: 24px"></span
          >流程审批记录<span
            class="horizontalLine"
            style="margin-left: 24px"
          ></span>
        </div>
        <div
          class="serchLine boderBom"
          v-for="(item, index) in historyData"
          :key="index"
        >
          <div class="searchCol1" style="margin-bottom: 8px">
            <span class="rightSpan"
              >{{ item.fdHandler }}（{{ item.loginName }}）</span
            ><span class="rightSpan" style="margin-left: 10px">{{
              item.fdCreateTime
            }}</span>
          </div>
          <div
            class="searchCol1"
            style="border-bottom: 1px solid #e2e2e4; padding-bottom: 8px"
          >
            <span v-if="item.ldkOperation">{{ item.ldkOperation }},</span>
            <span class="rightSpan">{{ item.fdMessage }}</span>
          </div>
        </div>
      </div>
    </div>
    <!-- 审批意见-不是预览审批流程进来的并且可以操作审批意见 -->
    <div class="approveDivLeft" v-if="!preview && operatable == '1'">
      <div class="liucheng_warp">
        <div class="titleText">
          <span class="horizontalLine" style="margin-right: 24px"></span
          >流程审批意见<span
            class="horizontalLine"
            style="margin-left: 24px"
          ></span>
        </div>
        <div
          class="searchLine"
          style="text-align: left; padding-bottom: 10px; margin-bottom: 0px"
        >
          <div class="searchCol1">
            <span
              style="float: left; margin-top: 5px; font-size: 12px;width: 100px;"
              class="leftSpan moreLine"
              ><span class="reqFont" v-if="mustShowCom">*</span>审批意见</span
            >
            <Input
              type="textarea"
              v-model="comment"
              :rows="2"
              placeholder="请填写审批意见..."
              :maxlength="300"
              style="font-size: 12px"
            />
          </div>
        </div>
      </div>
    </div>
    <Modal
      v-model="changeInformation"
      :mask-closable="true"
      class-name="cityModal"
      scrollable
      footer-hide
      width="1000"
    >
      <iframe
        id="myIframe"
        ref="iframe"
        src=""
        style="width: 100%; height: 520px; border: 0"
        frameborder="0"
      ></iframe>
    </Modal>
    <Modal
      v-model="handlerMore"
      :mask-closable="true"
      class-name="cityModal"
      scrollable
      width="400"
    >
      <p slot="header">
        <span>审批人</span>
      </p>
      <div class="region_content" style="margin: 10px">
        <Table
	        :data="handlerModalList"
	        :columns="handlerModalCols"
	        stripe
	        border
	      ></Table>
      </div>
      <div slot="footer" class="serchLine">
        <div class="searchCol1">
          <a class="but" style="margin-left: 20px" @click="handlerMore = false"
            >关闭</a
          >
        </div>
      </div>
    </Modal>
    <Loading ref="loadings" style="z-index: 99999"></Loading>
  </div>
</template>
<script>
import Loading from "@/components/Loading";
import { getList, getHeadersList, postDataList } from "@/api/dataRequst";
import util from "@/utils/util";

export default {
  name: "ApproveFlowDataMK",
  components: {
    Loading,
  },
  data() {
    return {
      groupShow: false, //放大镜展示
      loading: false,
      addObj: {
        nodeId: "", //节点ID，不可与现存节点重复
        nodeCn: "", //节点名称可选
        oprId: "", //节点人员域账号
        lastNodeId: "", //上一链接节点
        add: true, //新增传true
        isNewAdd: true, //当前新增
      },
      descr: "", //部门
      nodeCurr: {}, //添加节点之后的节点的信息
      isShow: false, //显示最后一个加号
      content: "", //弹框展示内容
      newList: [], //新增节点
      changeInformation: false, //所属公司机构弹窗
      deptUrl: "", //组织结构地址
      upOneLevelObj: {}, //添加节点的上一级数据
      addIdx: null, //添加节点的上一级index
      addList: [], //新增节点列表-获取取值取这个
      dellist: [], //删除节点列表-获取取值取这个
      //N1 开始    N2  起草   N3结束
      comment: "", //处理意见
      handlerMore: false, //是否同一个节点有多个审批人
      handlerModalList: [], //要展示的节点审批人
      handlerModalCols: [
        {
          title: "序号",
          type: "index",
          width: 70,
          align: "center"
        },
        {
          title: "姓名",
          key: "name",
          ellipsis: true,
          tooltip: true,
          align: "center"
        }, 
        {
          title: "域账号",
          key: "oprId",
          ellipsis: true,
          tooltip: true,
          align: "center"
        },
      ],
    };
  },
  props: {
    //审批流程list
    apprList: {
      type: Array,
      default: () => {
        return [];
      },
    },
    //审批历史记录-list
    historyData: {
      type: Array,
      default: () => {
        return [];
      },
    },
    //当前登录人
    userInfoName: {
      type: String,
      default: "",
    },
    //审批意见是否必填
    mustShowCom: {
      type: Boolean,
      default: true,
    },
    //是否可以新增，删除节点以及审批意见是否显示
    operatable: {
      type: String,
      default: "1",
    },
    //预览审批流程-是否是预览审批流程按钮
    preview: {
      type: String,
      default: "",
    },
    //获取组织架构树地址接口路径
    getOrganizationUrl: {
      type: String,
      default: () => {
        return process.env.VUE_APP_CONFIG+"/tag/flow/getOrgUrl";
      },
    },
    bacePath: {
      type: String,
      default: "",
    },
  },
  watch: {
    apprList: {
      handler(newValue, oldValue) {
        debugger;
        this.approveList = newValue.concat(this.newList);
        // N3是结束节点id，如果是结束节点就不能展示+号
        this.isShow =
          this.approveList.length > 0 &&
          this.approveList[this.approveList.length - 1].nodeId != "N3"
            ? false
            : true;
      },
      deep: true,
      immediate: true,
    },
    approveList: {
      handler(newValue, oldValue) {
        this.addList = [];
        this.dellist = [];
        let nodeId = "";
        this.approveList.map((item) => {
          // 新增的数组
          if (item.isNewAdd == true) {
            this.addList.push({
              nodeId: item.nodeId, //节点ID，不可与现存节点重复
              nodeCn: item.nodeDescr, //节点名称可选
              oprId: item.handlerList[0].oprId, //节点人员域账号
              lastNodeId: nodeId, //上一链接节点
              add: item.add, //新增传true
              isNewAdd: item.isNewAdd,
              editable: item.editable,
            });
          }
          // 删除的数组
          if (item.delete == true) {
            this.dellist.push({
              nodeId: item.nodeId,
              delete: item.delete,
            });
          }
          nodeId = item.nodeId;
        });
        this.approveList = [...this.approveList];
      },
      deep: true,
      immediate: true,
    },
  },
  computed: {
    //所有节点取这个approveList
    approveList: {
      get() {
        return this.apprList;
      },
      set(val) {},
    },
  },
  created() {},
  mounted() {
    window.addEventListener(
      "message",
      (event) => {
        this.dealMessage(event);
      },
      false
    );
    this.getOrganization();
  },
  methods: {
    // 获取组织架构地址
    async getOrganization() {
      let that = this;
      this.$refs.loadings.show();
      try {
        let res = await getList(this.getOrganizationUrl);
        let data = res.data;
        this.$refs.loadings.hide();
        if (data.status == "success") {
          this.deptUrl = data.result;
        } else {
          that.$Modal.error({
            title: "提示",
            content: data.message,
          });
        }
      } catch (error) {
        this.$refs.loadings.hide();
        that.$Modal.error({
          title: "提示",
          content: error,
        });
      }
    },
    //监听事件
    dealMessage(event) {
      let that = this;
      if (
        event.data &&
        event.data instanceof Array &&
        event.data.length &&
        event.data.length > 0
      ) {
        this.changeInformation = false;
        //获取当前登录人
        if (event.data.length > 1) {
          this.$Modal.confirm({
            title: "信息",
            content: `您选择了${event.data.length}条数据，默认选择第一条`,
            onOk: () => {
              let obj = event.data[0];
              // this.addObj.oprId = obj.OPRID;
              this.approveList.splice(this.addIdx + 1, 0, {
                add: true, //新增传true
                isNewAdd: true,
                editable: true,
                handlerList: [
                  {
                    emplId: obj.EMPLID,
                    longName: "",
                    name: obj.LAST_NAME,
                    oprId: obj.OPRID,
                    pinyin: "",
                  },
                ],
                nodeDescr: this.userInfoName + "插入的审批人", //节点名称
                approvalStatus: "未发送", //节点状态
                nodeId: this.addObj.nodeId, //节点ID，不可与现存节点重复
                lastNodeId: this.addObj.lastNodeId, //上一链接节点
              });
            },
            onCancel: () => {},
          });
        } else {
          let obj = event.data[0];
          // this.addObj.oprId = obj.OPRID;
          this.approveList.splice(this.addIdx + 1, 0, {
            add: true, //新增传true
            isNewAdd: true,
            editable: true,
            handlerList: [
              {
                emplId: obj.EMPLID,
                longName: "",
                name: obj.LAST_NAME,
                oprId: obj.OPRID,
                pinyin: "",
              },
            ],
            nodeDescr: this.userInfoName + "插入的审批人", //节点名称
            approvalStatus: "未发送", //节点状态
            nodeId: this.addObj.nodeId, //节点ID，不可与现存节点重复
            lastNodeId: this.addObj.lastNodeId, //上一链接节点
          });
        }
      }
      if (event.data && event.data == "cancel") {
        this.changeInformation = false;
      }
    },
    //添加节点
    addPer(appro, idx) {
      this.upOneLevelObj = appro;
      this.addObj.add = true; //新增传true
      this.addObj.isNewAdd = true;
      this.addObj.nodeCn = "审批节点"; //节点名称可选
      this.addObj.lastNodeId = this.upOneLevelObj.nodeId; //上一链接节点
      this.addObj.editable = true;
      let numArr = [];
      this.approveList.map((item) => {
        numArr.push(Number(item.nodeId.split("N")[1]));
      });
      numArr = numArr.sort((a, b) => a - b);
      this.addObj.nodeId = "N" + (Number(numArr[numArr.length - 1]) + 1); //节点ID，不可与现存节点重复
      this.addIdx = idx;
      this.addPeople();
    },
    //删除节点
    delePer(appro, index) {
      // 新增的节点直接删除
      if (this.approveList[index].isNewAdd == true) {
        this.approveList.splice(index, 1);
      } else {
        // 接口返回的数据增加删除状态
        this.$set(this.approveList[index], "delete", true);
      }
    },
    // 新增
    addPeople() {
      this.changeInformation = true;
      this.$refs.iframe.src = this.deptUrl;
    },
    //多审批人弹窗列表显示
    handlerModalShow(handlerModalList) {
      this.handlerMore = true;
      this.handlerModalList = handlerModalList;
    },
  },
  // beforeRouteEnter(to, from, next) {
  //   next((vm) => {
  //     console.log(from, "from");
  //     //  这里的vm指的就是vue实例，可以用来当做this使用
  //     vm.fromPath = from.path; //获取上级路由的路径
  //   });
  // },
  beforeDestroy() {
    window.removeEventListener("message", this.dealMessage, false);
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
  width: 100%;
  margin-bottom: 5px;
  border-radius: 2px;
  background: rgba(230, 239, 255, 0.5);
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}
.approveDivLeft {
  width: 100%;
  margin-top: 10px;
  border-radius: 2px;
  background: rgba(230, 239, 255, 0.5);
  text-align: center;
}
.liucheng_warp {
  display: block;
  // overflow: auto;
  text-align: center;
  line-height: 18px;
  letter-spacing: 0px;
  color: #444444;
  width: 100%;
  .titleText {
    padding-top: 25px;
    padding-bottom: 30px;
    font-size: 16px;
    font-weight: bolder;
  }
  .horizontalLine {
    width: 160px;
    height: 2px;
    opacity: 1;
    background: #c8d9f6;
    display: inline-block;
    position: relative;
    top: -4px;
  }
}
.liucheng_table {
  float: left;
  td {
    height: 130px;
    position: relative;
  }
}
.startImg {
  float: left;
  position: relative;
  top: -10px;
  left: 20px;
}
.endImg {
  float: left;
  position: relative;
  top: -10px;
  left: 125px;
}
.step_jia {
  position: absolute;
  top: 67px;
  left: 4px;
  font-size: 32px;
  color: #2d78ff;
}
.marginLeft32 {
  margin-left: 32px;
}
.step_xia {
  position: absolute;
  top: 67px;
  right: 0px;
  font-size: 32px;
  color: #2d78ff;
}
.top65 {
  top: -65px;
}
.addBtnBlue {
  position: absolute;
  top: 67px;
  right: 0px;
  z-index: 2;
  cursor: pointer;
  color: #2d78ff;
}
.deleBtn {
  position: absolute;
  right: 48px;
  top: 18px;
  cursor: pointer;
  font-size: 16px;
  color: #ff3b57;
}
.top15 {
  // top: 15px;
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

.dept .sureBtn {
  border: 1px solid #faa001;
  background: #faa001;
}
.dept .cancleBtn {
  color: #faa001;
  border: 1px solid #faa001;
}
//流程中-主题色
.blue_liucheng {
  width: 190px;
  height: 156px;
  overflow: hidden;
  display: inline-block;
  background-image: url("../../../public/static/img/lan.png");
  background-size: 100%;
  margin-right: 32px;

  .blue_inner {
    .stateText {
      font-size: 16px;
      font-weight: bolder;
      line-height: 16px;
      letter-spacing: 0px;
      text-align: center;
      margin-top: 22px;
    }
    .themeColor {
      color: #2d78ff;
    }
    .grayColor {
      color: #cdcfd4;
    }
    .redColor {
      color: #ff3b57;
    }
    .roleText {
      margin-top: 14px;
      font-size: 12px;
      font-weight: bold;
      line-height: 12px;
      letter-spacing: 0px;
      color: #444444;
      text-align: center;
      width: 80%;
      display: inline-block;
    }
    a {
      font-size: 14px;
      font-weight: normal;
      line-height: 14px;
      text-align: center;
      letter-spacing: 0px;
      color: #666666;
    }
    .tiemText {
      margin-top: 7px;
      font-size: 12px;
      font-weight: normal;
      line-height: 12px;
      letter-spacing: 0px;
      color: #666666;
      text-align: center;
    }
  }
  .spr_a {
    color: #515a6e;
    font-size: 12px;
    cursor: default;
  }
}
//其他颜色背景色
.backgroundHong {
  background: url("../../../public/static/img/hong.png");
  background-size: 100%;
}
// .backgroundHui {
//   background: url("../../../public/static/img/hui.png");
//   background-size: 100%;
// }
.backgroundHui {
  background: url("../../../public/static/img/hui.png");
  background-size: 100%;
}
.backgroundCheng {
  background: url("../../../public/static/img/cheng.png");
  background-size: 100%;
}
.backgroundHan {
  background: url("../../../public/static/img/lan.png");
  background-size: 100%;
}
// 开始
.startText {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  opacity: 1;
  background: #2d78ff;
  text-align: center;
  padding: 16px;
  position: relative;
  top: -5px;
  span {
    font-size: 16px;
    font-weight: normal;
    line-height: 16px;
    color: #ffffff;
    letter-spacing: 0px;
    height: 32px;
    width: 32px;
    display: flex;
  }
}
// 流程中

//结束
.endText {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  opacity: 1;
  background: #cdcfd4;
  text-align: center;
  padding: 16px;
  position: relative;
  top: -5px;
  span {
    font-size: 16px;
    font-weight: normal;
    line-height: 16px;
    color: #ffffff;
    letter-spacing: 0px;
    height: 32px;
    width: 32px;
    display: flex;
  }
}
.endTextColor {
  background: #fa7e23;
}
.boderBom {
  text-align: left;
  padding: 0 16px 8px 45px;
  font-size: 12px;
}
.marTop7 {
  margin-top: 7px;
}
.marTop15 {
  margin-top: 15px;
}
.colorgray {
  color: #cdcfd4;
}
.tableAppr {
  margin-bottom: 25px;
  overflow-x: auto;
  // width: 100%;
  display: block;
  margin: 0px 20px;
}
.tbodyAppr {
  width: 100%;
  text-align: center;
  // display: flex;
  // justify-content: center;
  // align-items: center;
  display: -webkit-box; /* Chrome 4+, Safari 3.1, iOS Safari 3.2+ */
  display: -moz-box; /* Firefox 17- */
  display: -webkit-flex; /* Chrome 21+, Safari 6.1+, iOS Safari 7+, Opera 15/16 */
  display: -moz-flex; /* Firefox 18+ */
  display: -ms-flexbox; /* IE 10 */
  // display: flex; /* Chrome 29+, Firefox 22+, IE 11+, Opera 12.1/17/18, Android 4.4+ */
  -webkit-box-pack: center;
  -ms-flex-pack: center;
  justify-content: center;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
}
.handlerModalBtn {
  color: #2d78ff;
  cursor: pointer;
}
.spr_a_modal {
  color: #515a6e;
  font-size: 12px;
  cursor: default;
}
</style>
