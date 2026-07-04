<template>
    <div class="contentBox">
        <div class="bg-wt radius marg-tp-20">
            <div class="tableBox">
                <div class="subHead pad-30">
                    <el-button class="button primary" style="margin-bottom: 10px;" @click="handleAdd">新增直播</el-button>
                </div>
                <div class="emptyPageInter">
                    <EmptyPage :isSearch="false" :baseData="[]"></EmptyPage>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import EmptyPage from "@/components/EmptyPage/index.vue";

const handleAdd = () => {
    ElMessage.info('直播管理功能开发中');
};
</script>