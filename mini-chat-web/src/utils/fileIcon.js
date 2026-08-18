/**
 * File icon mapping utility.
 * Maps file extensions to icon identifiers for display in chat bubbles.
 */

// Supported file extension → icon mapping
const FILE_ICON_MAP = {
  // Text
  txt: 'txt',

  // Word
  doc: 'word',
  docx: 'word',

  // Excel
  xls: 'excel',
  xlsx: 'excel',

  // PowerPoint
  ppt: 'ppt',
  pptx: 'ppt',

  // Archive
  rar: 'rar',

  // Compressed tar
  'tar.gz': 'tar',
  tgz: 'tar',
}

// Human-readable labels for each icon type
const FILE_TYPE_LABELS = {
  txt: '文本文档',
  word: 'Word 文档',
  excel: 'Excel 表格',
  ppt: 'PPT 演示文稿',
  rar: 'RAR 压缩包',
  tar: '压缩包',
  unknown: '未知文件',
}

// Icon colors per type (used for the SVG icons)
const FILE_TYPE_COLORS = {
  txt: '#409EFF',
  word: '#2B6CB0',
  excel: '#38A169',
  ppt: '#DD6B20',
  rar: '#9C27B0',
  tar: '#795548',
  unknown: '#909399',
}

/**
 * Resolve the icon type for a given file extension.
 * @param {string} extension - The file extension (e.g. 'docx', 'tar.gz')
 * @returns {string} Icon type key (e.g. 'word', 'unknown')
 */
export function getFileIconType(extension) {
  if (!extension) return 'unknown'
  const ext = extension.toLowerCase()
  return FILE_ICON_MAP[ext] || 'unknown'
}

/**
 * Get the human-readable label for a file type.
 * @param {string} extension - The file extension
 * @returns {string} Label (e.g. 'Word 文档')
 */
export function getFileTypeLabel(extension) {
  const iconType = getFileIconType(extension)
  return FILE_TYPE_LABELS[iconType] || FILE_TYPE_LABELS.unknown
}

/**
 * Get the accent color for a file type.
 * @param {string} extension - The file extension
 * @returns {string} CSS color value
 */
export function getFileTypeColor(extension) {
  const iconType = getFileIconType(extension)
  return FILE_TYPE_COLORS[iconType] || FILE_TYPE_COLORS.unknown
}

/**
 * Format file size into a human-readable string.
 * @param {number} bytes - File size in bytes
 * @returns {string} Formatted size (e.g. '2.5 MB')
 */
export function formatFileSize(bytes) {
  if (bytes == null || isNaN(bytes)) return '未知大小'
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const k = 1024
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  const value = bytes / Math.pow(k, i)
  return value.toFixed(i === 0 ? 0 : 1) + ' ' + units[i]
}
