import http from './http'

// ---- 认证 ----
export const login = (data) => http.post('/auth/login', data)
export const register = (data) => http.post('/auth/register', data)
export const getMe = () => http.get('/auth/me')

// ---- 用户 / 大厅 ----
export const getUserInfo = (username) => http.get(`/users/${username}`)
export const getUserPosts = (username, params) => http.get(`/users/${username}/posts`, { params })
export const getHall = (params) => http.get('/hall', { params })

// ---- 前台 ----
export const getSite = () => http.get('/site')
export const getPosts = (params) => http.get('/posts', { params })
export const getPost = (id) => http.get(`/posts/${id}`)
export const getCategories = () => http.get('/categories')
export const getTags = () => http.get('/tags')
export const getArchive = () => http.get('/archive')

// ---- 友链 ----
export const getLinks = () => http.get('/links')
export const applyLink = (data) => http.post('/links/apply', data)
export const adminListLinks = (params) => http.get('/admin/links', { params })
export const adminCreateLink = (data) => http.post('/admin/links', data)
export const adminUpdateLink = (id, data) => http.put(`/admin/links/${id}`, data)
export const adminApproveLink = (id) => http.put(`/admin/links/${id}/approve`)
export const adminDeleteLink = (id) => http.delete(`/admin/links/${id}`)

// ---- 好友 ----
export const getFriends = () => http.get('/friends')
export const getFriendRequests = () => http.get('/friends/requests')
export const getSentRequests = () => http.get('/friends/requests/sent')
export const sendFriendRequest = (data) => http.post('/friends/requests', data)
export const approveFriendRequest = (id) => http.put(`/friends/requests/${id}/approve`)
export const rejectFriendRequest = (id) => http.put(`/friends/requests/${id}/reject`)
export const removeFriend = (friendId) => http.delete(`/friends/${friendId}`)
export const getFriendRelation = (targetUserId) => http.get(`/friends/relation/${targetUserId}`)

// ---- 归档收藏 ----
export const getArchives = (type) => http.get('/archives', { params: { type } })
export const getArchiveStatus = (targetType, targetId) =>
  http.get('/archives/status', { params: { targetType, targetId } })
export const addArchive = (data) => http.post('/archives', data)
export const removeArchive = (targetType, targetId) =>
  http.delete('/archives', { params: { targetType, targetId } })

// ---- 说说 ----
export const getMurmurs = (params) => http.get('/murmurs', { params })
export const adminListMurmurs = (params) => http.get('/admin/murmurs', { params })
export const adminCreateMurmur = (data) => http.post('/admin/murmurs', data)
export const adminUpdateMurmur = (id, data) => http.put(`/admin/murmurs/${id}`, data)
export const adminDeleteMurmur = (id) => http.delete(`/admin/murmurs/${id}`)

// ---- 相册（相册组 + 照片/视频） ----
export const getAlbums = () => http.get('/albums')
export const getAlbumDetail = (id) => http.get(`/albums/${id}`)
export const adminListAlbums = () => http.get('/admin/albums')
export const adminCreateAlbum = (data) => http.post('/admin/albums', data)
export const adminUpdateAlbum = (id, data) => http.put(`/admin/albums/${id}`, data)
export const adminDeleteAlbum = (id) => http.delete(`/admin/albums/${id}`)
export const adminAddAlbumPhoto = (groupId, data) => http.post(`/admin/albums/${groupId}/photos`, data)
export const adminDeleteAlbumPhoto = (photoId) => http.delete(`/admin/albums/photos/${photoId}`)

// ---- 统计 / 日志 ----
export const getStats = () => http.get('/admin/stats')
export const adminListLogs = (params) => http.get('/admin/logs', { params })

// ---- 评论 ----
export const getPostComments = (postId) => http.get(`/posts/${postId}/comments`)
export const submitComment = (postId, data) => http.post(`/posts/${postId}/comments`, data)
export const adminListComments = (params) => http.get('/admin/comments', { params })
export const adminApproveComment = (id) => http.put(`/admin/comments/${id}/approve`)
export const adminUnapproveComment = (id) => http.put(`/admin/comments/${id}/unapprove`)
export const adminRejectComment = (id) => http.put(`/admin/comments/${id}/reject`)
export const adminDeleteComment = (id) => http.delete(`/admin/comments/${id}`)

// ---- 站点设置 ----
export const adminUpdateSite = (data) => http.put('/admin/site', data)

// ---- 上传 / 媒体库 ----
export const uploadFile = (file) => {
  const form = new FormData()
  form.append('file', file)
  return http.post('/admin/upload', form)
}
export const adminListFiles = (params) => http.get('/admin/files', { params })
export const adminDeleteFile = (name) => http.delete('/admin/files', { params: { name } })
export const getFilesUsage = () => http.get('/admin/files/usage')

// ---- 邀请码（管理员） ----
export const adminListInvites = () => http.get('/admin/invites')
export const adminGenerateInvites = (count) => http.post('/admin/invites', null, { params: { count } })

// ---- 个人资料 / 密码 ----
export const updateProfile = (data) => http.put('/admin/profile', data)
export const updatePassword = (data) => http.put('/admin/password', data)

// ---- 后台：文章 ----
export const adminListPosts = (params) => http.get('/admin/posts', { params })
export const adminGetPost = (id) => http.get(`/admin/posts/${id}`)
export const adminCreatePost = (data) => http.post('/admin/posts', data)
export const adminUpdatePost = (id, data) => http.put(`/admin/posts/${id}`, data)
export const adminDeletePost = (id) => http.delete(`/admin/posts/${id}`)

// ---- 后台：分类 ----
export const adminCreateCategory = (data) => http.post('/admin/categories', data)
export const adminUpdateCategory = (id, data) => http.put(`/admin/categories/${id}`, data)
export const adminDeleteCategory = (id) => http.delete(`/admin/categories/${id}`)

// ---- 后台：标签 ----
export const adminCreateTag = (data) => http.post('/admin/tags', data)
export const adminUpdateTag = (id, data) => http.put(`/admin/tags/${id}`, data)
export const adminDeleteTag = (id) => http.delete(`/admin/tags/${id}`)
