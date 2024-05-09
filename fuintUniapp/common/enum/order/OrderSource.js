import Enum from '../enum'

/**
 * 枚举类：订单来源
 * OrderSourceEnum
 */
export default new Enum([
  { key: 'MASTER', name: '普通订单', value: 10 },
  { key: 'BARGAIN', name: '砍价订单', value: 20 },
  { key: 'SHARP', name: '秒杀订单', value: 30 }
])
