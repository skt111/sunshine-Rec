/* 供给端雷达原型 · 交互脚本（原生 JS，无依赖）
   依据：需求包 v1.0 主文档 §4.1.4 状态机 / §4.1.5 交互流程 */

(function () {
  'use strict';

  function $(sel, root) { return (root || document).querySelector(sel); }
  function $all(sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); }

  /* 页签切换（分类页签 / 视图切换） */
  document.addEventListener('click', function (e) {
    var tab = e.target.closest('[data-tab]');
    if (tab) {
      var group = tab.getAttribute('data-group');
      $all('[data-group="' + group + '"]').forEach(function (el) { el.classList.remove('active', 'on'); });
      tab.classList.add(tab.classList.contains('tab') ? 'active' : 'on');
      var panel = document.querySelector('[data-panel="' + group + '-' + tab.getAttribute('data-tab') + '"]');
      if (panel) {
        $all('[data-panel-group="' + group + '"]').forEach(function (p) { p.style.display = 'none'; });
        panel.style.display = '';
      }
    }

    /* 快捷时间按钮 */
    var q = e.target.closest('.quick button');
    if (q) {
      $all('.quick button', q.parentNode).forEach(function (b) { b.classList.remove('on'); });
      q.classList.add('on');
    }

    /* 分组展开 */
    var gtoggle = e.target.closest('[data-group-toggle]');
    if (gtoggle) {
      var g = gtoggle.closest('.group');
      if (g) { g.classList.toggle('open'); }
    }
  });

  /* 抽屉控制 */
  window.openDrawer = function (id) {
    var d = document.getElementById(id);
    var m = document.getElementById('mask');
    if (d) { d.classList.add('show'); }
    if (m) { m.classList.add('show'); }
  };
  window.closeDrawer = function () {
    $all('.drawer').forEach(function (d) { d.classList.remove('show'); });
    var m = document.getElementById('mask');
    if (m) { m.classList.remove('show'); }
  };

  /* 立即更新：演示轮询状态（示意，不与真实后端交互） */
  window.demoCrawl = function (btn) {
    if (btn.disabled) { return; }
    btn.disabled = true;
    var raw = btn.textContent;
    btn.textContent = '采集进行中…';
    var steps = ['采集进行中…(1/54)', '采集进行中…(28/54)', '采集完成 · 部分成功'];
    var i = 0;
    var t = setInterval(function () {
      btn.textContent = steps[i];
      i++;
      if (i >= steps.length) {
        clearInterval(t);
        setTimeout(function () {
          btn.disabled = false;
          btn.textContent = raw;
          var st = document.getElementById('crawlState');
          if (st) { st.textContent = '部分成功'; st.className = 'v'; }
        }, 1200);
      }
    }, 900);
  };

  /* 重置筛选（示意） */
  window.resetFilters = function () {
    $all('.filters input').forEach(function (i) { i.value = ''; });
    $all('.quick button').forEach(function (b, idx) { b.classList.toggle('on', idx === 2); });
  };

  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') { window.closeDrawer(); }
  });
})();
