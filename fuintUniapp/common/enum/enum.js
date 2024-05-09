/**
 * 枚举类
 * Enum.IMAGE.name                => "图片"
 * Enum.getNameByKey('IMAGE')     => "图片"
 * Enum.getValueByKey('IMAGE')    => 10
 * Enum.getNameByValue(10)        => "图片"
 * Enum.getData()                 => [{key: "IMAGE", name: "图片", value: 10}]
 */
class Enum {
  constructor (param) {
    const keyArr = []
    const valueArr = []

    if (!Array.isArray(param)) {
      throw new Error('param is not an array!')
    }

    param.map(element => {
      if (!element.key || !element.name) {
        return
      }
      // 保存key值组成的数组，方便A.getName(name)类型的调用
      keyArr.push(element.key)
      valueArr.push(element.value)
      // 根据key生成不同属性值，以便A.B.name类型的调用
      this[element.key] = element
      if (element.key !== element.value) {
        this[element.value] = element
      }
    })


    // 保存源数组
    this.data = param
    this.keyArr = keyArr
    this.valueArr = valueArr

    // 防止被修改
    // Object.freeze(this)
  }

  // 根据key得到对象
  keyOf (key) {
    return this.data[this.keyArr.indexOf(key)]
  }

  // 根据key得到对象
  valueOf (key) {
    return this.data[this.valueArr.indexOf(key)]
  }

  // 根据key获取name值
  getNameByKey (key) {
    const prop = this.keyOf(key)
    if (!prop) {
      throw new Error('No enum constant' + key)
    }
    return prop.name
  }

  // 根据value获取name值
  getNameByValue (value) {
    const prop = this.valueOf(value)
    if (!prop) {
      throw new Error('No enum constant' + value)
    }
    return prop.name
  }

  // 根据key获取value值
  getValueByKey (key) {
    const prop = this.keyOf(key)
    if (!prop) {
      throw new Error('No enum constant' + key)
    }
    return prop.key
  }

  // 返回源数组
  getData () {
    return this.data
  }
}

export default Enum
