import axios from "axios";
import store from "../store";
import "../plugins/iview.js";
let utils = {
  /* ---cookie操作---- */
  setCookie: function (c_name, value, expire) {
    var date = new Date();
    date.setSeconds(date.getSeconds() + expire);
    document.cookie =
      c_name + "=" + escape(value) + "; expires=" + date.toGMTString();
    // console.log(document.cookie)
  },
  getCookie: function (c_name) {
    if (document.cookie.length > 0) {
      var c_start = document.cookie.indexOf(c_name + "=");
      if (c_start != -1) {
        c_start = c_start + c_name.length + 1;
        var c_end = document.cookie.indexOf(";", c_start);
        if (c_end == -1) c_end = document.cookie.length;
        return unescape(document.cookie.substring(c_start, c_end));
      }
    }
    return "";
  },
  delCookie: function (c_name) {
    setCookie(c_name, "", -1);
  },
  /*----数字千分符分割 ----*/
  format: function (num) {
    let Num = String(num);
    let reg = /^(-)?(([1-9]\d*)|0)(\.\d{1,2})?$/;
    if (reg.test(Num)) {
      let isNeg = false;
      if (Num.indexOf("-") === 0) {
        isNeg = true;
        Num = Num.replace(/-/, "");
      }
      let b = parseInt(Num).toString(),
        len = b.length,
        newValue;

      if (len <= 3) {
        newValue = b;
      } else {
        let r = len % 3;
        newValue =
          r > 0 ?
          b.slice(0, r) +
          "," +
          b
          .slice(r, len)
          .match(/\d{3}/g)
          .join(",") :
          b
          .slice(r, len)
          .match(/\d{3}/g)
          .join(",");
      }
      // 小数部分
      let idx = Num.indexOf(".");
      if (idx > -1) {
        newValue += Num.substring(idx);
      }
      if (isNeg) newValue = "-" + newValue;
      return newValue;
    } else {
      return Num;
    }
  },

  /*--- 返回处理---- */
  back() {
    let u = navigator.userAgent;
    // android终端
    let isAndroid = u.indexOf("Android") > -1 || u.indexOf("Adr") > -1;
    // ios终端
    let isiOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/);
    if (isAndroid) {
      Native.finish();
    } else if (isiOS) {
      window.location.href = "next://";
    } else {
      history.back();
    }
  },

  //请求统一添加信息
  getPostData() {
    return {
      token: store.state.baseInfo.token,
      emplId: store.state.baseInfo.emplId
    };
  },
  /*--- 判断是否为微信浏览器---- */
  isWeiXin() {
    var ua = window.navigator.userAgent.toLowerCase();
    if (ua.match(/MicroMessenger/i) == "micromessenger") {
      return true;
    } else {
      return false;
    }
  },//获取当前日期函数
  getNowFormatDate() {
    let date = new Date(),
      year = date.getFullYear(), //获取完整的年份(4位)
      month = date.getMonth() + 1, //获取当前月份(0-11,0代表1月)
      strDate = date.getDate() // 获取当前日(1-31)
    if (month < 10) month = `0${month}` // 如果月份是个位数，在前面补0
    if (strDate < 10) strDate = `0${strDate}` // 如果日是个位数，在前面补0
  
    return `${year}年${month}月${strDate}日`;
  },
  //获取当前星期
  getWeekDate() {
    var now = new Date();
    var day = now.getDay();
    var weeks = new Array("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六");
    var week = weeks[day];
    return week;
  },  
  //日期格式转化为字符串形式，格式YYYY-MM
  changeDate(date) {
    date = new Date(date);
    var seperator1 = "-";
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    if (month >= 1 && month <= 9) {
      month = "0" + month;
    }
    var currentdate = year + seperator1 + month;
    return currentdate;
  },
  //日期格式转化为字符串形式，格式YYYY-MM
  changeDay(date) {
    if (!date) return false;
    if (typeof date == "string" || typeof date == "number") {
      date = new Date(date);
    }
    var seperator1 = "-";
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var day = date.getDate();
    if (month >= 1 && month <= 9) {
      month = "0" + month;
    }
    if (day >= 0 && day <= 9) {
      day = "0" + day;
    }
    var currentdate = year + seperator1 + month + seperator1 + day;
    return currentdate;
  },
  //获取本周日期
  getWeek() {
    var today = new Date();
    var weekday = today.getDay();

    var monday = new Date(
      1000 * 60 * 60 * 24 * (1 - weekday) + today.getTime()
    );
    var friday = new Date(
      1000 * 60 * 60 * 24 * (5 - weekday) + today.getTime()
    );

    //本周一的日期 （起始日期）
    var startDate = this.changeDay(monday); // 日期变换
    //本周五的日期 （结束日期）
    var endDate = this.changeDay(friday); // 日期变换

    return {
      start: startDate,
      end: endDate
    };
  },
  //获取当前时间，格式YYYY-MM-DD
  getNowDay() {
    var date = new Date();
    var seperator1 = "-";
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var strDate = date.getDate();
    if (month >= 1 && month <= 9) {
      month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
      strDate = "0" + strDate;
    }
    var currentdate = year + seperator1 + month + seperator1 + strDate;
    return currentdate;
  },
  //获取当前日期的年月，格式YYYY-MM
  getNowDate() {
    var date = new Date();
    var seperator1 = "-";
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    if (month >= 1 && month <= 9) {
      month = "0" + month;
    }
    var currentdate = year + seperator1 + month;
    return currentdate;
  },
  //获取当前时间上一个月最后一天，格式YYYY-MM-DD
  getUpDate() {
    var date = new Date();
    var seperator1 = "-";
    var year = date.getFullYear();
    var month = date.getMonth();
    var day;
    if (month == 0) {
      year = year - 1;
      month = 12;
    }
    if (month >= 1 && month <= 9) {
      month = "0" + month;
    }
    if (
      month == 1 ||
      month == 3 ||
      month == 5 ||
      month == 7 ||
      month == 8 ||
      month == 10 ||
      month == 12
    ) {
      day = 31;
    } else if (month == 2) {
      var cond1 = year % 4 == 0; //条件1：年份必须要能被4整除
      var cond2 = year % 100 != 0; //条件2：年份不能是整百数
      var cond3 = year % 400 == 0; //条件3：年份是400的倍数
      var cond = (cond1 && cond2) || cond3;
      if (cond) {
        day = 29;
      } else {
        day = 28;
      }
    } else {
      day = 30;
    }
    var currentdate = year + seperator1 + month + seperator1 + day;
    return currentdate;
  },
  //判断是否是月份的最后一天
  checkLastDay(currDate) {
    var dates=currDate.split('-'),
    year=dates[0],
    month=dates[1],
    currDay=dates[2],
    isValid=false,//是否是最后一天，默认为否
    day;
    if (
      month == '01' ||
      month == '03' ||
      month == '05' ||
      month == '07' ||
      month == '08' ||
      month == '10' ||
      month == '12'
    ) {
      day = 31;
    } else if (month == '02') {
      var cond1 = year % 4 == 0; //条件1：年份必须要能被4整除
      var cond2 = year % 100 != 0; //条件2：年份不能是整百数
      var cond3 = year % 400 == 0; //条件3：年份是400的倍数
      var cond = (cond1 && cond2) || cond3;
      if (cond) {
        day = 29;
      } else {
        day = 28;
      }
    } else {
      day = 30;
    }
    if(day==currDay){
      isValid=true;
    }
    return isValid;
  },
  //获取当前时间上年，格式YYYY
  getNowYear() {
    var date = new Date();
    var year = date.getFullYear() - 1;
    var currentdate = String(year);
    return currentdate;
  },
  getCurrYear() {
    var date = new Date();
    var year = date.getFullYear();
    var currentdate = String(year);
    return currentdate;
  },
  //获取当前时间上一个月，格式YYYY-MM
  getNowMonth() {
    var date = new Date();
    var seperator1 = "-";
    var year = date.getFullYear();
    var month = date.getMonth();
    if (month == 0) {
      year = year - 1;
      month = 12;
    }
    if (month >= 1 && month <= 9) {
      month = "0" + month;
    }
    var currentdate = year + seperator1 + month;
    return currentdate;
  },
  //获取当前月和上一个月
  getNowLastMonth() {
    var date = new Date();
    var seperator1 = "-";
    var year = date.getFullYear(),
      curryear = date.getFullYear();
    var month = date.getMonth(),
      currMonth = month + 1;
    var day = date.getDate();
    if (month == 0) {
      year = year - 1;
      month = 12;
    }
    if (month >= 1 && month <= 9) {
      month = "0" + month;
    }
    if (day >= 0 && day <= 9) {
      day = "0" + day;
    }
    var lastdate = year + seperator1 + month + seperator1 + day;
    return {
      lastdate: lastdate,
      currdate: curryear + seperator1 + currMonth + seperator1 + day
    };
  },
  //15好之前获取当前时间上上月，15号之后获取上月，格式YYYY-MM
  getNowToMonth() {
    var date = new Date();
    var seperator1 = "-";
    var year = date.getFullYear(),
      month = date.getMonth() + 1,
      day = date.getDate();
    if (day > 15) {
      month = month - 1;
    } else {
      month = month - 2;
    }
    if (month == -1) {
      year = year - 1;
      month = 11;
    }
    if (month == 0) {
      year = year - 1;
      month = 12;
    }
    if (month >= 1 && month <= 9) {
      month = "0" + month;
    }
    var currentdate = year + seperator1 + month;
    return currentdate;
  },
  //20号之前获取当前时间上月，15号之后获取当月，格式YYYY-MM
  getNowToMonth20() {
    var date = new Date();
    var seperator1 = "-";
    var year = date.getFullYear(),
      month = date.getMonth() + 1,
      day = date.getDate();
    if (day <= 20) {
      month = month - 1;
    }
    if (month == 0) {
      year = year - 1;
      month = 12;
    }
    if (month >= 1 && month <= 9) {
      month = "0" + month;
    }
    var currentdate = year + seperator1 + month;
    return currentdate;
  },
  //获取当前时间上一季度的年月日，格式YYYY-MM-DD
  getLastQuarter() {
    var date = new Date();
    var seperator1 = "-",
      monthDay = "";
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var quarter = Math.ceil(month / 3);
    if (quarter == 1) {
      year = year - 1;
      monthDay = "12-31";
    } else if (quarter == 2) {
      monthDay = "03-31";
    } else if (quarter == 3) {
      monthDay = "06-30";
    } else if (quarter == 4) {
      monthDay = "09-30";
    }
    var currentdate = year + seperator1 + monthDay;
    return currentdate;
  },
  //从2018年到当前日期的季度末
  getquarter() {
    var quarterList = [];
    var date = new Date();
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var quarter = Math.ceil(month / 3);
    var val = year - 2018 + 1;
    for (var i = 0; i < val; i++) {
      quarterList = quarterList.concat(this.quarterList(year, quarter));
      year--;
      quarter = 4;
    }
    return quarterList;
  },
  quarterList(year, quarter) {
    var list = {
        1() {
          return {
            year: year - 1,
            monthDay: "12-31"
          };
        },
        2() {
          return {
            year: year,
            monthDay: "03-31"
          };
        },
        3() {
          return {
            year: year,
            monthDay: "06-30"
          };
        },
        4() {
          return {
            year: year,
            monthDay: "09-30"
          };
        }
      },
      listData = [];
    for (var i = quarter; i > 0; i--) {
      listData.push(list[i]().year + "-" + list[i]().monthDay);
    }
    return listData;
  },
  //获取当前日期的月份
  getCurrentMonths() {
    var date = new Date();
    var year = date.getFullYear() + "",
      month = date.getMonth() + 1,
      months = [],
      years = [],
      allMonths = [];
    for (var i = month - 1; i > 0; i--) {
      i = i >= 1 && i <= 9 ? "0" + i : i;
      months.push({
        code: i + "",
        content: i + "",
        checked: false
      });
    }
    for (var i = 12; i > 0; i--) {
      i = i >= 1 && i <= 9 ? "0" + i : i;
      allMonths.push({
        code: i + "",
        content: i + "",
        checked: false
      });
    }
    for (var i = year; i > 2014; i--) {
      years.push({
        code: i + "",
        content: i + "",
        checked: false
      });
    }
    return {
      allMonths: allMonths, //1到12月
      months: month == 1 ? allMonths : months, //从1月到当前月
      currentMonth: month == 1 ?
        "12" : month >= 1 && month <= 9 ?
        "0" + (month - 1) : month - 1 > 9 ?
        month - 1 : "0" + (month - 1), //当年月的上一个月
      years: years, //2015年至今
      currentYear: month == 1 ? year - 1 + "" : year + "" //如果是一月，则是上一年，否则当前年
    };
  },
  //当前数值格式处理
  initFormat(data) {
    var spstr = data.split("");
    if (spstr.length < 5) {
      for (var i = 1; i <= 6 - spstr.length; i++) spstr.splice(0, 0, "000");
    }
    spstr.forEach((item, index) => {
      if (item == "%") {
        spstr[index] = "10";
      } else if (item == ".") {
        spstr[index] = "11";
      } else if (item.length == 1) {
        spstr[index] = "0" + item;
      }
    });
    return spstr;
  },
  //获取当前时间，格式HH-mm
  getNowTime() {
    var date = new Date();
    var seperator1 = "-";
    var hour = date.getHours();
    var minute = date.getMinutes();
    if (hour >= 1 && hour <= 9) {
      hour = "0" + hour;
    }
    if (minute >= 1 && minute <= 9) {
      minute = "0" + minute;
    }
    var currentTime = hour + seperator1 + minute;
    return currentTime;
  },
  //码表缓存
  getSession(type) {
    var dataList = {
      FXLB() {
        return "riskList"; //风险类别
      },
      ZBPL() {
        return "rateList"; //指标频率
      },
      YJZT() {
        return "statusTypeList"; //预警状态
      },
      PHQSJLX() {
        return "eventList"; //产险事件类型
      },
      PHQCPLB() {
        return "productList"; //产险涉及产品类别
      },
      PHQSSXT() {
        return "losePropList"; //产险损失形态
      },
      LHQSSLX() {
        return "loseLifeList"; //产险损失类型
      },
      RISKLEVEL() {
        return "riskLevelList"; //风险偏好层级
      }
    };
    var datas = JSON.parse(localStorage.getItem(dataList[type]()));
    if (!datas) {
      window.axios
        .get("/sysConf/sysConf/rmsCodeList/" + type)
        .then(res => {
          datas = res.data.result;
          localStorage.setItem(dataList[type](), JSON.stringify(datas));
        })
        .catch(error => {
          console.log(error);
        });
      /* window.axios.get('../../static/data/riskType.json').then(res => {
                let riskTypeList = res.data.riskTypeList,
                    rateList = res.data.rateList,
                    statusList = res.data.statusList;
                localStorage.setItem('riskList', JSON.stringify(riskTypeList));
                localStorage.setItem('rateList', JSON.stringify(rateList));
                localStorage.setItem('statusTypeList', JSON.stringify(statusList));
            }).catch(error => {
                //console.log(error);
                that.$Modal.error({
                    title: '信息',
                    content: error
                });
            }); */
    }
    return datas;
  },
  //获取关键字的值'1','2'
  getKeyValue(data, key) {
    let values = "";
    data.map(function (item, index) {
      values += item[key] + (index + 1 == data.length ? "" : ",");
    });
    return values;
  },
  //获取关键字["1","2"]
  getKeyList(data, key) {
    let values = [];
    data.map(function (item) {
      if (key == "month") {
        values.push(item[key] + "月");
      } else {
        if (item[key]) values.push(item[key]);
      }
    });
    return values;
  },
  //获取关键字["1","2"],值小于50不展示
  getKeyListSpecial(data, key) {
    let values = [];
    data.map(function (item) {
      if (item[key] < 50) {
        values.push("");
      } else {
        values.push(item[key]);
      }
    });
    return values;
  },
  //对象转化为数组
  objToArr(obj) {
    var arr = [];
    for (let i in obj) {
      arr.push(obj[i]);
    }
    return arr;
  },
  //对象转化为键值对对象
  objToObj(obj) {
    var objList = {};
    for (var k in obj) {
      objList[k] = {
        name: k,
        value: obj[k]
      };
    }
    return objList;
  },
  //判断是否为空对象
  isEmptyObject(obj) {
    for (var key in obj) {
      return false; //返回false，不为空对象
    }
    return true; //返回true，为空对象
  },
    //判断对象里的元素是否为空
  isNoObject(obj) {
    var isValid=true;
    for (var key in obj) {
      if(!obj[key]){
        isValid=false;
        break;
      }
    }
    return isValid; //返回true，为对象有值
  },
  //判断对象集合里是否有空数据
  isEmptyList(data, fields) {
    let valid = true;
    data.forEach(item => {
      if (!valid) return false;
      for (let obj in item) {
        if (obj == "sigTeamMember" || obj == "emplid") {
          //组员不校验
          valid = valid && true;
        } else if (fields.indexOf(obj) > -1) {
          valid = valid && (item[obj] === "" ? false : true);
        }
        if (!valid) break;
      }
    });
    return valid;
  },
  //深层拷贝对象
  objDeepCopy(source) {
    var sourceCopy = {};
    for (var item in source)
      sourceCopy[item] =
      source[item]!=null&&typeof source[item] === "object" ?
      this.objDeepCopy(source[item]) :
      source[item];
    return sourceCopy;
  },
  //深层拷贝数组
  arrDeepCopy(source) {
    var sourceCopy =
      source instanceof Array ? [] : source instanceof Object ? {} : null;
    for (var item in source) {
      sourceCopy[item] =
        typeof source[item] === "object" ?
        this.arrDeepCopy(source[item]) :
        source[item];
    }
    return sourceCopy;
  },
  groupByFour(data, len) {
    var newData = [],
      lens = len ? len : 4;
    for (var i = 0; i < Math.ceil(data.length / lens); i++) {
      newData.push(data.slice(0 + i * lens, lens + i * lens));
    }
    return newData;
  },
  removeElement(_element) {
    var _parentElement = _element.parentNode;
    if (_parentElement) {
      _parentElement.removeChild(_element);
    }
  },
  setUserImgTalent({
    container = document.getElementById("app"),
    width = "270px",
    height = "270px",
    textAlign = "center",
    textBaseline = "middle",
    font = "18px microsoft yahei",
    fillStyle = "rgba(0, 0, 0, 0.1)",
    content = "请勿外传",
    rotate = "-30",
    zIndex = 1000
  } = {}) {
    var args = arguments[0];
    var canvas = document.createElement("canvas");

    canvas.setAttribute("width", width);
    canvas.setAttribute("height", height);
    var ctx = canvas.getContext("2d");

    ctx.textAlign = textAlign;
    ctx.textBaseline = textBaseline;
    ctx.font = font;
    ctx.fillStyle = fillStyle;
    ctx.rotate((Math.PI / 180) * rotate);
    ctx.fillText(content, parseFloat(width) / 2, parseFloat(height) / 2);

    var base64Url = canvas.toDataURL();
    const watermarkDiv = document.createElement("div");
    watermarkDiv.setAttribute(
      "style",
      `
              position:absolute;
              top:0;
              left:0;
              width:100%;
              height:100%;
              z-index:${zIndex};
              pointer-events:none;
              background-repeat:repeat;
              background-image:url('${base64Url}')`
    );

    container.style.position = "relative";
    container.insertBefore(watermarkDiv, container.firstChild);
  },
  setUserImgModal({
    container = document.getElementById("dicDiv"),
    width = "350px",
    height = "270px",
    textAlign = "center",
    textBaseline = "middle",
    font = "28px microsoft yahei",
    fillStyle = "rgba(0, 0, 0, 0.05)",
    content = "请勿外传",
    rotate = "-30",
    zIndex = 1000
  } = {}) {
    var args = arguments[0];
    var canvas = document.createElement("canvas");

    canvas.setAttribute("width", width);
    canvas.setAttribute("height", height);
    var ctx = canvas.getContext("2d");

    ctx.textAlign = textAlign;
    ctx.textBaseline = textBaseline;
    ctx.font = font;
    ctx.fillStyle = fillStyle;
    ctx.rotate((Math.PI / 180) * rotate);
    ctx.fillText(content, parseFloat(width) / 2, parseFloat(height) / 2);

    var waterOld = document.getElementById("waterImg");
    if (waterOld) {
      this.removeElement(waterOld);
    }

    var base64Url = canvas.toDataURL();
    const watermarkDiv = document.createElement("div");
    watermarkDiv.setAttribute(
      "style",
      `
              position:absolute;
              top:0;
              left:0;
              width:100%;
              height:100%;
              z-index:${zIndex};
              pointer-events:none;
              background-repeat:repeat;
              background-image:url('${base64Url}')`
    );
    watermarkDiv.setAttribute("id", "waterImg");
    container.style.position = "relative";
    container.insertBefore(watermarkDiv, container.firstChild);
  },
  excelDownload(downloadUrl, excelPath) {
    ///导出
    /* var layIndex = layer.load(2, {
            shade: [0.4, '#eeeeee']
        }); */
    var iframeOld = document.getElementsByTagName("iframe");
    if (iframeOld && iframeOld.length > 0) {
      this.removeElement(iframeOld[0]);
    }

    var iframe = document.createElement("iframe");
    //console.log(excelPath);
    iframe.src = downloadUrl + "?path=" + excelPath + "&filePath=" + excelPath;
    if (!( /*@cc_on!@*/ 0)) {
      //if not IE
      iframe.onload = function () {
        //layer.close(layIndex);
      };
    } else {
      iframe.onreadystatechange = function () {
        //文件下载是在http请求的interactive也就是浏览器交互阶段。
        if (iframe.readyState == "interactive") {
          //layer.close(layIndex);
        }
      };
    }
    iframe.style.display = "none";
    document.body.appendChild(iframe);
  },
  //下载文件流
  downloadStream(param) {
    if (!param.data) {
      return;
    }
    let f_start = param.headers["content-disposition"].indexOf("=");
    let fileName = param.headers["content-disposition"].substring(f_start + 1);    
    console.log(fileName);
    let blob = new Blob([param.data], {
      type: "application/octet-stream"
    });
    if ("download" in document.createElement("a")) {
      if (navigator.userAgent.indexOf('Edge') > -1) {
        navigator.msSaveBlob(blob, fileName);
      } else {
        // 非IE下载
        const elink = document.createElement("a");
        elink.download = decodeURIComponent(fileName);
        elink.style.display = "none";
        elink.href = URL.createObjectURL(blob);
        document.body.appendChild(elink);
        elink.click();
        URL.revokeObjectURL(elink.href); // 释放URL 对象
        document.body.removeChild(elink);
      }
    } else {
      // IE10+下载
      navigator.msSaveBlob(blob, fileName);
    }
  },
  /**
   * 动画垂直滚动到页面指定位置
   * @param { Number } currentY 当前位置
   * @param { Number } targetY 目标位置
   */
  scrollAnimation(currentY, targetY) {
    // 获取当前位置方法
    // const currentY = document.documentElement.scrollTop || document.body.scrollTop
    let that = this;
    // 计算需要移动的距离
    let needScrollTop = targetY - currentY;
    let _currentY = currentY;
    setTimeout(() => {
      // 一次调用滑动帧数，每次调用会不一样
      const dist = Math.ceil(needScrollTop / 10);
      _currentY += dist;
      window.scrollTo(_currentY, currentY);
      // 如果移动幅度小于十个像素，直接移动，否则递归调用，实现动画效果
      if (needScrollTop > 10 || needScrollTop < -10) {
        that.scrollAnimation(_currentY, targetY);
      } else {
        window.scrollTo(_currentY, targetY);
      }
    }, 1);
  },
  setUserImg({
    container = document.getElementById("showMainArea"),
    width = "400px",
    height = "380px",
    textAlign = "center",
    textBaseline = "middle",
    font = "18px microsoft yahei",
    fillStyle = "rgba(0, 0, 0, 0.05)",
    content = "请勿外传",
    rotate = "-30",
    zIndex = 1000
  } = {}) {
    var args = arguments[0];
    var canvas = document.createElement("canvas");

    canvas.setAttribute("width", width);
    canvas.setAttribute("height", height);
    var ctx = canvas.getContext("2d");

    ctx.textAlign = textAlign;
    ctx.textBaseline = textBaseline;
    ctx.font = font;
    ctx.fillStyle = fillStyle;
    ctx.rotate((Math.PI / 180) * rotate);
    ctx.fillText(content, parseFloat(width) / 2, parseFloat(height) / 2);

    var waterOld = document.getElementById("waterImg");
    if (waterOld) {
      this.removeElement(waterOld);
    }

    var base64Url = canvas.toDataURL();
    const watermarkDiv = document.createElement("div");
    watermarkDiv.setAttribute(
      "style",
      `
          position:absolute;
          top:0;
          left:0;
          width:100%;
          height:100%;
          z-index:${zIndex};
          pointer-events:none;
          background-repeat:repeat;
          background-image:url('${base64Url}')`
    );
    
    watermarkDiv.setAttribute("id", "waterImg");
    container.style.position = "relative";
    container.insertBefore(watermarkDiv, container.firstChild);
  },
  //算薪期间，人头确认计算
  isInSalary(currMonth, currDay) {
    //获取当前年月
    var isIn = false,
      date = new Date(),
      year = date.getFullYear(),
      month = date.getMonth() + 1,
      monthSalary = date.getMonth() + 1,
      day = date.getDate();
    if (currMonth == '1') {
      //当前月
      if (day >= currDay) {
        monthSalary = month;
        isIn = true;
      } else {
        if (month - 1 == 0) {
          year = year - 1;
        }
        monthSalary = month - 1 == 0 ? 12 : month - 1;
      }
    }
    return {
      year: year,
      month: monthSalary,
      isIn: isIn //是否在期间内
    };
  },
  //list进行分组
  groupList(data) {
    var map = {},
      dest = [];
    for (var i = 0; i < data.length; i++) {
      var ai = data[i];
      if (!map[ai.SEQNO]) {
        dest.push({
          SEQNO: ai.SEQNO,
          EOAWSTEP_STATUS: 'N',
          EOAWSTEPSTAT_DESCR: ai.EOAWSTEPSTAT_DESCR,
          EOAW_STEP_DESCR: ai.EOAW_STEP_DESCR,
          userList: [ai]
        });
        map[ai.SEQNO] = ai;
      } else {
        for (var j = 0; j < dest.length; j++) {
          var dj = dest[j];
          if (dj.SEQNO == ai.SEQNO) {
            dj.userList.push(ai);
            break;
          }
        }
      }
    }
    return dest;
  },
    //ie不兼容多行文字超出时补充...，用js来拼接
    wordLimit(word,wordlength){   //参数分别为：类名，要显示的字符串长度
    	if(word){
    		if(word.length > wordlength){
            	return word.substr(0,wordlength)+'...';      //截取元素的文本的长度并加上省略号
	        }else{
	        	return word;
	        }
    	}else{
    		return '';
    	}
	},
	//判断传入对象中，几个key是否为空
	isObjEmpty(obj,arr){
		var flag = true;
		arr.forEach(function(item1){
			if(!obj[item1]){
				flag = false;
			}
		});
		return flag;
	},
	fetchKeyFromArray(arr,key){
		var keyArr = [];
		arr.forEach(function(item){
			keyArr.push(item[key]);
		});
		return keyArr;
	}
};
export default utils;