import request from './request'

// 预生成用户名（注册页面加载时调用）
export const generateUsername = () => request.get('/user/generate-username')

// 用户注册（含ticket/randstr/captchaSign）
export const register = (data) => request.post('/user/register', data)
// 用户登录（用户名+密码）
export const login = (data) => request.post('/user/login', data)
// 手机号+短信验证码登录
export const phoneLogin = (data) => request.post('/user/login/phone', data)
// 获取当前用户信息
export const getUserInfo = () => request.get('/user/info')
// 修改个人信息
export const updateUserInfo = (data) => request.put('/user/info', data)
// 修改密码（需smsCode）
export const changePassword = (data) => request.put('/user/password', data)
// 找回密码（需smsCode）
export const resetPassword = (data) => request.post('/user/password/reset', data)
// 实名认证（支持人脸base64或URL）
export const realNameAuth = (data) => request.post('/user/real-name', data)
// 申请成为志愿者
export const volunteerApply = () => request.post('/user/volunteer/apply')
// 查看志愿者申请进度
export const getVolunteerStatus = () => request.get('/user/volunteer/apply/status')
// 申请成为送养人
export const donorApply = () => request.post('/user/donor/apply')
// 查看送养人申请进度
export const getDonorStatus = () => request.get('/user/donor/apply/status')
// 上传/修改头像
export const uploadAvatar = (file) => {
  const form = new FormData()
  form.append('file', file)
  return request.post('/user/avatar', form, { headers: { 'Content-Type': 'multipart/form-data' } })
}
