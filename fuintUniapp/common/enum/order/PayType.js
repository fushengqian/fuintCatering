import Enum from '../enum'

/**
 * 枚举类：订单支付方式
 * PayTypeEnum
 */
export default new Enum([
  { key: 'BALANCE', name: '余额支付', value: 'BALANCE' },
  { key: 'WECHAT', name: '微信支付', value: 'JSAPI' },
  { key: 'WECHAT_H5', name: '微信H5支付', value: 'WECHAT_H5' },
  { key: 'CASH', name: '余额支付', value: 'CASH' },
  { key: 'MICROPAY', name: '微信扫码支付', value: 'MICROPAY' },
  { key: 'ALISCAN', name: '支付宝支付', value: 'ALISCAN' },
])
