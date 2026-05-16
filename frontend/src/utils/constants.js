// 统一管理后端所有状态枚举，供页面组件使用

export const PET_STATUS = {
  PENDING:    { label: '待审核', type: 'warning' },
  FIRST_PASS: { label: '待终审', type: 'primary' },
  APPROVED:   { label: '已通过', type: 'success' },
  REJECTED:   { label: '已打回', type: 'danger' },
  ADOPTED:    { label: '已领养', type: 'info' },
  OFFLINE:    { label: '已下架', type: 'info' }
}

export const ORDER_STATUS = {
  PENDING_PAY: { label: '待支付', type: 'warning' },
  PAID:        { label: '已支付', type: 'success' },
  SHIPPED:     { label: '已发货', type: 'primary' },
  RECEIVED:    { label: '已收货', type: 'success' },
  CANCELLED:   { label: '已取消', type: 'info' }
}

export const APPLY_STATUS = {
  PENDING:  { label: '待审核', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已驳回', type: 'danger' }
}

export const ROLE_MAP = {
  USER:         '普通用户',
  USER_ADOPTER: '送养人',
  VOLUNTEER:    '志愿者',
  ADMIN:        '管理员'
}

export const GENDER_MAP = {
  male:   '公',
  female: '母'
}
