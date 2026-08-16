/**
 * 本地草稿工具：编辑内容自动暂存到浏览器 localStorage，
 * 页面退出/刷新后再次进入时可恢复继续编辑。
 * 说明：草稿按浏览器保存（换设备/清缓存不共享），保存成功后会清除对应草稿。
 */

const PREFIX = 'blog_draft_'

/** 保存草稿（自动带时间戳） */
export function saveDraft(key, value) {
  try {
    localStorage.setItem(PREFIX + key, JSON.stringify({ at: Date.now(), value }))
    return true
  } catch (e) {
    // 存储空间不足 / 隐私模式不可用时静默失败
    return false
  }
}

/** 读取草稿：{ at: 时间戳, value: 内容 }，无草稿或损坏返回 null */
export function loadDraft(key) {
  try {
    const raw = localStorage.getItem(PREFIX + key)
    if (!raw) {
      return null
    }
    const parsed = JSON.parse(raw)
    if (!parsed || parsed.value === null || parsed.value === undefined) {
      return null
    }
    return parsed
  } catch (e) {
    clearDraft(key)
    return null
  }
}

/** 清除草稿 */
export function clearDraft(key) {
  try {
    localStorage.removeItem(PREFIX + key)
  } catch (e) {
    /* ignore */
  }
}
