import Enum from '../../enum'

/**
 * 枚举类：售后单状态
 * RefundStatusEnum
 */
export default new Enum([
  { key: 'A', name: '待审核' },
  { key: 'B', name: '已同意' },
  { key: 'C', name: '已拒绝' },
  { key: 'D', name: '已取消' }
])
