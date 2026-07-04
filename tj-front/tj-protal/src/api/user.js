import request from "@/utils/request.js"
const USER_API_PREFIX = "/us"
const AUTH_API_PREFIX = "/as"
const PHONE_LOGIN_TYPE = 2;
const PW_LOGIN_TYPE = 1;

// 微信登录（由微信开放平台 redirect，跳转至 wxLogin 回调地址，后端无需此 API）
// export const wxLogins = (data) => ...
// export const saveWxUuid = (uuid) => ...
// export const checkWxLoginStatus = (uuid) => ...

// 微信登录相关 API（后端若无对应接口，可先注释掉调用处）
export const saveWxUuid = (uuid) =>
	request({
		url: `${USER_API_PREFIX}/students/wx/uuid`,
		method: 'post',
		data: { uuid }
	});

export const checkWxLoginStatus = (uuid) =>
	request({
		url: `${USER_API_PREFIX}/students/wx/status`,
		method: 'get',
		params: { uuid }
	});

// 手机号验证码登录
export const phoneLogins = (data) => {
	data.type = PHONE_LOGIN_TYPE;
	return request({
		url: `${AUTH_API_PREFIX}/accounts/login`,
		method: "post",
		data,
		withCredentials: true
	});
}

// 账号密码登录
export const userLogins = (data) => {
	data.type = PW_LOGIN_TYPE;
	return request({
		url: `${AUTH_API_PREFIX}/accounts/login`,
		method: "post",
		data,
		withCredentials: true
	});
}

// 发送验证码 - 后端 CodeService.sendVerifyCode(phone) 需通过 /us/students/sendVerifyCode 暴露
// 当前使用 /us/students/sendSms（见后端 StudentController）
export const verifycode = (params) =>
request({
	url: `${USER_API_PREFIX}/students/sendSms`,
	method: 'post',
	params
})

// 找回密码（学生修改密码）- POST /us/students/password，body: { cellphone, code, password }
export const resetPassword = (params) =>
	request({
		url: `${USER_API_PREFIX}/students/password`,
		method: 'post',
		data: params
	})

// 账号注册 - POST /us/students/register，body: { cellphone, password, code }
export const userRegist = (params) =>
request({
	url: `${USER_API_PREFIX}/students/register`,
	method: 'post',
	data: params
})

// 获取当前登录用户信息
export const getUserInfo = (params) =>
	request({
		url: `${USER_API_PREFIX}/users/me`,
		method: 'get',
		params
	})

// 更新学员信息 - PUT /us/students
export const updateUserInfo = data =>
	request({
		url: `${USER_API_PREFIX}/students`,
		method: 'put',
		data
	})

// 发送短信验证码（解绑手机号用）
export const sendSms = (params) =>
	request({
		url: `${USER_API_PREFIX}/students/sendSms`,
		method: 'post',
		params
	})

// 更新绑定手机号
export const bindPhone = (params) =>
	request({
		url: `${USER_API_PREFIX}/students/updateBindPhone`,
		method: 'post',
		params
	})

// 用户修改密码（个人中心修改密码）- PUT /us/students/password
export const updatePassword =  data =>
	request({
		url: `${USER_API_PREFIX}/students/password`,
		method: 'put',
		data
	})

// 账号退出登录
export const userLogout = () => {
	return request({
		url: `${AUTH_API_PREFIX}/accounts/logout`,
		method: "post",
		withCredentials: true
	});
}
