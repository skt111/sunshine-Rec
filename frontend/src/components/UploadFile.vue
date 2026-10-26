<template>
  <div class="position">
    <input
      ref="fileInput"
      type="file"
      v-on:change="addFile()"
      name="myfile"
      id="file"
      class="file"
      v-if="isUpdate"
    />
    <div
      id="fileSpan"
      v-if="isUpdate"
      @click="updata"
    >
      <i-button icon="ios-cloud-upload-outline">上传材料</i-button>
    </div>
    <ul
      class="file-list"
      v-if="!isHome && showList"
    >
      <li
        v-for="(item,index) in list"
        class="file-li"
        :key="'file'+index"
      >
        <span>{{index+1}}、</span>
        <a
          target="_blank"
          class="file-Name"
          @click="viewFile(item)"
          :title="item.fileName"
        >{{item.fileName}}</a>
        <input
          type="file"
          class="files"
          ref="filesImg"
          @change="changeFile(index)"
          v-if="isUpdate"
        />
        <a
          @click="deleteFile(index)"
          v-if="isUpdate"
        >删除</a>
      </li>
    </ul>
    <ul
      class="file-list"
      v-if="isHome"
    >
      <li
        v-for="(item,index) in list"
        class="file-li"
        :key="'files'+index"
      >
        <a
          target="_blank"
          class="file-Name"
          @click="viewFile(item)"
          :title="item.fileName"
          :style="isUpdate?'':'border-left:1px solid #e5e5e5;'"
        >{{item.fileName}}</a>
        <a
          @click="deleteFile(index)"
          v-if="isUpdate"
        >删除</a>
      </li>
    </ul>
  </div>
</template>
<script>
/*--------
上传文件：
<UploadFile :list="files" :upUrl="" :isHome="true" :isUpdate="false" @getFile="files"></UploadFile>
list已上传的文件列表[{},{},...,{}] ；
isUpdate是否可以上传，true上传编辑等，false展示；
isHome
upUrl

getFile()返回已上传文件列表信息
--------*/
import util from "@/utils/util";
export default {
  name: "UploadFile",
  props: {
    list: {
      type: Array,
      default: function() {
        return [];
      }
    },
    isUpdate: {
      type: Boolean,
      default: true
    },
    upUrl: {
      type: String,
      default: "/indMan/file/upload/batch?fileType=picLogo"
    },
    postData: {
      type: String,
      default: ""
    },
    isHome: {
      type: Boolean,
      default: false
    },
    uploadType: {
    	type: String,
      default: ""
    },
    businessType: {
    	type: String,
      default: ""
    },
    showList: {
      type: Boolean,
      default: true
    },
  },
  data() {
    return {
    	response:{}
    };
  },
  mounted: function() {},
  methods: {
    updata() {
      this.$refs.fileInput.value = "";
      this.$refs.fileInput.click();
    },
    addFile: function() {
      var that = this;
      // 获取文件对象
      var fileObj = document.getElementById("file").files[0];
      if (!fileObj) {
        return;
      }
      // 接收上传文件的后台地址
      var FileController = this.upUrl;
      //FormData对象
      var form = new FormData();
      // 可以增加表单数据
      //form.append("fileType", "picLogo");
      form.append("file", fileObj); // 文件对象
      if(this.uploadType=='importAndExport'){
      	if(this.businessType){
      		form.append("businessUnit", that.businessType); // 文件对象
      	}else{
          that.$Message.error({
              title: "信息",
              content: "请选择业务单位"
          });
          return false;
      	}
      }
      //可以限制大小
      var size = Math.round(fileObj.size / 1024 / 1024);
      if (size > 5) {
        alert("文件大小不得超过5M");
        return;
      }

      // XMLHttpRequest 对象
      var xhr = new XMLHttpRequest();

      xhr.open("post", FileController, true);
      xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status == 200) {
          var response = xhr.response;
          that.response = response;
          //将图片信息加到data里的list中
          if (that.isHome) {
            //首页只展示一个
            that.list.splice(0, that.list.length);
          }
          if(JSON.parse(response).status=='fail'){
          	alert(JSON.parse(response).message);
          }else{
          	that.list.push({
	            fileName: JSON.parse(response).result.sigAttaName,
	            attachLink: JSON.parse(response).result.sigAttaPath
	          });
	          that.filesSend();
          }
        }
        if (xhr.readyState == 4 && xhr.status != 200) {
          alert(xhr.statusText);
        }
      };
      xhr.send(form);
    },
    changeFile: function(index) {
      var that = this;
      // 获取文件对象
      var fileObj = this.$refs.filesImg[index].files[0];
      if (!fileObj) {
        return;
      }
      var FileController = this.upUrl;
      var form = new FormData();
      form.append("file", fileObj); // 文件对象
      var xhr = new XMLHttpRequest();
      xhr.open("post", FileController, true);
      xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status == 200) {
          var response = xhr.response;
          //将图片信息加到data里的list中
          that.list.splice(index, 1, {
            fileName: JSON.parse(response).result.sigAttaName,
            attachLink: JSON.parse(response).result.sigAttaPath,
            attachType: JSON.parse(response).result.attachType
          });
          that.filesSend();
        }
        if (xhr.status != 200) {
          alert(xhr.statusText);
        }
      };
      xhr.send(form);
    },
    viewFile(item) {
      //util.excelDownload("/indMan/excel/index/down", item.attachLink);
      //下载数据
      let that = this;
      this.axios
        .post(
          "/socSecu/attConfig/download",
          {
            //params: {
              sigAttaPath: item.attachLink,
              sigAttaName: item.fileName
            //}
          },
          {
            responseType: "blob"
          }
        )
        .then(response => {
          if (response.status == 200) {
            util.downloadStream(response);
          } else {
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
        });
    },
    deleteFile: function(index) {
      let that = this;
      this.$refs.fileInput.value = "";
      this.list.splice(index, 1);
      this.filesSend();
    },
    realDelFile: function() {
      let type = this.upUrl.split("=");
      this.axios
        .post("/indMan/file/delete/riskdeclarebook?indexSubject=" + type[1])
        .then(res => {
          console.log("删除成功");
        })
        .catch(error => {
          console.log(error);
        });
    },
    filesSend: function() {
      if(this.uploadType=='importAndExport'){
			this.$emit("getFile", this.response);
      }else{
      	this.$emit("getFile", this.list);
      }
    }
  }
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
.position {
  float: left;
  .file {
    height: 32px;
    position: relative;
    cursor: pointer;
    line-height: 26px;
    width: 100px;
    z-index: 2;
    opacity: 0;
    left: 115px;
  }
  #fileSpan {
    display: inline-block;
    position: absolute;
    left: 125px;
    z-index: 99999;
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
        float: left;
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
</style>