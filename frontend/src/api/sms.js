import request from './request'

// 发送短信验证码
export const sendSmsCode = (phone) => request.post('/sms/code', { phone })
