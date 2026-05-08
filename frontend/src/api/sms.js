import request from './request'

// 发送短信验证码（需先通过滑块验证）
export const sendSmsCode = (data) => request.post('/sms/code', data)
