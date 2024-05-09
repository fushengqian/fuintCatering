<template>
  <view class="container">
    <view v-if="isLoading" class="loading">
      <u-loading mode="circle"></u-loading>
    </view>
    <view v-else class="field-body" @click="handleSelect()">
      <view class="field-value onelist-hidden">{{ valueText ? valueText: placeholder }}</view>
    </view>
    <u-select v-model="show" mode="mutil-column-auto" :list="options" :default-value="defaultValue" @confirm="onConfirm"></u-select>
  </view>
</template>

<script>
  import Emitter from '@/uview-ui/libs/util/emitter'
  import { isEmpty } from '@/utils/util'
  import RegionModel from '@/common/model/Region'

  // 根据选中的value集获取索引集keys
  // 用于设置默认选中
  const findOptionsKey = (data, searchValue, deep = 1, keys = []) => {
    const index = data.findIndex(item => item.value === searchValue[deep - 1])
    if (index > -1) {
      keys.push(index)
      if (data[index].children) {
        findOptionsKey(data[index].children, searchValue, ++deep, keys)
      }
    }
    return keys
  }

  export default {
    name: 'SelectRegion',
    mixins: [Emitter],
    model: {
      prop: 'value',
      event: 'change'
    },
    props: {
      // v-model 指定选中项
      value: {
        type: Array,
        default: () => {
          return []
        }
      },
      // 未选中时的提示文字
      placeholder: {
        type: String,
        default: '请选择省/市/区'
      }
    },
    data() {
      return {
        // 正在加载
        isLoading: true,
        // 是否显示
        show: false,
        // 默认选中的值
        defaultValue: [],
        // 选中项内容(文本展示)
        valueText: '',
        // 级联选择器数据
        options: []
      }
    },
    watch: {
      // 监听v-model
      value(val) {
        // 设置默认选中的值
        this.valueText = val.map(item => item.label).join('/')
        this.setDefaultValue(val)
        // 将当前的值发送到 u-form-item 进行校验
        this.dispatch('u-form-item', 'on-form-change', val)
      },
    },
    created() {
      // 获取地区数据
      this.getTreeData()
    },

    methods: {

      // 打开选择器
      handleSelect() {
        this.show = true
      },

      // 获取地区数据
      getTreeData() {
        const app = this
        app.isLoading = true
        RegionModel.getTreeData()
          .then(regions => {
            // 格式化级联选择器数据
            this.options = this.getOptions(regions)
          })
          .finally(() => app.isLoading = false)
      },

      // 确认选择后的回调
      onConfirm(value) {
        // 绑定到v-model执行的值
        this.$emit('input', value)
        this.$emit('change', value)
      },

      /**
       * 设置默认选中的值
       * 该操作是为了每次打开选择器时聚焦到上次选择
       * @param {Object} value
       */
      setDefaultValue(value) {
        const values = value.map(item => item.value)
        const options = this.options
        this.defaultValue = findOptionsKey(options, values)
      },

      /**
       * 格式化级联选择器数据
       * @param {*} regions 地区数据
       */
      getOptions(regions) {
        const { getOptions, getChildren } = this
        const options = []
        for (const index in regions) {
          const item = regions[index]
          const children = getChildren(item)
          const optionItem = {
            value: item.id,
            label: item.name
          }
          if (children !== false) {
            optionItem.children = getOptions(children)
          }
          options.push(optionItem)
        }
        
        return options
      },

      // 获取子集地区
      getChildren(item) {
        if (item.city) {
          return item.city
        }
        if (item.region) {
          return item.region
        }
        return false
      }

    }
  }
</script>

<style lang="scss" scoped>
  .container {
    width: 100%;
  }

  .loading {
    padding-left: 10rpx;
    // text-align: center;
  }
</style>
