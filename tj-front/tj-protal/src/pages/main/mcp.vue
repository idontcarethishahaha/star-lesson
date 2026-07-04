<template>
    <div class="mcp-page">
        <div class="container" style="display: flex; margin-bottom: 20px;">
            <Breadcrumb data="MCP 服务器管理"/>
            <div class="btn" style="float: right; position: absolute; top: 90px; right: 60px;">
                <span class="bt bt-round bt-primary" @click="openCreateDialog">创建 MCP Server</span>
            </div>
        </div>

        <div class="mcp-content">
            <div v-if="loading" class="loading">加载中...</div>
            <div v-else-if="servers.length === 0" class="empty">
                <p>暂无 MCP Server</p>
                <el-button type="primary" @click="openCreateDialog">创建第一个 MCP Server</el-button>
            </div>
            <div v-else class="server-grid">
                <div v-for="server in servers" :key="server.id" class="server-card" :class="{ running: server.status === 'running' }">
                    <div class="server-header">
                        <div class="server-icon">
                            <span class="icon-text">{{ server.name?.charAt(0).toUpperCase() || 'M' }}</span>
                        </div>
                        <div class="server-info">
                            <div class="server-name">{{ server.name }}</div>
                            <div class="server-type">{{ server.type || 'stdio' }}</div>
                        </div>
                        <div class="server-status">
                            <el-tag :type="server.status === 'running' ? 'success' : 'info'" size="small">
                                {{ server.status === 'running' ? '运行中' : '已停止' }}
                            </el-tag>
                        </div>
                    </div>

                    <div class="server-body">
                        <div class="server-desc">{{ server.description || '暂无描述' }}</div>

                        <div class="server-config">
                            <div class="config-item">
                                <span class="config-label">命令:</span>
                                <code>{{ server.command || 'npx' }}</code>
                            </div>
                            <div class="config-item" v-if="server.args">
                                <span class="config-label">参数:</span>
                                <code>{{ server.args }}</code>
                            </div>
                            <div class="config-item" v-if="server.url">
                                <span class="config-label">地址:</span>
                                <code>{{ server.url }}</code>
                            </div>
                        </div>

                        <div class="server-tools" v-if="server.tools?.length">
                            <div class="tools-label">可用工具 ({{ server.tools.length }}):</div>
                            <div class="tools-list">
                                <el-tag v-for="tool in server.tools.slice(0, 5)" :key="tool" size="small" style="margin-right: 5px; margin-bottom: 5px;">
                                    {{ tool }}
                                </el-tag>
                                <span v-if="server.tools.length > 5" class="more-tools">+{{ server.tools.length - 5 }} 更多</span>
                            </div>
                        </div>
                    </div>

                    <div class="server-footer">
                        <el-button v-if="server.status === 'running'" size="small" type="warning" @click="stopServer(server)">停止</el-button>
                        <el-button v-else size="small" type="success" @click="startServer(server)">启动</el-button>
                        <el-button size="small" @click="viewServerDetail(server)">详情</el-button>
                        <el-button size="small" type="danger" @click="deleteServer(server)">删除</el-button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 创建对话框 -->
        <el-dialog v-model="createDialogVisible" title="创建 MCP Server" width="600px" @close="handleCreateDialogClose">
            <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="120px">
                <el-form-item label="名称" prop="name">
                    <el-input v-model="createForm.name" placeholder="MCP Server 名称" />
                </el-form-item>
                <el-form-item label="类型" prop="type">
                    <el-select v-model="createForm.type" placeholder="选择类型" style="width: 100%;">
                        <el-option label="stdio" value="stdio" />
                        <el-option label="sse" value="sse" />
                        <el-option label="http" value="http" />
                    </el-select>
                </el-form-item>
                <el-form-item label="命令" prop="command">
                    <el-input v-model="createForm.command" placeholder="如: npx, node, python" />
                </el-form-item>
                <el-form-item label="参数" prop="args">
                    <el-input v-model="createForm.args" type="textarea" :rows="2" placeholder="命令行参数，每行一个参数" />
                </el-form-item>
                <el-form-item label="环境变量" prop="env">
                    <el-input v-model="createForm.env" type="textarea" :rows="2" placeholder="环境变量，格式: KEY=VALUE" />
                </el-form-item>
                <el-form-item label="URL" prop="url">
                    <el-input v-model="createForm.url" placeholder="HTTP/SSE 类型的服务地址" />
                </el-form-item>
                <el-form-item label="描述" prop="description">
                    <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="MCP Server 描述" />
                </el-form-item>
                <el-form-item label="全局" prop="isGlobal">
                    <el-switch v-model="createForm.isGlobal" />
                    <span style="margin-left: 10px; color: #999; font-size: 12px;">全局服务器可供所有用户使用</span>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="createDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitCreateForm">创建</el-button>
            </template>
        </el-dialog>

        <!-- 详情对话框 -->
        <el-dialog v-model="detailDialogVisible" title="MCP Server 详情" width="700px">
            <div v-if="currentServer" class="server-detail">
                <div class="detail-header">
                    <div class="detail-icon">
                        <span class="icon-text large">{{ currentServer.name?.charAt(0).toUpperCase() || 'M' }}</span>
                    </div>
                    <div class="detail-title">
                        <h3>{{ currentServer.name }}</h3>
                        <el-tag :type="currentServer.status === 'running' ? 'success' : 'info'">
                            {{ currentServer.status === 'running' ? '运行中' : '已停止' }}
                        </el-tag>
                    </div>
                </div>

                <div class="detail-body">
                    <div class="detail-section">
                        <h4>基本信息</h4>
                        <table class="detail-table">
                            <tr>
                                <td>类型</td>
                                <td>{{ currentServer.type || 'stdio' }}</td>
                            </tr>
                            <tr>
                                <td>命令</td>
                                <td><code>{{ currentServer.command || 'npx' }}</code></td>
                            </tr>
                            <tr v-if="currentServer.args">
                                <td>参数</td>
                                <td><code>{{ currentServer.args }}</code></td>
                            </tr>
                            <tr v-if="currentServer.env">
                                <td>环境变量</td>
                                <td><code>{{ currentServer.env }}</code></td>
                            </tr>
                            <tr v-if="currentServer.url">
                                <td>URL</td>
                                <td><code>{{ currentServer.url }}</code></td>
                            </tr>
                            <tr v-if="currentServer.isGlobal">
                                <td>全局</td>
                                <td><el-tag type="success" size="small">是</el-tag></td>
                            </tr>
                        </table>
                    </div>

                    <div class="detail-section">
                        <h4>描述</h4>
                        <p>{{ currentServer.description || '暂无描述' }}</p>
                    </div>

                    <div class="detail-section">
                        <h4>可用工具 ({{ currentServer.tools?.length || 0 }})</h4>
                        <div v-if="currentServer.tools?.length" class="tools-grid">
                            <div v-for="tool in currentServer.tools" :key="tool" class="tool-item">
                                {{ tool }}
                            </div>
                        </div>
                        <p v-else>暂无工具</p>
                    </div>
                </div>
            </div>
            <template #footer>
                <el-button @click="detailDialogVisible = false">关闭</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getMcpServers, createMcpServer, startMcpServer as startMcpServerApi, stopMcpServer as stopMcpServerApi, deleteMcpServer as deleteMcpServerApi } from '@/api/mcp.js';
import Breadcrumb from '@/components/Breadcrumb.vue';

const loading = ref(false);
const servers = ref([]);

// 创建对话框
const createDialogVisible = ref(false);
const createFormRef = ref(null);
const createForm = ref({
    name: '',
    type: 'stdio',
    command: 'npx',
    args: '',
    env: '',
    url: '',
    description: '',
    isGlobal: false
});
const createRules = {
    name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
    command: [{ required: true, message: '请输入命令', trigger: 'blur' }]
};

// 详情对话框
const detailDialogVisible = ref(false);
const currentServer = ref(null);

// 获取服务器列表
const fetchServers = async () => {
    loading.value = true;
    try {
        const res = await getMcpServers();
        servers.value = res || [];
    } catch (error) {
        console.error('获取 MCP Server 列表失败:', error);
        ElMessage.error('获取列表失败');
    } finally {
        loading.value = false;
    }
};

// 启动服务器
const startServer = async (server) => {
    try {
        await startMcpServerApi(server.id);
        ElMessage.success('启动成功');
        fetchServers();
    } catch (error) {
        ElMessage.error('启动失败: ' + (error.message || '未知错误'));
    }
};

// 停止服务器
const stopServer = async (server) => {
    try {
        await stopMcpServerApi(server.id);
        ElMessage.success('停止成功');
        fetchServers();
    } catch (error) {
        ElMessage.error('停止失败: ' + (error.message || '未知错误'));
    }
};

// 删除服务器
const deleteServer = async (server) => {
    try {
        await ElMessageBox.confirm(`确定要删除 MCP Server "${server.name}" 吗？`, '删除确认', {
            confirmButtonText: '删除',
            cancelButtonText: '取消',
            type: 'warning'
        });
        await deleteMcpServerApi(server.id);
        ElMessage.success('删除成功');
        fetchServers();
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('删除失败: ' + (error.message || '未知错误'));
        }
    }
};

// 查看详情
const viewServerDetail = (server) => {
    currentServer.value = server;
    detailDialogVisible.value = true;
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
        type: 'stdio',
        command: 'npx',
        args: '',
        env: '',
        url: '',
        description: '',
        isGlobal: false
    };
};

// 提交创建表单
const submitCreateForm = async () => {
    try {
        await createFormRef.value.validate();
        await createMcpServer(createForm.value);
        ElMessage.success('创建成功');
        createDialogVisible.value = false;
        fetchServers();
    } catch (error) {
        if (error.message) {
            ElMessage.error('创建失败: ' + error.message);
        }
    }
};

onMounted(() => {
    fetchServers();
});
</script>

<style lang="scss" scoped>
.mcp-page {
    margin: 0 20px 20px;

    .mcp-content {
        background: #fff;
        border-radius: 8px;
        padding: 20px;
        min-height: 500px;
    }

    .loading, .empty {
        text-align: center;
        padding: 60px 0;
        color: #999;

        p {
            margin-bottom: 20px;
        }
    }

    .server-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
        gap: 20px;
    }

    .server-card {
        border: 1px solid #eee;
        border-radius: 8px;
        overflow: hidden;
        transition: all 0.3s;

        &:hover {
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        &.running {
            border-color: #67c23a;
        }

        .server-header {
            display: flex;
            align-items: center;
            padding: 15px;
            background: #f9f9f9;
            border-bottom: 1px solid #eee;

            .server-icon {
                width: 45px;
                height: 45px;
                border-radius: 8px;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                display: flex;
                align-items: center;
                justify-content: center;
                margin-right: 12px;

                .icon-text {
                    color: #fff;
                    font-size: 20px;
                    font-weight: bold;

                    &.large {
                        font-size: 28px;
                    }
                }
            }

            .server-info {
                flex: 1;

                .server-name {
                    font-size: 16px;
                    font-weight: bold;
                }

                .server-type {
                    font-size: 12px;
                    color: #999;
                }
            }
        }

        .server-body {
            padding: 15px;

            .server-desc {
                color: #666;
                font-size: 14px;
                margin-bottom: 15px;
            }

            .server-config {
                margin-bottom: 15px;

                .config-item {
                    margin-bottom: 8px;

                    .config-label {
                        color: #999;
                        font-size: 12px;
                        margin-right: 8px;
                    }

                    code {
                        background: #f5f5f5;
                        padding: 2px 6px;
                        border-radius: 4px;
                        font-size: 12px;
                        color: #666;
                    }
                }
            }

            .server-tools {
                .tools-label {
                    font-size: 12px;
                    color: #999;
                    margin-bottom: 8px;
                }

                .tools-list {
                    display: flex;
                    flex-wrap: wrap;
                }

                .more-tools {
                    font-size: 12px;
                    color: #999;
                    display: flex;
                    align-items: center;
                }
            }
        }

        .server-footer {
            padding: 15px;
            border-top: 1px solid #eee;
            display: flex;
            gap: 10px;
        }
    }
}

.server-detail {
    .detail-header {
        display: flex;
        align-items: center;
        margin-bottom: 20px;

        .detail-icon {
            width: 70px;
            height: 70px;
            border-radius: 12px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 20px;

            .icon-text {
                color: #fff;
                font-size: 28px;
                font-weight: bold;
            }
        }

        .detail-title {
            h3 {
                margin: 0 0 10px;
                font-size: 20px;
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
                font-weight: bold;
            }

            p {
                margin: 0;
                color: #666;
                line-height: 1.6;
            }
        }

        .detail-table {
            width: 100%;
            border-collapse: collapse;

            td {
                padding: 8px 0;
                border-bottom: 1px solid #f0f0f0;

                &:first-child {
                    width: 100px;
                    color: #999;
                    font-size: 13px;
                }

                &:last-child {
                    color: #666;
                    font-size: 13px;

                    code {
                        background: #f5f5f5;
                        padding: 2px 6px;
                        border-radius: 4px;
                    }
                }
            }
        }

        .tools-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
            gap: 10px;

            .tool-item {
                background: #f5f5f5;
                padding: 8px 12px;
                border-radius: 4px;
                font-size: 12px;
                color: #666;
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
