/**
 * 通用 CSV 导出工具
 *
 * 用法：
 *   import { exportToCSV } from '@/utils/export'
 *   exportToCSV(dataArray, '文件名.csv')
 */

/**
 * 将 JSON 数组导出为 CSV 文件并触发下载
 * @param {Array<Object>} data - 数据数组
 * @param {string} filename - 下载文件名
 */
export function exportToCSV(data, filename = 'export.csv') {
  if (!data || data.length === 0) {
    console.warn('[export] 没有可导出的数据')
    return
  }

  // 提取表头（所有键的并集）
  const headers = Object.keys(data[0])

  // CSV 内容：表头行 + 数据行
  const rows = [headers.map(escapeCsv).join(',')]
  for (const row of data) {
    const values = headers.map(h => {
      const v = row[h]
      if (v === null || v === undefined) return ''
      return escapeCsv(String(v))
    })
    rows.push(values.join(','))
  }
  const csvContent = rows.join('\r\n')

  // 添加 BOM 头以确保 Excel 正确识别 UTF-8 中文
  const blob = new Blob(['﻿' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)

  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * 转义 CSV 字段（处理含逗号、引号、换行的值）
 */
function escapeCsv(value) {
  if (value.includes(',') || value.includes('"') || value.includes('\n') || value.includes('\r')) {
    return '"' + value.replace(/"/g, '""') + '"'
  }
  return value
}
