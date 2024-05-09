import Enum from '../enum'

/**
 * 枚举类：优惠券到期类型
 * ExpireTypeEnum
 */
export default new Enum([
  { key: 'RECEIVE', name: '领取后', value: 10 },
  { key: 'FIXED_TIME', name: '固定时间', value: 20 }
])
