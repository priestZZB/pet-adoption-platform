export function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = n => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export function formatPrice(price) {
  return '¥' + Number(price).toFixed(2)
}

export function getStatusLabel(status, map) {
  return map[status]?.label || status
}

export function getStatusType(status, map) {
  return map[status]?.type || ''
}
