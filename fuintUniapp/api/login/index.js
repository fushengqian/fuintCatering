import request from '@/utils/request'

// api地址
const api = {
  register: 'clientApi/sign/register',
  login: 'clientApi/sign/signIn',
  mpWxLogin: 'clientApi/sign/mpWxLogin',
  mpWxAuth: 'clientApi/sign/mpWxAuth',
  captcha: 'clientApi/captcha/getCode',
  sendSmsCaptcha: 'clientApi/sms/sendVerifyCode'
}

// 用户注册
export function register(data) {
  return request.post(api.register, data)
}

// 用户登录
export function login(data) {
  return request.post(api.login, data)
}

// 微信小程序快捷登录
export function mpWxLogin(data, option) {
  return request.post(api.mpWxLogin, data, option)
}

// 微信公众号授权
export function mpWxAuth(data, option) {
  return request.post(api.mpWxAuth, data, option)
}

// 图形验证码
export function captcha() {
  return request.get(api.captcha)
}

// 发送短信验证码
export function sendSmsCaptcha(data) {
  return request.post(api.sendSmsCaptcha, data)
}
