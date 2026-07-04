import axios from '@/api/axios';

// 获取 MCP Server 列表
export function getMcpServers() {
    return axios({
        url: '/mcp/servers',
        method: 'get'
    });
}

// 获取我的 MCP Server 列表
export function getMyMcpServers() {
    return axios({
        url: '/mcp/servers/my',
        method: 'get'
    });
}

// 获取 MCP Server 详情
export function getMcpServer(id) {
    return axios({
        url: `/mcp/servers/${id}`,
        method: 'get'
    });
}

// 创建 MCP Server
export function createMcpServer(data) {
    return axios({
        url: '/mcp/servers',
        method: 'post',
        data
    });
}

// 更新 MCP Server
export function updateMcpServer(id, data) {
    return axios({
        url: `/mcp/servers/${id}`,
        method: 'put',
        data
    });
}

// 删除 MCP Server
export function deleteMcpServer(id) {
    return axios({
        url: `/mcp/servers/${id}`,
        method: 'delete'
    });
}

// 启动 MCP Server
export function startMcpServer(id) {
    return axios({
        url: `/mcp/servers/${id}/start`,
        method: 'post'
    });
}

// 停止 MCP Server
export function stopMcpServer(id) {
    return axios({
        url: `/mcp/servers/${id}/stop`,
        method: 'post'
    });
}
