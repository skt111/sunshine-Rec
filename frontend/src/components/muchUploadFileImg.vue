<template>
  <div>
    <ul class="file-list" style="list-style: none">
      <li
        v-for="(item, index) in list"
        class="file-li"
        :key="'file' + index"
        style="display: inline-block"
      >
        <img
          :src="item.fileContent"
          style="width: 60px; height: 60px"
          @click="viewFile(item)"
        />
        <img
          src="../assets/img/common/delete.png"
          @click="deleteFile(item, index)"
          style="
            vertical-align: top;
            width: 18px;
            position: relative;
            right: 10px;
            top: -5px;
            cursor: pointer;
          "
        />
      </li>
    </ul>
    <div class="position" v-if="read != 'read'">
      <input
        ref="fileInput"
        type="file"
        v-on:change="addFile()"
        name="myfile"
        :id="id"
        class="file"
      />
      <div class="fileSpan" @click="updata">
        <i-button icon="ios-cloud-upload-outline">{{ buttonSpan }}</i-button>
      </div>
    </div>
    <Modal
      v-model="contractModal"
      title="文件预览"
      :styles="{ width: '1200px', top: '0px' }"
    >
      <iframe class="prism-player" :src="pdfUrl" width="100%"></iframe>
    </Modal>
    <Loading ref="loadings" style="z-index: 2"></Loading>
  </div>
</template>
<script>
/*--------
上传文件：
<UploadFile :list="files" :upUrl="" @getFile="files"></UploadFile>
list已上传的文件列表[{},{},...,{}] ；
upUrl

getFile()返回已上传文件列表信息
--------*/
import util from "@/utils/util";
import Loading from "@/components/Loading";

export default {
  name: "UploadFile",
  components: {
    Loading,
  },
  props: {
    list: {
      type: Array,
      default: function () {
        return [];
      },
    },
    upUrl: {
      type: String,
      default: "/inductAuto/archives/leaveConnect/upLoadLeaveFiles",
    },
    postData: {
      type: String,
      default: "",
    },
    id: {
      type: String,
      default: "file",
    },
    buttonSpan: {
      type: String,
      default: "上传材料",
    },
    fileType: "",
    eid: "",
    read: "",
    emplId: "",
  },
  data() {
    return {
      pdfUrl: "",
      contractModal: false,
    };
  },
  mounted: function () {},
  methods: {
    updata() {
      this.$refs.fileInput.value = "";
      this.$refs.fileInput.click();
    },
    addFile: function () {
      var that = this;
      if (that.list.length == 10) {
        that.$Message.error({
          title: "信息",
          content: "最多上传十张图片",
        });
        return;
      }
      this.$refs.loadings.show();
      // 获取文件对象
      var fileObj = document.getElementById(this.id).files[0];
      if (!fileObj) {
        this.$refs.loadings.hide();
        return;
      }
      if (!that.eid) {
        this.$Message.info({
          title: "提示",
          content: "请先选择员工",
        });
        return false;
      }
      //FormData对象
      var form = new FormData();
      // 可以增加表单数据
      form.append("fileType", that.fileType);
      form.append("eid", that.eid);
      form.append("emplId", that.emplId);
      // 接收上传文件的后台地址
      var FileController = this.upUrl;
      form.append("file", fileObj); // 文件对象
      //if(fileObj.name.indexOf('.jpg')>-1 || fileObj.name.indexOf('.pdf')>-1){
      //可以限制大小
      /*var size = Math.round(fileObj.size / 1024 / 1024);
      if (size > 5) {
        alert("文件大小不得超过5M");
        return;
      }*/
      // XMLHttpRequest 对象
      var xhr = new XMLHttpRequest();
      xhr.open("post", FileController, true);
      xhr.onreadystatechange = function () {
        if (xhr.readyState == 4 && xhr.status == 200) {
          var response = xhr.response;
          if (JSON.parse(response).result) {
            //将图片信息加到data里的list中
            that.list.push({
              fileContent:
                JSON.parse(response).result.filePath.indexOf(".pdf") > -1
                  ? "../assets/img/common/pdf.png"
                  : URL.createObjectURL(fileObj),
              filePath: JSON.parse(response).result.filePath,
            });
          }
          that.filesSend(xhr.response);
        }
        if (xhr.readyState == 4 && xhr.status != 200) {
          alert(xhr.statusText);
        }
      };
      xhr.send(form);
      /*}else{
      	that.$Message.error({
					title: "信息",
					content: '请下载模板，填写后上传！'
				});
   		}*/
    },
    //切换查看合同类型
    changeContractType(dataLocation) {
      var that = this;
      localStorage.setItem("pdfLocation", dataLocation);
      this.pdfUrl = "";
      this.$nextTick(() => {
        that.pdfUrl = "/static/pdf/web/viewer.html";
      });
    },
    viewFile(item) {
      //点击查看的操作
      let that = this;
      if (item.filePath.split(".")[1] == "pdf") {
        this.contractModal = true;
        that.changeContractType(item.filePath);
      } else {
        this.axios
          .post("/inductAuto/eprotocol/common/queryImgUrl", {
            objectKey: item.filePath, //文件存储的路径
            expiration: 0.9,
          })
          .then((res) => {
            let data = res.data;
            if (data.status == "success") {
              //多个
              // data.result.forEach(item => {
              //window.open('data:image/png;base64,'+data.message, "_blank");
              const img = new Image();
              img.src = "data:image/png;base64," + data.message;
              that.checkImg(img);
              /* const newWin = window.open("", "_blank");
							 newWin.document.write(img.outerHTML);
							 newWin.document.title = "图片预览";
							 newWin.document.close(); */

              //});
            } else {
              that.$Message.error({
                title: "信息",
                content: data.message,
              });
            }
          })
          .catch((error) => {
            //console.log(error);
            that.$Message.error({
              title: "信息",
              content: error,
            });
          });
      }

      /* //下载数据
      let that = this;
      this.axios
        .post(
          "/inductAuto/archives/leaveConnect/downloadLeaveFiles",
          {
              filePath: item.filePath,
              fileType:that.fileType,
              emplId:that.emplId
          },
          {
            responseType: "blob"
          }
        )
        .then(response => {
        	debugger;
          if (response.status == 200) {
          	if (typeof response.headers["content-disposition"] == "undefined") {
              that.$Message.error({
                title: "信息",
                content: "暂无数据,无需下载",
              });
              return false;
            }
            util.downloadStream(response,'decode');
          } else {
          	debugger;
            that.$Message.error({
              title: "信息",
              content: response.statusText
            });
          }
        })
        .catch(error => {
          that.$Message.error({
            title: "信息",
            content: response.statusText
          });
        });*/
    },
    checkImg(img) {
      var that = this,
        height = document.documentElement.clientHeight;

      if (img.height != 0) {
        that.openImg(img, height);
      } else {
        setTimeout(() => {
          that.checkImg(img);
        });
      }
    },
    //打开图片
    openImg(img, height) {
      img.style.height = height > img.height ? img.height : height + "px"; //ie浏览器需要逐个设置属性，否则无效，因为ie下认为属性是只读属性

      const newWin = window.open("", "_blank");
      newWin.document.write(img.outerHTML);
      newWin.document.title = "图片预览";
      newWin.document.body.style.position = "absolute";
      newWin.document.body.style.width = "100%";
      newWin.document.body.style.height = "100%";
      newWin.document.body.style.padding = "0";
      newWin.document.body.style.margin = "0";
      newWin.document.body.style.textAlign = "center";
      newWin.document.close();
    },
    deleteFile: function (item, index) {
      let that = this;
      this.list.splice(index, 1);
      this.axios
        .post("/inductAuto/archives/leaveConnect/delLeaveFiles", {
          fileType: that.fileType,
          filePath: item.filePath,
        })
        .then((res) => {
          debugger;
          if (res.code == "OK") {
            that.$Message.success({
              title: "信息",
              content: "删除成功",
            });
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    realDelFile: function () {
      let type = this.upUrl.split("=");
      this.axios
        .post("/indMan/file/delete/riskdeclarebook?indexSubject=" + type[1])
        .then((res) => {
          console.log("删除成功");
        })
        .catch((error) => {
          console.log(error);
        });
    },
    filesSend: function (response) {
      if (response) {
        this.$emit("getFile", this.list, response);
      } else {
        this.$emit("getFile", this.list);
      }
    },
  },
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
.position {
  float: right;
  .file {
    height: 32px;
    cursor: pointer;
    line-height: 26px;
    width: 100px;
    z-index: 2;
    opacity: 0;
    position: relative;
    left: 100px;
  }
  .fileSpan {
    display: inline-block;
  }
  .file-list {
    list-style: none;
    display: block;
    .file-li {
      height: 32px;
      line-height: 32px;
      position: relative;
      span {
        width: 20px;
        float: left;
      }
      a {
        cursor: pointer;
      }
      .file-Name {
        width: 200px;
        margin-right: 20px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .files {
        position: absolute;
        left: 20px;
        cursor: pointer;
        height: 32px;
        line-height: 32px;
        width: 200px;
        z-index: 2;
        opacity: 0;
      }
    }
  }
}
/deep/ .ivu-modal-body {
  height: 500px;
  .prism-player {
    height: 470px;
  }
}
</style>