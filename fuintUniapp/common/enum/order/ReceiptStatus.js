import Enum from '../enum'

/**
 * 枚举类：订单收货状态
 * ReceiptStatusEnum
 */
export default new Enum([
  { key: 'NOT_RECEIVED', name: '未收货', value: 10 },
  { key: 'RECEIVED', name: '已收货', value: 20 }
])
