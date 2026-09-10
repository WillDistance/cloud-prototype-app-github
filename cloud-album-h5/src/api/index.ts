import api from './http'

export interface CommonResult<T> {
  errorCode: string
  errorMag: string
  data: T
}

export interface AuthLogin {
  email: string
  password: string
}

export interface LoginResult {
  accessToken: string
  refreshToken: string
  tokenType: string
  accessTokenExpiresIn: number
  refreshTokenExpiresIn: number
}

export interface UserProfile {
  email: string
  timeZone: string
  preferredLanguage: string
}

export interface Device {
  bindingId?: number
  deviceId: string
  model: string
  status: string
  bindTime?: string
  giftCapacityBytes?: number
  giftExpireTime?: string
}

export interface StorageOverview {
  totalCapacityBytes: number
  usedBytes: number
  reservedBytes: number
  remainingBytes: number
  nearestExpireTime?: string
}

export interface Entitlement {
  entitlementNo: string
  sourceType: string
  nameSnapshot: string
  capacityBytes: number
  effectiveTime: string
  expireTime: string
}

export interface PhotoItem {
  photoNo: string
  fileName: string
  widthPixels?: number
  heightPixels?: number
  takenTime?: string
  uploadedTime: string
  thumbnailUrl: string
}

export interface PhotoPage {
  items: PhotoItem[]
  hasMore: boolean
  nextCursorTime?: string
  nextCursorId?: number
}

export interface StoragePlan {
  planCode: string
  planVersion: number
  planName: string
  capacityBytes: number
  durationValue: number
  durationUnit: string
  priceCent: number
  currency: string
  recommended: boolean
}

export interface PaymentOrder {
  orderNo: string
  status: string
  paymentChannel: string
  paymentMethod?: string
  amountCent: number
  currency: string
  providerOrderId?: string
  checkoutUrl?: string
  expireTime: string
  paidTime?: string
}

export const authApi = {
  login: (data: AuthLogin) => api.post<CommonResult<LoginResult>>('/api/auth/login', data),
  refresh: (refreshToken: string) => api.post<CommonResult<LoginResult>>('/api/auth/refresh', { refreshToken }),
  sendRegisterCode: (email: string) => api.post('/api/auth/sendRegisterCode', { email }),
  verifyRegisterCode: (email: string, code: string) => api.post('/api/auth/verifyRegisterCode', { email, code }),
  register: (data: { email: string; password: string; confirmPassword: string }, timeZone: string, language: string) =>
    api.post('/api/auth/register', data, { headers: { 'X-Time-Zone': timeZone, 'Accept-Language': language } }),
  sendResetPasswordCode: (email: string) => api.post('/api/auth/sendResetPasswordCode', { email }),
  verifyResetPasswordCode: (email: string, code: string) => api.post('/api/auth/verifyResetPasswordCode', { email, code }),
  resetPassword: (data: { email: string; newPassword: string; confirmPassword: string }) => api.post('/api/auth/resetPassword', data),
  logout: (refreshToken: string) => api.post('/api/auth/logout', { refreshToken }),
}

export const userApi = {
  profile: () => api.get<CommonResult<UserProfile>>('/api/user/getProfile'),
  updateLanguage: (language: string) => api.post<CommonResult<UserProfile>>('/api/user/updateLanguage', { language }),
}

export const deviceApi = {
  mine: () => api.get<CommonResult<Device | null>>('/api/device/getMyDevice'),
  bind: (deviceId: string, password: string) => api.post<CommonResult<Device>>('/api/device/bindDevice', { deviceId, password }),
}

export const storageApi = {
  plans: () => api.get<CommonResult<StoragePlan[]>>('/api/storagePlan/listActivePlans'),
  overview: () => api.get<CommonResult<StorageOverview>>('/api/storage/getOverview'),
  entitlements: () => api.get<CommonResult<Entitlement[]>>('/api/storage/listEntitlements'),
}

export const paymentApi = {
  createOrder: (data: { planCode: string; planVersion: number; clientRequestId: string }) =>
    api.post<CommonResult<PaymentOrder>>('/api/paymentOrder/createOrder', data),
  captureOrder: (orderNo: string) =>
    api.post<CommonResult<PaymentOrder>>('/api/paymentOrder/captureOrder', { orderNo }),
  getOrder: (orderNo: string) =>
    api.get<CommonResult<PaymentOrder>>('/api/paymentOrder/getOrder', { params: { orderNo } }),
}

export const photoApi = {
  list: (params: Record<string, string | number>) => api.get<CommonResult<PhotoPage>>('/api/photo/listPhotos', { params }),
  detail: (photoNo: string) => api.get(`/api/photo/getPhotoDetail`, { params: { photoNo } }),
  originalUrl: (photoNo: string) => api.get(`/api/photo/getOriginalDownloadUrl`, { params: { photoNo } }),
  delete: (photoNos: string[]) => api.post('/api/photo/deletePhotos', { photoNos }),
}
