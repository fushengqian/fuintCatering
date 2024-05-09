import Enum from '../../enum'

/**
 * 枚举类：商家审核状态
 * AuditStatusEnum
 */
export default new Enum([
  { key: 'WAIT', name: '待审核', value: 0 },
  { key: 'REVIEWED', name: '已同意', value: 10 },
  { key: 'REJECTED', name: '已拒绝', value: 20 }
])
