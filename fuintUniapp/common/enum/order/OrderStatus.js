import Enum from '../enum'

/**
 * 枚举类：订单状态
 * OrderStatusEnum
 */
export default new Enum([
  { key: 'CREATED', name: '待支付', value: 'A' },
  { key: 'PAID', name: '已支付', value: 'B' },
  { key: 'CANCEL', name: '已取消', value: 'C' },
  { key: 'DELIVERY', name: '待发货', value: 'D' },
  { key: 'DELIVERED', name: '已发货', value: 'E' },
  { key: 'RECEIVED', name: '已收货', value: 'F' },
  { key: 'DELETED', name: '已删除', value: 'G' },
  { key: 'REFUND', name: '已退款', value: 'H' },
])
