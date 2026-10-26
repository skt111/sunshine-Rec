<!--
 * @Descripttion: 头部
 * @version: 
 * @Author: wuxiaoling-ghq
 * @LastEditors: wuxiaoling-ghq
-->
<template>
  <div class="headers">
    <div class="leftLogo">
      <img class="imgLogo" src="../../assets/img/head/logo.png" />
      <img class="imgTitle" src="../../assets/img/head/logoTitle.png" />
      <img class="logoBg" src="../../assets/img/head/topBg.png" />
    </div>
    <div class="rightOperate">
      <!-- 默认长服年 -->
      <div class="longServiceYear">
        <span>当前长服年：{{ year }}年</span>
      </div>
      <div class="infos" v-clickoutside="handleClose">
        <img @click="toggleInfo" src="../../assets/img/head/notification.png" />
        <span class="nums" @click="toggleInfo">{{ nums }}</span>
        <div class="searchCon" v-if="show">
          <div v-if="todoList.length > 0" v-for="(per, idx) in todoList" :key="'per' + idx" :class="['oneCon',idx==0?'first':'']"
            @click.stop="showlist(per)">
            <span class="oneTodo" :title="per.todoMsgContent">{{idx+1}}、{{ per.todoMsgContent }}</span>
          </div>
          <div class="moreBtn" v-if="isMore" @click="showMore">
            <span class="moreCon">查看更多</span>
            <Icon type="ios-arrow-down" color="#1890ff" size="28" />
          </div>
          <div class="noCon" v-if="todoList.length == 0">
            <p>暂无数据！</p>
            <img :src="noDataImg" />
          </div>
        </div>
      </div>
      <div class="person">
        <img src="../../assets/img/toPerson.png" />
        <span>{{ emplName }}</span>
      </div>
      <div class="helpInfo" @click="guide">
        <div class="showCon">
          <img class="iconLeft" src="../../assets/img/head/topFile.png" />
          <span>指导文件</span>
          <img class="iconRight" src="../../assets/img/head/topArrowDown.png" />
        </div>
      </div>
      <!-- <div class="quitCon" @click="toQuit">
        <img src="../../assets/img/head/exit.png" />
        <span>注销</span>
      </div> -->
    </div>
  </div>
</template>

<script>
  import util from "@/utils/util";
  import { postDataList } from "../../api/dataRequst";
  export default {
    name: "MainHeader",
    components: {},
    data() {
      return {
        noDataImg:require("../../assets/img/common/noData.png"),
        year: "", //长服年
        nums: 12, //消息信息
        emplName: localStorage.getItem("emplName"),
        currDate: util.getNowFormatDate(), //当前日期
        currWeek: util.getWeekDate(), //当前星期
        pageData: {
          pageNum: 1,
          pageSize: 20
        },
        show: false,
        isMore: false,//是否有查看更多
        todoList: []//待办列表
      };
    },
    computed: {},
    watch: {},
    created() { },
    mounted() {
      // this.initYear();//当前长服年
    },
    methods: {
      //查询待办列表
      async initTodo() {
        try {
          let res = await postDataList(process.env.VUE_APP_CONFIG + "/tm/queryTodoList", this.pageData);
          let data = res.data;
          if (data.status == 'success') {
            //处理跳转
            this.todoList.push.apply(
              this.todoList,
              data.result ? data.result.list : []
            );
            this.pageData.pageNum =
              data.result && data.result.list.length > 0
                ? data.result.pageNum == data.result.pages
                  ? 1
                  : Number(data.result.pageNum) + 1
                : 1;
            this.isMore =
              data.result && data.result.list.length > 0
                ? data.result.pageNum == data.result.pages
                  ? false
                  : true
                : false;
          } else {
            this.$Message.error({
              title: "信息",
              duration: 5,
              closable: true,
              content: data.message,
            });
          }
        } catch (e) {
          this.$Message.error({
            title: "信息",
            duration: 5,
            closable: true,
            content: "请求失败",
          });
        }
      },
      //查看更多
      showMore() {
        this.show = true;
        this.initTodo();
      },
      //收起和展开
      toggleInfo() {
        this.show = !this.show;
        if (this.show) {
          this.pageData.pageNum = 1;
          this.todoList=[];
          this.initTodo();
        }
      },
      //收起待办
      handleClose(){
        this.show=false;
      },
      //打开待办
      showlist(per){
        //per.todoMsgUrl
        //单位对接人的待办统一跳转到单位进度管理列表页；特例和销售导入的审批待办跳到流程审批页面
        this.show=false;
        if(per.todoMsgUrl)
          this.$router.push({
            path:per.todoMsgUrl
          });
      },
      //指导文件
      guide(){
        this.$router.push({
            path:'/config/otherBase/handbook'
          });
      },
      //默认长服年
      async initYear() {
        let that = this;
        try {
          let res = await postDataList(
            process.env.VUE_APP_CONFIG + "/annual/defaultYear"
          );
          let data = res.data;
          if (data.status == "success") {
            that.year = data.result;
          } else {
            this.$Message.error({
              title: "信息",
              duration: 5,
              closable: true,
              content: data.message,
            });
          }
        } catch (error) {
          this.$Message.error({
            title: "信息",
            duration: 5,
            closable: true,
            content: "请求失败",
          });
        }
      },
    },
    beforeDestroy() { },
  };
</script>

<style lang="scss" scoped>
  .headers {
    position: fixed;
    top: 0;
    background: #fff;
    height: 56px;
    width: 100%;
    border-bottom: 1px solid #e9eaf1;
    z-index: 2;
  }

  .leftLogo {
    height: 56px;
    position: relative;
    float: left;

    .imgLogo {
      width: 64px;
      height: 46px;
      left: 16px;
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
    }

    .imgTitle {
      width: 106px;
      height: 17px;
      left: 96px;
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
    }

    .logoBg {
      width: 328px;
      height: 48px;
      left: 216px;
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
    }
  }

  .rightOperate {
    float: right;
    padding-right: 18px;
    padding-top: 4px;
    padding-bottom: 4px;
    font-size: 14px;

    .longServiceYear {
      padding: 4px 0px;
      display: inline-block;
      margin-left: 18px;
      transform: translateY(-20%);
      cursor: pointer;

      span {
        color: #3c4353;
        font-size: 15px;
        vertical-align: middle;
      }
    }

    .infos {
      position: relative;
      display: inline-block;
      padding: 12px;
      cursor: pointer;

      img {
        width: 24px;
        height: 24px;
      }

      .nums {
        width: 20px;
        height: 20px;
        border-radius: 50%;
        color: #fff;
        position: absolute;
        top: 6px;
        right: 2px;
        background: #ff4d4f;
      }
    }

    .person {
      margin-left: 14px;
      display: inline-block;
      padding: 4px 0px;
      transform: translateY(-20%);

      img {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        border: 2px solid #adc6ff;
        vertical-align: middle;
      }

      span {
        margin-left: 10px;
        font-size: 14px;
        color: #3c4353;
        vertical-align: middle;
      }
    }

    .helpInfo {
      margin-left: 28px;
      display: inline-block;
      transform: translateY(-20%);
      cursor: pointer;

      .showCon {
        border: 1px solid #597ef7;
        box-shadow: 0px 2px 0px 0px rgba(0, 0, 0, 0.016);
        background: #f0f5ff;
        padding: 2px 8px;
        border-radius: 2px;

        &:hover {
          span {
            color: #2f54eb;
          }
        }

        .iconLeft {
          width: 14px;
          height: 14px;
          vertical-align: middle;
        }

        span {
          color: #3c4353;
          font-size: 14px;
          margin-left: 8px;
          //margin-right:8px;
        }

        .iconRight {
          width: 14px;
          height: 14px;
          vertical-align: middle;
          display: none;
        }
      }
    }

    .quitCon {
      display: none;
      margin-left: 18px;
      //display:inline-block;
      transform: translateY(-20%);
      cursor: pointer;

      img {
        width: 14px;
        height: 14px;
        margin-right: 4px;
        vertical-align: middle;
      }

      span {
        color: #3c4353;
        font-size: 14px;
      }
    }
  }
  .searchCon {
  overflow-y: auto;
  position: absolute;
  right:0;
  height: 270px;
  width: 460px;
  background: #fff;
  z-index: 3;
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
  border-radius: 0px 0px 5px 5px;
  .oneCon {
    width: 100%;
    float: left;
    //margin-top: 10px;
    padding:5px 20px;
    //height: 32px;
    line-height: 24px;
    color: #3d557d;
    cursor: pointer;
    &.first{
      margin-top:5px;
    }
    &:hover{
      background: #F4F6FA;
    }
    .oneTodo {
      float: left;
      width: 100%;
      text-align: left;
      /* overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap; */
    }
  }
  .moreBtn {
    float: left;
    width: 100%;
    margin-top: 20px;
    cursor: pointer;
    text-align: center;
    .moreCon {
      display: block;
      color: #1890FF;
    }
  }
  .noCon {
    text-align: center;
    padding-top: 50%;
    transform: translateY(-50%);
  }
}
</style>