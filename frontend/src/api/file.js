import request from './request'

// 单文件上传
export const uploadFile = (file, module = 'common') => {
  const form = new FormData()
  form.append('file', file)
  form.append('module', module)
  return request.post('/file/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } })
}
// 多文件上传
export const uploadFiles = (files, module = 'common') => {
  const form = new FormData()
  files.forEach(f => form.append('files', f))
  form.append('module', module)
  return request.post('/file/upload/multi', form, { headers: { 'Content-Type': 'multipart/form-data' } })
}
