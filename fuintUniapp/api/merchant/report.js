import request from '@/utils/request'

// api地址
const api = {
  overview: 'merchantApi/report/overview',
  chart: 'merchantApi/report/chart',
  top: 'merchantApi/report/top'
}

// 数据概况
export function getOverview(param, option) {
  const options = {
    isPrompt: true,
    load: true,
    ...option
  }
  return request.get(api.overview, param, options)
}

// 运营走势
export function getChart(param, option) {
  const options = {
    isPrompt: true,
    load: true,
    ...option
  }
  return request.get(api.chart, param, options)
}

// 数据排行
export function getTop(param, option) {
  const options = {
    isPrompt: true,
    load: true,
    ...option
  }
  return request.post(api.top, param, options)
}
