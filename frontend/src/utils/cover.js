/**
 * 根据资源标题自动生成默认封面 SVG
 * 纯色背景 + 书本装饰 + 完整标题
 */

const colors = [
  '#667eea', '#764ba2', '#f5576c', '#4facfe', '#43e97b',
  '#fa709a', '#a18cd1', '#fccb90', '#5c6bc0', '#fa8bff',
  '#ff6f00', '#2bd2ff', '#66a6ff', '#78909c', '#ec407a',
]

function hashString(str) {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  return Math.abs(hash)
}

/**
 * 生成一本书的 SVG path（居中）
 */
function bookSVG(w, h) {
  const size = Math.min(w, h) * 0.5
  const cx = w / 2
  const cy = h / 2 - size * 0.08
  const sw = size * 0.6   // book width
  const sh = size * 0.75  // book height
  const x = cx - sw / 2
  const y = cy - sh / 2
  const spine = sw * 0.18
  const r = 3

  // 左页（书脊侧）
  const leftPage = [
    `M ${x + spine} ${y}`,
    `L ${x + r} ${y}`,
    `Q ${x} ${y} ${x} ${y + r}`,
    `L ${x} ${y + sh - r}`,
    `Q ${x} ${y + sh} ${x + r} ${y + sh}`,
    `L ${x + spine} ${y + sh}`,
    `Z`,
  ].join(' ')

  // 右页
  const rightPage = [
    `M ${x + spine} ${y}`,
    `L ${x + sw - r} ${y}`,
    `Q ${x + sw} ${y} ${x + sw} ${y + r}`,
    `L ${x + sw} ${y + sh - r}`,
    `Q ${x + sw} ${y + sh} ${x + sw - r} ${y + sh}`,
    `L ${x + spine} ${y + sh}`,
    `Z`,
  ].join(' ')

  // 书脊
  const spineRect = [
    `M ${x + spine - 1} ${y}`,
    `L ${x + spine + 1} ${y}`,
    `L ${x + spine + 1} ${y + sh}`,
    `L ${x + spine - 1} ${y + sh}`,
    `Z`,
  ].join(' ')

  // 页码线（右页几条横线）
  const lines = []
  const lineStart = x + spine + sw * 0.12
  const lineEnd = x + sw - sw * 0.12
  const lineY1 = y + sh * 0.38
  const lineY2 = y + sh * 0.52
  const lineY3 = y + sh * 0.66
  lines.push(`  <line x1="${lineStart}" y1="${lineY1}" x2="${lineEnd}" y2="${lineY1}" stroke="rgba(255,255,255,0.25)" stroke-width="${Math.max(1, sw * 0.02)}" stroke-linecap="round"/>`)
  lines.push(`  <line x1="${lineStart}" y1="${lineY2}" x2="${lineEnd - sw * 0.15}" y2="${lineY2}" stroke="rgba(255,255,255,0.25)" stroke-width="${Math.max(1, sw * 0.02)}" stroke-linecap="round"/>`)
  lines.push(`  <line x1="${lineStart}" y1="${lineY3}" x2="${lineEnd}" y2="${lineY3}" stroke="rgba(255,255,255,0.25)" stroke-width="${Math.max(1, sw * 0.02)}" stroke-linecap="round"/>`)

  // 所有元素用半透明白色填充/描边
  return [
    `  <path d="${leftPage}" fill="rgba(255,255,255,0.15)"/>`,
    `  <path d="${rightPage}" fill="rgba(255,255,255,0.12)"/>`,
    `  <path d="${spineRect}" fill="rgba(255,255,255,0.2)"/>`,
    ...lines,
  ].join('\n')
}

/**
 * 根据资源标题生成默认封面 data URL
 * 纯色背景 + 书本装饰 + 完整标题文字
 * @param {string} title - 资源标题
 * @param {number} [width=400] - SVG 宽度
 * @param {number} [height=300] - SVG 高度
 * @returns {string} data:image/svg+xml URL
 */
export function getTitleCover(title, width = 400, height = 300) {
  if (!title) return '/default-cover.svg'

  const hash = hashString(title)
  const bgColor = colors[hash % colors.length]

  // 最多显示前 8 个字
  const displayText = title.length > 8 ? title.slice(0, 8) + '…' : title
  const fontSize = Math.min(width, height) * 0.18

  const svg = [
    `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">`,
    `  <rect width="${width}" height="${height}" fill="${bgColor}"/>`,
    bookSVG(width, height),
    `  <text x="${width / 2}" y="${height / 2 + Math.min(width, height) * 0.24}" text-anchor="middle" dominant-baseline="central"`,
    `    font-family="'PingFang SC','Microsoft YaHei',sans-serif" font-size="${fontSize.toFixed(0)}" font-weight="bold" fill="rgba(255,255,255,0.92)">`,
    `    ${displayText}`,
    `  </text>`,
    `</svg>`,
  ].join('\n')

  return `data:image/svg+xml,${encodeURIComponent(svg)}`
}
