import Enum from '../enum'

/**
 * 枚举类：订单支付状态
 * PayStatusEnum
 */
export default new Enum([
  { key: 'PENDING', name: '待支付', value: 10 },
  { key: 'SUCCESS', name: '已支付', value: 20 }
])
