<!-- 架构页面 -->
<template>
  <div class="layoutsWrapper">
    <LeftSide></LeftSide>
    <div class="contain fx-1">
      <Header></Header>
      <router-view class="mainWrapper" />
    </div>
  </div>
</template>
<script setup>
import LeftSide from "./components/LeftSide.vue";
import Header from "./components/Header.vue";
import { onMounted } from 'vue';
import { getTypeAll } from "@/api/api";
import { catchDataesStore } from '@/store';

let store = catchDataesStore();

onMounted(() => {
  if(!store.getCategoryTree){
     getTypeAll({admin:true})
        .then((res) => {
          if (res.code === 200) {
            store.setCategoryTree(res.data);
            let map = {};
            res.data.forEach(f => {
              map[f.id] = {id:f.id, name:f.name}
              f.children.forEach(s => {
                map[s.id] = {id:s.id, name:s.name, parentId: s.parentId}
                s.children.forEach(t => map[t.id] = {id:t.id, name:t.name, parentId:t.parentId})
              })
            })
            store.setCategoryMap(map);
          }
        })
        .catch((err) => console.log(err))
  }
});
</script>
<style lang="scss" scoped>
.layoutsWrapper {
  // min-height: 100vh;
  display: flex;
  .contain {
    padding-left: 226px;
    overflow: hidden;
    min-width: 1366px;
  }
  .mainWrapper {
    padding-top: 66px;
    padding-left: 20px;
    padding-right: 20px;
    // padding-bottom: 20px;
  }
}
</style>