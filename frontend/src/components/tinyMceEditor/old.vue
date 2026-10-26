// 富文本编译器
<template>
  <div class="editorDiv">
    <Editor
      :initial-value="EditorContent"
      @onChange="onChangeHandler"
      id="tinyMce"
      :init="editorInit"
    ></Editor>
  </div>
</template>

<script lang="ts">
import tinymce from "tinymce/tinymce";
import "tinymce/themes/modern/theme";
import Editor from "@tinymce/tinymce-vue";
import {
  Component,
  Vue,
  Model,
  Watch,
  Prop,
  Emit
} from "vue-property-decorator";

@Component({
  components: {
    Editor
  }
})
export default class Content extends Vue {
  // 父组件传进来的html
  @Prop(String) EditorContent: any;
  // 初始值
  public initialVal: any = "";
  public editorInit: any = {
    language_url: "/adminnew/static/zh_CN.js",
    language: "zh_CN",
    selector: "textarea",
    skin_url: "/adminnew/static/skins/lightgray",
    height: 300,
    // width:600,
    plugins:
      "link lists image code table colorpicker textcolor wordcount contextmenu",
    toolbar:
      "bold italic underline strikethrough | fontsizeselect | forecolor backcolor | alignleft aligncenter alignright alignjustify | bullist numlist | outdent indent blockquote | undo redo | link unlink image code | removeformat"
  };
  //最先开始执行
  created(): void {
    tinymce.init({});
  }
  //监听路由变化
  @Watch("$route")
  onRouteChanged(route: any, oldRoute: any): void {
    this.$forceUpdate();
  }
  /**
   * 富文本内容变化时事件
   */
  @Emit()
  public onChangeHandler(res: any, index: any, tag: any) {
    let html: any = res.level.content;
    this.updateContent(html);
  }
  @Emit()
  updateContent(html: any) {}
}
</script>


<style scoped lang="scss">
.editorDiv {
  padding: 0px 20px 20px 20px;
}
</style>