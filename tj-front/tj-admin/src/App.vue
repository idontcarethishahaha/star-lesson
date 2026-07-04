<!-- 主页面 -->
<template>
  <el-config-provider :locale="locale">
    <router-view></router-view>
  </el-config-provider>
</template>

<script setup>
import { defineComponent } from "vue";
import zhCn from "element-plus/lib/locale/lang/zh-cn";
let locale = zhCn;
</script>
