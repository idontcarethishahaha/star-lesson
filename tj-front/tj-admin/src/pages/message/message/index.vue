<template>
    <div class="contentBox">
        <div class="bg-wt radius marg-tp-20">
            <div class="tableBox">
                <div class="subHead pad-30">
                    <h3 class="font-bold text-lg mb-4">消息管理</h3>
                    <el-tabs v-model="activeTab" @tab-change="onTabChange">
                        <el-tab-pane name="notice" label="通知消息" />
                        <el-tab-pane name="sms" label="短信消息" />
                    </el-tabs>
                </div>
                <div class="emptyPageInter">
                    <EmptyPage :isSearch="false" :baseData="[]"></EmptyPage>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import EmptyPage from "@/components/EmptyPage/index.vue";

const activeTab = ref('notice');

const onTabChange = () => {
    // 预留，消息列表接口待接入
};
</script>

<style scoped lang="scss">
@import '../index.scss';
</style>