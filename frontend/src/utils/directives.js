"use strict";

import Vue from 'vue';

var selfDirective = {};
//自定义指令
/*------------
  el:指定所绑定的元素，可以用来直接操作DOM。
  binding：一个对象，包含有I下属性：
    name:指令名，不包括v-前缀。
    value：指定的绑定值，例如：v-my-directive="1+1"中，绑定值为2.
    oldValue:指定绑定的前一个值，仅在update和componentUpdated钩子中可用。无论值是否改变都可用。
    expression：字符串形式的指令表达式。例如v-my-directive="1+1"中，表达式为"1+1"。
    arg:传给指定的参数，可选，例如v-my-directive:foo中，参数为"foo".
    modifiers：一个包含修饰符的对象。例如：v-my-directive.foo.bar中，修饰符对象为{foo:true,bar:true}。
    vnode:Vue编译生成的虚拟节点。
    oldVnode：上一个虚拟节点，仅在update和componentUpdated钩子中可用。
    -------------*/
/*---函数防抖--*/
const debounce = {
    //钩子函数，被绑定元素插入父节点时调用 (父节点存在即可调用，不必存在于 document 中)。
    inserted: function(el, binding) {
        let timer
        el.addEventListener('keyup', () => {
            if (timer) {
                clearTimeout(timer)
            }
            timer = setTimeout(() => {
              if(binding.value)
                binding.value()
            }, 1000);
        })
    }
};
/*----外部关闭下拉菜单--*/
const clickoutside = {
    // 初始化指令:bind：只调用一次，指令第一次绑定到元素时调用。
    bind(el, binding, vnode) {
        function documentHandler(e) {
            // 这里判断点击的元素是否是本身，是本身，则返回
            if (el.contains(e.target)) {
                return false;
            }
            // 判断指令中是否绑定了函数
            if (binding.expression) {
                // 如果绑定了函数 则调用那个函数，此处binding.value就是handleClose方法
                binding.value(e);
            }
        }
        // 给当前元素绑定个私有变量，方便在unbind中可以解除事件监听
        el.__vueClickOutside__ = documentHandler;
        document.addEventListener('click', documentHandler);
    },
    // 所在组件的 VNode 更新时调用，但是可能发生在其孩子的 VNode 更新之前。
    // 指令的值可能发生了改变也可能没有。但是你可以通过比较更新前后的值来忽略不必要的模板更新 
    update() {},
    unbind(el, binding) {
        // 解除事件监听:只调用一次，指令与元素解绑时调用。
        document.removeEventListener('click', el.__vueClickOutside__);
        delete el.__vueClickOutside__;
    }
};
//数字校验

/*
 * [setValueWithExpressionVue 更新数据模型]
 * @param {Boolean} toString    [是否转化为字符串]
 * @param {[type]}  currObj    [当前的数据模型]
 * @param {[type]}  expression [指令表达式]
 * @param {[type]}  value      [指令的值]
 * @param {[type]}  key        [用于批量渲染时的跟踪键]
 * @param {[type]}  arg        [指令的参数]
 */
function setValueWithExpressionVue (option) {
    let expression = option.expression.split('.');
    expression.forEach(function (item, i) {
     if (i < expression.length - 1) {
      option.currObj = option.currObj[item]
     } else {
       if(option.key !== undefined){
         option.currObj[item][option.key][option.arg] = (option.value === "" || option.toString) ? option.value : option.value*1
       }else{
         option.currObj[item] = (option.value === "" || option.toString) ? option.value : option.value*1
       }
     }
    })
}
/*
 * [handleInput 在输入阶段的处理逻辑]
 * @param  {[DOM]} ele   [当前指令操作的dom对象]
 * @param  {[虚拟节点]} vnode [当前指令渲染的虚拟节点]
 * @param  {[指令信息]} rel   [当前指令的所有指令信息]
 * @param  {[校验类型]} type  [输入阶段的校验类型]
 *      "number": 仅支持输入数字
 *      "float": 仅支持数字和小数点
 */
function handleInput(ele,vnode,rel){
    let rule;
    switch(true) {
      case rel.modifiers.float: // 浮点型
        rule = /[^\d\.]/g; break;
      default: //默认仅支持输入数字
        rule = /\D/g;
    }
    let val = ele.value.replace(rule,"");
    let maxLen = vnode.data.attrs && vnode.data.attrs['max-len'] ? vnode.data.attrs['max-len'] :0;
    if(maxLen>0){val = val.substr(0,maxLen)}
    let minVal=vnode.data.attrs && vnode.data.attrs['minVal']!=='' ? vnode.data.attrs['minVal'] :null,
    maxVal=vnode.data.attrs && vnode.data.attrs['maxVal']!=='' ? vnode.data.attrs['maxVal'] :null;
    if(minVal&&val<minVal){val = ""}
    if(maxVal&&val>maxVal){val = ""}
    setValueWithExpressionVue({
      currObj:vnode.context.$data,
      expression:rel.expression,
      value:val,
      key:vnode.key,
      arg:rel.arg,
      toString:rel.modifiers.string || rel.modifiers.float
    })
  }
const isFigure =(el,binding,vnode)=> {
    let ele = el.tagName === 'INPUT' ? el : el.querySelector('input')
    ele.oninput = function() {
      //获取相关的指令配置信息
      let rel = vnode.data.directives.filter(item =>{
        return item.name === "isFigure"
      })[0]
      vnode.context.$nextTick(()=>{
        handleInput(ele,vnode,rel)
      })
    }
};
//邮箱
function handleEmail(ele,vnode,rel){
    let rule;
    switch(true) {
      default: //默认仅支持输入邮箱
        rule = /^[a-zA-Z0-9]+([-_.][a-zA-Z0-9]+)*@[a-zA-Z0-9]+([-_.][a-zA-Z0-9]+)*\.[a-z]{2,}$/g;
    }
    let val = ele.value;
    if(!rule.test(ele.value))
     {
        val = "";
        setValueWithExpressionVue({
        currObj:vnode.context.$data,
        expression:rel.expression,
        value:val,
        key:vnode.key,
        arg:rel.arg,
        toString:rel.modifiers.string || rel.modifiers.float
        })
    }
  }
 const isEmail =(el,binding,vnode)=> {
    let ele = el.tagName === 'INPUT' ? el : el.querySelector('input')
    ele.onblur = function() {
      //获取相关的指令配置信息
      let rel = vnode.data.directives.filter(item =>{
        return item.name === "isEmail"
      })[0]
      vnode.context.$nextTick(()=>{
        handleEmail(ele,vnode,rel)
      })
    }
}; 
//是否必填的
const isRequired =(el,binding,vnode)=> {
  if(el.className.indexOf('required')<0){
    return false;
  }
  let ele = el.tagName === 'INPUT'||  el.tagName === 'TEXTAREA'?el : el.querySelector('input');
  if(el.tagName === 'SELECT'){
    ele= el.tagName === 'SELECT' ? el : el.querySelector('select');
    ele.onchange = function() {
      if(ele.value==''&&ele.className.indexOf('error')<0){
        ele.classList.add("error");
      }
    }
  }else{
    ele.onblur = function() {
      if(ele.value==''&&ele.className.indexOf('error')<0){
        ele.classList.add("error");
      }else if(ele.value!=''){
        ele.classList.remove("error");
      }
    }
  }
  ele.onfocus = function() {
    if(ele.value==''&&ele.className.indexOf('error')>-1){
      ele.classList.remove("error");
    }
  }
}; 
//是否数字
const isNum =(el,binding,vnode)=> {
  if(el.className.indexOf('required')<0){
    return false;
  }
  //填写的话，必须是数字
  var nums=false;
  if(el.className.indexOf('isNums')>-1){
    nums=true;
  }
  let ele = el.tagName === 'INPUT' ? el : el.querySelector('input');
  ele.onblur = function() {
    var rule = /^(([0-9]+\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\.[0-9]+)|([0-9]*[1-9][0-9]*))$/g;
    var isValid=!rule.test(ele.value);
    if(nums&&!ele.value){
      isValid=true;
    }
    if(isValid&&ele.className.indexOf('error')<0){
      ele.classList.add("error");
    }else{
      ele.classList.remove("error");
    }
  }
  ele.onfocus = function() {
    if(ele.className.indexOf('error')>-1){
      ele.classList.remove("error");
    }
  }
}; 
 
//是否必填数字
const isRequireNum =(el,binding,vnode)=> {
  if(el.className.indexOf('isRequireNum')<0){
    return false;
  }
  let ele = el.tagName === 'INPUT' ? el : el.querySelector('input');
  var scope=vnode.data.attrs['scope'],scopeList=scope.split(',');
  var scopeType=vnode.data.attrs['scopeType'];
  if(!scope){
    return false;
  }
  var sysLeft=scopeList[0].substr(0,1),
  sysLeftVal=scopeList[0].substr(1),
  sysRightVal=scopeList[1].substr(0,scopeList[1].length-1),
  sysRight=scopeList[1].substr(-1);
  ele.onblur = function() {
    var rule = /^(([1-9]{1}\d*)|(0{1}))(\.\d{0,2})?$/g;
    if(scopeType&&scopeType=='int'){
      rule=/^[1-9]\d*$/g;//整数
    }
    var isValid=rule.test(ele.value);
    if(Number(ele.value)<Number(sysLeftVal)){
      isValid=isValid&&false;
    }else if(Number(ele.value)==Number(sysLeftVal)&&sysLeft=='['){
      isValid=isValid&&true;
    }else if(Number(ele.value)>Number(sysLeftVal)&&Number(ele.value)<Number(sysRightVal)){
      isValid=isValid&&true;
    }else if(Number(ele.value)==Number(sysRightVal)&&sysRight==']'){
      isValid=isValid&&true;
    }else{
      isValid=isValid&&false;
    }
    
    if(!isValid&&ele.className.indexOf('error')<0){
      ele.classList.add("error");
      ele.value="";
    }else{
      ele.classList.remove("error");
    }
  }
  ele.onfocus = function() {
    if(ele.className.indexOf('error')>-1){
      ele.classList.remove("error");
    }
  }
}; 
selfDirective.install = function(Vue) {
    Vue.directive('debounce', debounce);
    Vue.directive('clickoutside', clickoutside);
    Vue.directive('isFigure', isFigure);
    Vue.directive('isEmail', isEmail);
    Vue.directive('isRequired', isRequired);
    Vue.directive('isNum', isNum);
    Vue.directive('isRequireNum', isRequireNum);
};

Vue.use(selfDirective)

export default selfDirective;