import axios from '@/api/axios';

// 获取工具列表
export function getTools(params) {
    return axios({
        url: '/tools',
        method: 'get',
        params
    });
}

// 获取工具详情
export function getTool(id) {
    return axios({
        url: `/tools/${id}`,
        method: 'get'
    });
}

// 创建工具
export function createTool(data) {
    return axios({
        url: '/tools',
        method: 'post',
        data
    });
}

// 更新工具
export function updateTool(id, data) {
    return axios({
        url: `/tools/${id}`,
        method: 'put',
        data
    });
}

// 删除工具
export function deleteTool(id) {
    return axios({
        url: `/tools/${id}`,
        method: 'delete'
    });
}

// 获取我的工具列表
export function getMyTools() {
    return axios({
        url: '/tools/my',
        method: 'get'
    });
}

// 安装工具
export function installTool(data) {
    return axios({
        url: '/tools/install',
        method: 'post',
        data
    });
}

// 卸载工具
export function uninstallTool(toolId) {
    return axios({
        url: `/tools/${toolId}/install`,
        method: 'delete'
    });
}

// 获取已安装的工具
export function getInstalledTools() {
    return axios({
        url: '/tools/installed',
        method: 'get'
    });
}

// 创建工具版本
export function createToolVersion(toolId, data) {
    return axios({
        url: `/tools/${toolId}/versions`,
        method: 'post',
        data
    });
}

// 获取工具版本列表
export function getToolVersions(toolId) {
    return axios({
        url: `/tools/${toolId}/versions`,
        method: 'get'
    });
}

// 获取工具版本详情
export function getToolVersion(toolId, versionId) {
    return axios({
        url: `/tools/${toolId}/versions/${versionId}`,
        method: 'get'
    });
}
