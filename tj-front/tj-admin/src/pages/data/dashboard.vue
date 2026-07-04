<template>
    <div class="kibana-dashboard-container">
        <div v-if="showError" class="error-container">
            <div class="error-icon">
                <svg t="1750485736348" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="64" height="64">
                    <path d="M512 938.666667C276.362667 938.666667 85.333333 747.637333 85.333333 512S276.362667 85.333333 512 85.333333s426.666667 191.029333 426.666667 426.666667-191.029333 426.666667-426.666667 426.666667z m0-64c200.298667 0 362.666667-162.368 362.666667-362.666667S712.298667 149.333333 512 149.333333 149.333333 311.701333 149.333333 512s162.368 362.666667 362.666667 362.666667z m-85.333333-266.666667l85.333333 128 85.333334-128h-170.666666z m0-181.333334l85.333333 128 85.333334-128h-170.666666z" fill="#999" />
                </svg>
            </div>
            <h2>数据大屏服务暂不可用</h2>
            <p>Kibana服务未启动或网络连接失败</p>
            <p>请检查以下服务是否正常运行：</p>
            <ul>
                <li>Elasticsearch (端口: 9200)</li>
                <li>Kibana (端口: 5601)</li>
            </ul>
            <el-button type="primary" @click="refreshPage">刷新页面</el-button>
        </div>
        <div v-else class="iframe-wrapper">
            <iframe
                ref="kibanaIframe"
                :src="kibanaUrl"
                frameborder="0"
                width="100%"
                :height="iframeHeight"
                allowfullscreen
                @load="onIframeLoad"
                @error="onIframeError"
            ></iframe>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

// 配置项（可根据实际情况调整）
const props = defineProps({
    kibanaUrl: {
        type: String,
        default: 'http://192.168.227.128:5601/app/dashboards', // Kibana 仪表盘列表首页
    },
    pageTitle: {
        type: String,
        default: '数据大屏',
    }
});

const iframeHeight = ref('80vh');
const iframeRef = ref(null);
const isFullScreen = ref(false);
const showError = ref(false);
let errorTimeout = null;

const refreshIframe = () => {
    if (iframeRef.value) {
        iframeRef.value.contentWindow?.location.reload();
    }
};

const refreshPage = () => {
    showError.value = false;
    if (errorTimeout) {
        clearTimeout(errorTimeout);
    }
    errorTimeout = setTimeout(() => {
        if (iframeRef.value && iframeRef.value.contentWindow) {
            try {
                const doc = iframeRef.value.contentDocument || iframeRef.value.contentWindow.document;
                if (doc && doc.body) {
                    const bodyText = doc.body.innerText || doc.body.textContent || '';
                    if (bodyText.includes('拒绝连接') || bodyText.includes('无法访问')) {
                        showError.value = true;
                    }
                }
            } catch (e) {
                showError.value = true;
            }
        } else {
            showError.value = true;
        }
    }, 5000);
};

const onIframeLoad = () => {
    if (errorTimeout) {
        clearTimeout(errorTimeout);
    }
    showError.value = false;
};

const onIframeError = () => {
    showError.value = true;
};

window.addEventListener('resize', () => {
    iframeHeight.value = window.innerHeight * 0.8 + 'px';
});

onMounted(() => {
    errorTimeout = setTimeout(() => {
        if (iframeRef.value && iframeRef.value.contentWindow) {
            try {
                const doc = iframeRef.value.contentDocument || iframeRef.value.contentWindow.document;
                if (doc && doc.body) {
                    const bodyText = doc.body.innerText || doc.body.textContent || '';
                    if (bodyText.includes('拒绝连接') || bodyText.includes('无法访问')) {
                        showError.value = true;
                    }
                }
            } catch (e) {
                showError.value = true;
            }
        } else {
            showError.value = true;
        }
    }, 5000);
});

onUnmounted(() => {
    if (errorTimeout) {
        clearTimeout(errorTimeout);
    }
});
</script>

<style scoped lang="scss">
.kibana-dashboard-container {
    padding: 20px;
    height: 100vh;
    box-sizing: border-box;

    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;

        h1 {
            margin: 0;
            font-size: 24px;
            color: #333;
        }
    }

    .iframe-toolbar {
        display: flex;
        gap: 10px;
    }

    .iframe-wrapper {
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

        iframe {
            min-height: 700px;
            transition: height 0.3s ease;
        }
    }

    .error-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 60vh;
        text-align: center;

        .error-icon {
            margin-bottom: 20px;
        }

        h2 {
            font-size: 24px;
            color: #333;
            margin-bottom: 10px;
        }

        p {
            font-size: 14px;
            color: #666;
            margin-bottom: 10px;
        }

        ul {
            text-align: left;
            margin-bottom: 20px;
            padding-left: 20px;

            li {
                font-size: 14px;
                color: #666;
                margin-bottom: 5px;
            }
        }
    }

    .header,
    .sidebar {
        transition: all 0.3s ease;
    }

    .hidden {
        display: none;
    }
}
</style>