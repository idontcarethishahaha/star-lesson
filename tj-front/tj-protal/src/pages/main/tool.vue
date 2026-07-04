<template>
    <div class="tool-market">
        <div class="container" style="display: flex; margin-bottom: 20px;">
            <Breadcrumb data="工具市场"/>
            <div class="btn" style="float: right; position: absolute; top: 90px; right: 60px;">
                <span class="bt bt-round" style="margin-right: 10px;" @click="activeTab = 'installed'">已安装</span>
                <span class="bt bt-round" style="margin-right: 10px;" @click="activeTab = 'market'">工具市场</span>
                <span class="bt bt-round bt-primary" @click="openCreateDialog">创建工具</span>
            </div>
        </div>

        <!-- 工具市场 -->
        <div v-show="activeTab === 'market'" class="tool-content">
            <div class="tool-filter">
                <el-input v-model="searchName" placeholder="搜索工具名称" style="width: 200px; margin-right: 10px;" clearable @change="fetchTools" />
                <el-select v-model="searchToolType" placeholder="工具类型" style="width: 150px; margin-right: 10px;" clearable @change="fetchTools">
                    <el-option label="MCP" value="MCP" />
                    <el-option label="Function" value="FUNCTION" />
                </el-select>
                <el-button @click="fetchTools">搜索</el-button>
            </div>

            <div class="tool-list">
                <div v-if="loading" class="loading">加载中...</div>
                <div v-else-if="tools.length === 0" class="empty">暂无工具</div>
                <div v-else class="tool-grid">
                    <div v-for="tool in tools" :key="tool.id" class="tool-card">
                        <div class="tool-icon">
                            <img v-if="tool.icon" :src="tool.icon" alt="icon" />
                            <span v-else class="icon-placeholder">{{ tool.name?.charAt(0) || 'T' }}</span>
                        </div>
                        <div class="tool-info">
                            <div class="tool-name">{{ tool.name }}</div>
                            <div class="tool-subtitle">{{ tool.subtitle || '暂无描述' }}</div>
                            <div class="tool-desc">{{ tool.description || '' }}</div>
                            <div class="tool-tags">
                                <el-tag v-if="tool.toolType" size="small" type="info">{{ tool.toolType }}</el-tag>
                                <el-tag v-if="tool.labels?.length" v-for="label in tool.labels.slice(0, 3)" :key="label" size="small">{{ label }}</el-tag>
                            </div>
                            <div class="tool-meta">
                                <span>版本: {{ tool.version || '1.0.0' }}</span>
                                <span v-if="tool.versionCount">工具数: {{ tool.toolList?.length || 0 }}</span>
                            </div>
                        </div>
                        <div class="tool-actions">
                            <el-button v-if="tool.installed" type="success" size="small" disabled>已安装</el-button>
                            <el-button v-else type="primary" size="small" @click="installTool(tool)">安装</el-button>
                            <el-button size="small" @click="viewToolDetail(tool)">详情</el-button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="pagination" v-if="total > 0">
                <el-pagination
                    v-model:current-page="currentPage"
                    :page-size="pageSize"
                    :total="total"
                    layout="prev, pager, next"
                    @current-change="handlePageChange"
                />
            </div>
        </div>

        <!-- 已安装工具 -->
        <div v-show="activeTab === 'installed'" class="tool-content">
            <div v-if="loading" class="loading">加载中...</div>
            <div v-else-if="installedTools.length === 0" class="empty">暂无已安装的工具</div>
            <div v-else class="tool-grid">
                <div v-for="tool in installedTools" :key="tool.id" class="tool-card installed">
                    <div class="tool-icon">
                        <img v-if="tool.icon" :src="tool.icon" alt="icon" />
                        <span v-else class="icon-placeholder">{{ tool.name?.charAt(0) || 'T' }}</span>
                    </div>
                    <div class="tool-info">
                        <div class="tool-name">{{ tool.name }}</div>
                        <div class="tool-subtitle">{{ tool.subtitle || '暂无描述' }}</div>
                        <div class="tool-desc">{{ tool.description || '' }}</div>
                        <div class="tool-tags">
                            <el-tag v-if="tool.labels?.length" v-for="label in tool.labels.slice(0, 3)" :key="label" size="small">{{ label }}</el-tag>
                        </div>
                        <div class="tool-meta">
                            <span>已安装版本: {{ tool.installedVersion || tool.version }}</span>
                            <span>工具数: {{ tool.toolList?.length || 0 }}</span>
                        </div>
                    </div>
                    <div class="tool-actions">
                        <el-button type="danger" size="small" @click="uninstallTool(tool)">卸载</el-button>
                        <el-button size="small" @click="viewToolDetail(tool)">详情</el-button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 创建工具对话框 -->
        <el-dialog v-model="createDialogVisible" title="创建工具" width="600px" @close="handleCreateDialogClose">
            <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
                <el-form-item label="工具名称" prop="name">
                    <el-input v-model="createForm.name" placeholder="请输入工具名称" />
                </el-form-item>
                <el-form-item label="图标" prop="icon">
                    <el-input v-model="createForm.icon" placeholder="图标URL（可选）" />
                </el-form-item>
                <el-form-item label="副标题" prop="subtitle">
                    <el-input v-model="createForm.subtitle" placeholder="简短描述" />
                </el-form-item>
                <el-form-item label="描述" prop="description">
                    <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="详细描述工具功能" />
                </el-form-item>
                <el-form-item label="工具类型" prop="toolType">
                    <el-select v-model="createForm.toolType" placeholder="选择工具类型" style="width: 100%;">
                        <el-option label="MCP" value="MCP" />
                        <el-option label="Function" value="FUNCTION" />
                    </el-select>
                </el-form-item>
                <el-form-item label="上传类型" prop="uploadType">
                    <el-select v-model="createForm.uploadType" placeholder="选择上传类型" style="width: 100%;">
                        <el-option label="GitHub" value="GITHUB" />
                        <el-option label="NPM" value="NPM" />
                        <el-option label="Docker" value="DOCKER" />
                        <el-option label="手动" value="MANUAL" />
                    </el-select>
                </el-form-item>
                <el-form-item label="上传地址" prop="uploadUrl">
                    <el-input v-model="createForm.uploadUrl" placeholder="GitHub URL 或其他地址" />
                </el-form-item>
                <el-form-item label="标签" prop="labels">
                    <el-select v-model="createForm.labels" multiple placeholder="选择标签" style="width: 100%;">
                        <el-option label="教育" value="教育" />
                        <el-option label="电商" value="电商" />
                        <el-option label="客服" value="客服" />
                        <el-option label="数据分析" value="数据分析" />
                        <el-option label="内容创作" value="内容创作" />
                    </el-select>
                </el-form-item>
                <el-form-item label="全局工具" prop="isGlobal">
                    <el-switch v-model="createForm.isGlobal" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="createDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitCreateForm">创建</el-button>
            </template>
        </el-dialog>

        <!-- 工具详情对话框 -->
        <el-dialog v-model="detailDialogVisible" title="工具详情" width="700px">
            <div v-if="currentTool" class="tool-detail">
                <div class="detail-header">
                    <div class="detail-icon">
                        <img v-if="currentTool.icon" :src="currentTool.icon" alt="icon" />
                        <span v-else class="icon-placeholder large">{{ currentTool.name?.charAt(0) || 'T' }}</span>
                    </div>
                    <div class="detail-title">
                        <h3>{{ currentTool.name }}</h3>
                        <p>{{ currentTool.subtitle }}</p>
                    </div>
                </div>
                <div class="detail-body">
                    <div class="detail-section">
                        <h4>描述</h4>
                        <p>{{ currentTool.description || '暂无描述' }}</p>
                    </div>
                    <div class="detail-section">
                        <h4>工具列表 ({{ currentTool.toolList?.length || 0 }})</h4>
                        <div class="tool-def-list" v-if="currentTool.toolList?.length">
                            <div v-for="(toolDef, index) in currentTool.toolList" :key="index" class="tool-def-item">
                                <div class="tool-def-name">{{ toolDef.name }}</div>
                                <div class="tool-def-desc">{{ toolDef.description || '暂无描述' }}</div>
                            </div>
                        </div>
                        <p v-else>暂无工具定义</p>
                    </div>
                    <div class="detail-section">
                        <h4>标签</h4>
                        <el-tag v-if="currentTool.labels?.length" v-for="label in currentTool.labels" :key="label" style="margin-right: 5px;">{{ label }}</el-tag>
                        <span v-else>暂无标签</span>
                    </div>
                    <div class="detail-section">
                        <h4>版本历史</h4>
                        <div v-if="toolVersions.length" class="version-list">
                            <div v-for="version in toolVersions" :key="version.id" class="version-item">
                                <span class="version-name">v{{ version.version }}</span>
                                <span class="version-date">{{ version.createdAt }}</span>
                                <span class="version-log">{{ version.changeLog || '无更新日志' }}</span>
                            </div>
                        </div>
                        <p v-else>暂无版本</p>
                    </div>
                </div>
            </div>
            <template #footer>
                <el-button @click="detailDialogVisible = false">关闭</el-button>
                <el-button v-if="currentTool && !currentTool.installed" type="primary" @click="installTool(currentTool)">安装</el-button>
                <el-button v-else-if="currentTool && currentTool.installed" type="danger" @click="uninstallTool(currentTool)">卸载</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getTools, installTool as installToolApi, uninstallTool as uninstallToolApi, getInstalledTools, getToolVersions } from '@/api/tool.js';
import Breadcrumb from '@/components/Breadcrumb.vue';

const activeTab = ref('market');
const loading = ref(false);
const tools = ref([]);
const installedTools = ref([]);
const searchName = ref('');
const searchToolType = ref('');
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);

// 创建工具
const createDialogVisible = ref(false);
const createFormRef = ref(null);
const createForm = ref({
    name: '',
    icon: '',
    subtitle: '',
    description: '',
    toolType: 'MCP',
    uploadType: 'GITHUB',
    uploadUrl: '',
    labels: [],
    isGlobal: false
});
const createRules = {
    name: [{ required: true, message: '请输入工具名称', trigger: 'blur' }],
    toolType: [{ required: true, message: '请选择工具类型', trigger: 'change' }]
};

// 工具详情
const detailDialogVisible = ref(false);
const currentTool = ref(null);
const toolVersions = ref([]);

// 获取工具列表
const fetchTools = async (page = 1) => {
    loading.value = true;
    try {
        const params = {
            pageNo: page,
            pageSize: pageSize.value,
            name: searchName.value || undefined,
            toolType: searchToolType.value || undefined
        };
        const res = await getTools(params);
        if (res) {
            tools.value = res.list || res.records || [];
            total.value = res.total || 0;
            currentPage.value = res.pageNo || page;
        }
    } catch (error) {
        console.error('获取工具列表失败:', error);
        ElMessage.error('获取工具列表失败');
    } finally {
        loading.value = false;
    }
};

// 获取已安装工具
const fetchInstalledTools = async () => {
    loading.value = true;
    try {
        const res = await getInstalledTools();
        installedTools.value = res || [];
    } catch (error) {
        console.error('获取已安装工具失败:', error);
        ElMessage.error('获取已安装工具失败');
    } finally {
        loading.value = false;
    }
};

// 安装工具
const installTool = async (tool) => {
    try {
        await installToolApi({ toolId: tool.id });
        ElMessage.success('安装成功');
        tool.installed = true;
        fetchInstalledTools();
    } catch (error) {
        ElMessage.error('安装失败: ' + (error.message || '未知错误'));
    }
};

// 卸载工具
const uninstallTool = async (tool) => {
    try {
        await uninstallToolApi(tool.toolId || tool.id);
        ElMessage.success('卸载成功');
        fetchInstalledTools();
        if (detailDialogVisible.value) {
            detailDialogVisible.value = false;
        }
    } catch (error) {
        ElMessage.error('卸载失败: ' + (error.message || '未知错误'));
    }
};

// 查看工具详情
const viewToolDetail = async (tool) => {
    currentTool.value = tool;
    detailDialogVisible.value = true;
    try {
        const res = await getToolVersions(tool.id);
        toolVersions.value = res || [];
    } catch (error) {
        toolVersions.value = [];
    }
};

// 打开创建对话框
const openCreateDialog = () => {
    createDialogVisible.value = true;
};

// 关闭创建对话框
const handleCreateDialogClose = () => {
    createFormRef.value?.resetFields();
    createForm.value = {
        name: '',
        icon: '',
        subtitle: '',
        description: '',
        toolType: 'MCP',
        uploadType: 'GITHUB',
        uploadUrl: '',
        labels: [],
        isGlobal: false
    };
};

// 提交创建表单
const submitCreateForm = async () => {
    try {
        await createFormRef.value.validate();
        await import('@/api/tool.js').then(api => api.createTool(createForm.value));
        ElMessage.success('创建成功');
        createDialogVisible.value = false;
        fetchTools();
    } catch (error) {
        if (error.message) {
            ElMessage.error('创建失败: ' + error.message);
        }
    }
};

// 分页
const handlePageChange = (page) => {
    fetchTools(page);
};

onMounted(() => {
    fetchTools();
    fetchInstalledTools();
});
</script>

<style lang="scss" scoped>
.tool-market {
    margin: 0 20px 20px;

    .tool-content {
        background: #fff;
        border-radius: 8px;
        padding: 20px;
    }

    .tool-filter {
        margin-bottom: 20px;
        display: flex;
        align-items: center;
    }

    .tool-list {
        min-height: 400px;
    }

    .tool-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
        gap: 20px;
    }

    .tool-card {
        border: 1px solid #eee;
        border-radius: 8px;
        padding: 20px;
        transition: all 0.3s;

        &:hover {
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        &.installed {
            border-color: #67c23a;
        }

        .tool-icon {
            width: 60px;
            height: 60px;
            border-radius: 8px;
            overflow: hidden;
            margin-bottom: 15px;

            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }

            .icon-placeholder {
                width: 100%;
                height: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: #fff;
                font-size: 24px;
                font-weight: bold;

                &.large {
                    font-size: 32px;
                }
            }
        }

        .tool-info {
            .tool-name {
                font-size: 18px;
                font-weight: bold;
                margin-bottom: 5px;
            }

            .tool-subtitle {
                color: #666;
                font-size: 14px;
                margin-bottom: 10px;
            }

            .tool-desc {
                color: #999;
                font-size: 13px;
                margin-bottom: 10px;
                overflow: hidden;
                text-overflow: ellipsis;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
            }

            .tool-tags {
                margin-bottom: 10px;

                .el-tag {
                    margin-right: 5px;
                    margin-bottom: 5px;
                }
            }

            .tool-meta {
                font-size: 12px;
                color: #999;

                span {
                    margin-right: 15px;
                }
            }
        }

        .tool-actions {
            margin-top: 15px;
            display: flex;
            gap: 10px;
        }
    }

    .loading, .empty {
        text-align: center;
        padding: 60px 0;
        color: #999;
    }

    .pagination {
        margin-top: 20px;
        display: flex;
        justify-content: center;
    }
}

.tool-detail {
    .detail-header {
        display: flex;
        align-items: center;
        margin-bottom: 20px;

        .detail-icon {
            width: 80px;
            height: 80px;
            border-radius: 8px;
            overflow: hidden;
            margin-right: 20px;

            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }

            .icon-placeholder {
                width: 100%;
                height: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: #fff;
                font-size: 32px;
                font-weight: bold;
            }
        }

        .detail-title {
            h3 {
                margin: 0 0 5px;
                font-size: 20px;
            }

            p {
                margin: 0;
                color: #666;
            }
        }
    }

    .detail-body {
        .detail-section {
            margin-bottom: 20px;

            h4 {
                margin: 0 0 10px;
                font-size: 14px;
                color: #333;
            }

            p {
                margin: 0;
                color: #666;
                line-height: 1.6;
            }
        }

        .tool-def-list {
            border: 1px solid #eee;
            border-radius: 4px;

            .tool-def-item {
                padding: 10px;
                border-bottom: 1px solid #eee;

                &:last-child {
                    border-bottom: none;
                }

                .tool-def-name {
                    font-weight: bold;
                    margin-bottom: 5px;
                }

                .tool-def-desc {
                    font-size: 12px;
                    color: #999;
                }
            }
        }

        .version-list {
            .version-item {
                display: flex;
                align-items: center;
                padding: 10px;
                border-bottom: 1px solid #eee;

                &:last-child {
                    border-bottom: none;
                }

                .version-name {
                    font-weight: bold;
                    margin-right: 15px;
                }

                .version-date {
                    color: #999;
                    font-size: 12px;
                    margin-right: 15px;
                }

                .version-log {
                    color: #666;
                    font-size: 13px;
                }
            }
        }
    }
}

.btn {
    display: flex;
    align-items: center;
    gap: 10px;
}
</style>
