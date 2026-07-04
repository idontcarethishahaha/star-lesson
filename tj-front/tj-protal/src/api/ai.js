import request from "@/utils/request.js"

/* =============================================
 * tj-chat 服务 - RAG 知识库 + 流式聊天
 * 网关路由: /chat/**, /file/**, /session/**
 * ============================================= */

// ===================== 会话管理 =====================

// 创建用户会话
export const createUserSession = (data) =>
    request({
        url: `/session`,
        method: 'post',
        data
    })

// 查询用户会话列表
export const getUserSessionList = () =>
    request({
        url: `/session/list`,
        method: 'get'
    })

// 修改会话（标题/标签）
export const updateUserSession = (id, data) =>
    request({
        url: `/session/${id}`,
        method: 'put',
        params: data
    })

// 删除会话
export const deleteUserSession = (id) =>
    request({
        url: `/session/${id}`,
        method: 'delete'
    })

// ===================== 聊天记录 =====================

// 分页查询聊天记录（GET /chat/records?sessionId&pageNo&pageSize）
export const getChatRecord = (params) =>
    request({
        url: `/chat/records`,
        method: 'get',
        params
    })

// ===================== 流式聊天（SSE）=====================

// 流式聊天 (SSE) - 返回原始响应供前端使用 fetch-event-source
// 后端: GET /chat/?message&sessionId
export const streamChat = (params) =>
    request({
        url: `/chat/`,
        method: 'get',
        params,
        responseType: 'stream'
    })

// 知识库流式聊天 (SSE) - 基于 RAG 向量检索的流式回答
// 后端: GET /chat/file?message&sessionId
export const streamChatByFile = (params) =>
    request({
        url: `/chat/file`,
        method: 'get',
        params,
        responseType: 'stream'
    })

// 简单聊天（非流式）
// 后端: GET /chat/simple?message&sessionId
export const simpleChat = (params) =>
    request({
        url: `/chat/simple`,
        method: 'get',
        params
    })

// ===================== 知识库文件管理 =====================

// 上传 Markdown 文件到知识库
export const uploadMarkdown = (file, level = 2) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('level', level)
    return request({
        url: `/file/upload`,
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// 分页查询用户文件列表
export const queryMarkdownPage = (params) =>
    request({
        url: `/file/page`,
        method: 'get',
        params
    })

// 根据文件 ID 获取文件内容
export const getMarkdown = (fileId) =>
    request({
        url: `/file/${fileId}`,
        method: 'get'
    })

// 更新文件内容
export const updateMarkdown = (data) =>
    request({
        url: `/file/update`,
        method: 'put',
        data
    })

// 删除文件
export const deleteMarkdown = (fileId) =>
    request({
        url: `/file/${fileId}`,
        method: 'delete'
    })

// ===================== 知识库问答 =====================

// 基于知识库内容对话（返回匹配片段列表含 title、content、score）
// 后端: GET /file/chat?message
export const chatByMarkdownDoc = (params) =>
    request({
        url: `/file/chat`,
        method: 'get',
        params
    })
