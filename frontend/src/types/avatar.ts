export type AvatarStatus = 'ACTIVE' | 'DELETED'

export interface AvatarResponse {
  id: number
  accountId: number
  originalFileName: string
  contentType: string
  fileSize: number
  current: boolean
  status: AvatarStatus
  uploadedBy: number
  uploadedAt: string
  deletedAt?: string
}
