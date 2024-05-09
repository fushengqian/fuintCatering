<template name="neoceansoft-keyboard">

    <view  :style="{'height':popupHeight}" class="numView">

        <view style="display: flex;">


            <view :style="{'width':numWidht}">

                <view style="width: 100%; height: 2rpx;background-color: #efefef;" />
                <view style="display: flex;">
                    <view v-for="(item,index) in num1" :key="index" :style="{'height':numBtnHeight,'width':numBtnWidth+'px'}"
                        class="numLayout">
                        <view v-if="index==1" style="width: 2rpx; height: 100%;background-color: #efefef;"></view>
                        <button plain="true" hover-class="numClickCss"
                            :style="{'height':numBtnHeight,'width':numBtnWidth+'px'}" class="numBtn"
                            @click="btnClick(item)">
                            {{item}}
                        </button>
                        <view v-if="index==1" style="width: 2rpx; height: 100%;background-color: #efefef;"></view>
                    </view>
                </view>


                <view style="width: 100%; height: 2rpx;background-color: #efefef;" />
                <view style="display: flex;">
                    <view v-for="(item,index) in num2" :key="index" :style="{'height':numBtnHeight,'width':numBtnWidth+'px'}"
                        class="numLayout">
                        <view v-if="index==1" style="width: 2rpx; height: 100%;background-color: #efefef;"></view>
                        <button plain="true" hover-class="numClickCss"
                            :style="{'height':numBtnHeight,'width':numBtnWidth+'px'}" class="numBtn"
                            @click="btnClick(item)">
                            {{item}}
                        </button>
                        <view v-if="index==1" style="width: 2rpx; height: 100%;background-color: #efefef;"></view>
                    </view>
                </view>


                <view style="width: 100%; height: 2rpx;background-color: #efefef;" />
                <view style="display: flex;">
                    <view v-for="(item,index) in num3" :key="index" :style="{'height':numBtnHeight,'width':numBtnWidth+'px'}"
                        class="numLayout">
                        <view v-if="index==1" style="width: 2rpx; height: 100%;background-color: #efefef;"></view>
                        <button plain="true" hover-class="numClickCss"
                            :style="{'height':numBtnHeight,'width':numBtnWidth+'px'}" class="numBtn"
                            @click="btnClick(item)">
                            {{item}}
                        </button>
                        <view v-if="index==1" style="width: 2rpx; height: 100%;background-color: #efefef;"></view>
                    </view>
                </view>


                <view style="width: 100%; height: 2rpx;background-color: #efefef;" />
                <view style="display: flex;">
                    <view v-for="(item,index) in num4" :key="index" class="numLayout">
                        <view v-if="index==1" style="width: 2rpx; height: 100%;background-color: #efefef;"></view>
                        <button plain="true" v-if="index==2||(keyboardType == 'password'&&index==1)"
                            :hover-class="keyboardType=='payment'?'':'numClickCss'"
                            :style="{'height':numBtnHeight,'width':numBtnWidth+'px'}" class="numBtn"
                            @click="btnClick(item.type)">
                            <view v-if="keyboardType!='payment'" class="iconfont icon-deletenumber"></view>

                        </button>
                        <button v-else plain="true" hover-class="numClickCss"
                            :style="{'height':numBtnHeight,'width':keyboardType=='password'||(keyboardType=='payment'&&index==0)?((numBtnWidth*2)-1)+'px':(numBtnWidth-1)+'px'}"
                            class="numBtn" @click="btnClick(item.type)">
                            {{item.name}}
                        </button>
                        <view v-if="index==1" style="width: 2rpx; height: 100%;background-color: #efefef;"></view>
                    </view>
                </view>

            </view>
            <view v-if="keyboardType=='payment'"
                :style="{'width':payboardWidth,'height':payboardHeight,'display':'flex'}">
                <view style="width: 2rpx;height: 100%;background-color: #efefef;"></view>
                <view :style="{'display':'flex','width':'100%','height':'100%'}">

                    <view style="width: 100%;height: 100%;">
                        <view style="width: 100%;height: 2rpx;background-color: #efefef;"></view>
                        <button plain="true" hover-class="numClickCss"
                            :style="{'height':numBtnHeight,'width':(numBtnWidth-1)+'px'}" class="numBtn" @click="btnClick(-2)">
                            <view class="iconfont icon-deletenumber"></view>

                        </button>
                        <button plain="true" hover-class="behaviorCommonCss" :style="{'height':behaviorBtnHeight,'width':(numBtnWidth-1),'color':behaviorTextColor,
                            'background':behaviorBgColor}" class="behaviorCss" @click="paymentClick()">
                            {{behaviorName}}

                        </button>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
    export default {
        data() {
            return {
                numBtnWidth: 0,
                numBtnHeight: '',
                behaviorBtnHeight: '',
                popupHeight: '',
                payboardWidth: '',
                payboardHeight: '',
                numWidht: '',
                numOpWidht: '',
                opBtnWidth: '',
                opBtnHeight1: '',
                opBtnHeight2: '',
                nums: [],
                isShowPay: true,
                bl: 0.45,
                num1: [1, 2, 3],
                num2: [4, 5, 6],
                num3: [7, 8, 9],
                num4: [{
                    name: '.',
                    type: '.'
                }, {
                    name: '0',
                    type: 0
                }, {
                    name: '删除',
                    type: -2
                }],


            }
        },
        props: {
            keyboardType: {
                type: String,
                default: "number" // number idcard,payment,password
            },
            behaviorName: {
                type: String,
                default: '付款'
            },
            behaviorTextColor: {
                type: String,
                default: '#ffffff'
            },
            behaviorBgColor: {
                type: String,
                default: '#41ae3c'
            }
        },


        created() {


            var that = this
            uni.getSystemInfo({
                success(res) {
                    console.log("页面信息：" + JSON.stringify(res))
                    var width = 0
                    if (that.keyboardType == 'payment') {
                        width = (res.screenWidth / 4) * 3
                    } else {
                        width = (res.screenWidth / 3) * 3
                    }
                    var height = ''
                    if (that.keyboardType == 'payment') {
                        that.bl = 0.58
                        height = width / 3 * that.bl
                    } else {
                        height = width / 3 * that.bl
                    }

                    that.opBtnWidth = (res.screenWidth / 4) * 1 + 'px'
                    // that.opBtnHeight1 = (height * 4) * (3 / 5) + 'px'
                    // that.opBtnHeight2 = (height * 4) * (2 / 5) + 'px'
                    that.opBtnHeight1 = ((height * 2) + 2.5) + 'px'
                    that.opBtnHeight2 = ((height * 2) + 2) + 'px'
                    that.numWidht = width + 'px'
                    that.payboardWidth = (res.screenWidth - width) + 'px'
                    that.payboardHeight = height * 3
                    that.numBtnWidth = width / 3
                    that.numBtnHeight = height + 'px'
                    that.behaviorBtnHeight = height * 3 + 'px'
                    that.popupHeight = (height * 4) + 'px'

                }
            })
            if (this.keyboardType == 'password') {
                //密码键盘，不存在小数点问题
                this.num4.splice(0, 1)
            } else if (this.keyboardType == 'idcard') {
                this.num4[0].name = 'X'
                this.num4[0].type = 'X'
            } else if (this.keyboardType == 'payment') {
                this.num4.splice(2, 1)
                var bean = this.num4[0]
                this.num4[0] = this.num4[1]
                this.num4[1] = bean

            }

        },
        methods: {
            paymentClick(){
                this.$emit('paymentClick')
            },


            // 数字点击事件回调
            btnClick(item) {
                // this.$emit('click',item)
                this.$emit('keyclick', item)
                this.inputNum(item)
            },
            // 输入处理事件回调
            inputNum(item) {
                switch (item) {
                    case 0:
                    //todo 验证待处理
                        this.putNum('0')
                        break
                    case 1:
                        //todo 验证待处理
                        this.putNum('1')

                        break
                    case 2:
                        //todo 验证待处理
                        this.putNum('2')
                        break
                    case 3:
                        //todo 验证待处理
                        this.putNum('3')
                        break
                    case 4:
                        //todo 验证待处理
                        this.putNum('4')
                        break
                    case 5:
                        //todo 验证待处理
                        this.putNum('5')
                        break
                    case 6:
                        //todo 验证待处理
                        this.putNum('6')
                        break
                    case 7:
                        //todo 验证待处理
                        this.putNum('7')
                        break
                    case 8:
                        //todo 验证待处理
                        this.putNum('8')
                        break
                    case 9:
                        //todo 验证待处理
                        this.putNum('9')
                        break
                    case '.':
                        this.putNum('.')
                        break
                    case 'X':
                        this.putNum('X')
                        break
                    case -2:
                        this.del()
                        break

                }
            },
            putNum(data) {
                this.nums.push(data)
                var result = this.nums.join('')
                this.$emit('result', result)
            },
            del() {
                if (this.nums.length == 0) {
                    return
                }
                this.nums.splice(this.nums.length - 1, 1)
                var result = this.nums.join('')
                this.$emit('result', result)



            },




        }
    }
</script>

<style scoped>
    @font-face {
        font-family: 'iconfont';

        src: url('https://at.alicdn.com/t/font_2180051_21huv31g6dq.woff2?t=1625469481487') format('woff2'),
            url('https://at.alicdn.com/t/font_2180051_21huv31g6dq.woff?t=1625469481487') format('woff'),
            url('https://at.alicdn.com/t/font_2180051_21huv31g6dq.ttf?t=1625469481487') format('truetype');

    }


    .iconfont {
        font-family: "iconfont" !important;
        font-size: 14px;
        font-style: normal;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
    }

    .icon-deletenumber:before {
        content: "\e666";
    }




    .numView {
        position: fixed;
        bottom: 0rpx;
        width: 100%;
    }

    .numBtn {
        display: flex;
        align-items: center;
        justify-content: center;
        justify-items: center;
        border-radius: 20rpx;
        border: 0rpx solid #ffffff;


    }

    .behaviorCss {
        display: flex;
        align-items: center;
        justify-content: center;
        justify-items: center;
        border-radius: 0rpx;
        border: 0rpx solid #E0E0E0;
        color: #ffffff;
        font-weight: 500;
        font-size: 30rpx;

    }

    .behaviorCommonCss {
        background-color: #efefef !important;
        transform: translate(1rpx, 1rpx);

    }

    .numLayout {
        display: flex;
    }

    .numLayout2 {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

    }

    .boderSy {
        width: 2rpx;
        background-color: #efefef;
    }

    .opBtn1 {
        background-color: #f29100;
        display: flex;
        align-items: center;
        justify-content: center;
        justify-items: center;
        align-content: center;
        color: #FFFFFF;
        font-weight: 400;
        font-size: 30rpx;
        text-align: center;


    }

    .numClickCss {
        background-color: #efefef;
        color: #FFFFFF;
    }


    .opBtn1x {
        background-color: #ffd28b;
        display: flex;
        color: #9fa0a0;
        align-items: center;
        justify-content: center;
        justify-items: center;
        align-content: center;
        font-weight: 400;
        font-size: 30rpx;
        text-align: center;

    }


    .opBtn2 {
        background-color: #0090ff;
        display: flex;
        align-items: center;
        justify-content: center;
        justify-items: center;
        align-content: center;
        color: #FFFFFF;
        font-weight: 400;
        font-size: 30rpx;
        text-align: center;

    }

    .opBtn2x {
        background-color: #7fcff4;
        display: flex;
        align-items: center;
        justify-content: center;
        justify-items: center;
        align-content: center;
        font-weight: 400;
        font-size: 30rpx;
        text-align: center;
        color: #9fa0a0;

    }

    .customStyle {
        color: 'red'
    }
</style>
