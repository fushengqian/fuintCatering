<template>
    <view class="privacy">
        <u-popup id="privacy" mode="bottom" :border-radius="26" v-model="showPrivacy" :closeable="true">
            <view class="ws-privacy-popup">
                <view class="ws-privacy-popup__header">
                    <!--标题-->
                    <view class="ws-picker__title">{{ title }}</view>
                </view>
                 <scroll-view class="content-scroll" :scroll-y="true">
                    <view class="ws-privacy-popup__container">
                        <text>{{ desc }}</text>
                        <text>{{ subDesc }}</text>
                    </view>
                </scroll-view>
                <view class="ws-privacy-popup__footer">
                    <button class="is-agree" id="agree-btn" open-type="agreePrivacyAuthorization"
                        @agreeprivacyauthorization="handleAgree">
                        {{agreeText}}
                    </button>
                    <button class="is-disagree" id="disagree-btn" @click="handleDisagree">
                        {{disagreeText}}
                    </button>
                </view>
            </view>
        </u-popup>
    </view>
</template>
<script>
	export default {
		name: 'wxPrivacy',
		emits: ['disagree', 'agree'],
		props: {
			// 标题
			title: {
				type: String,
				default: '用户隐私保护提示'
			},
			// 自定义隐私保护指引名称
			protocol: {
				type: String,
				default: '《用户隐私保护指引》'
			},
			// 是否自动获取隐私保护指引名称（开启后调用getPrivacySetting获取名称）
			enableAutoProtocol: {
				type: Boolean,
				default: false, // 默认为使用自定义隐私指引名称
			},
			// 子描述
			subDesc: {
				type: String,
				default: '当您点击同意并开始使用产品服务时，即表示你已理解并同意该条款内容，该条款将对您产生法律约束力。如您拒绝，将无法使用相应服务。'
			},
			/**
			 * 控制是否可以点击不同意按钮并显示提示。
			 * 如果设置为 true，用户可以点击不同意按钮执行后续逻辑。
			 * 如果设置为 false，点击不同意按钮会显示提示信息，但不会执行后续逻辑。
			 * 默认为 true
			 */
			disagreeEnabled: {
				type: Boolean,
				default: true, // 默认为可以点击
			},
			/**
			 * 配置不同意按钮的提示消息内容。
			 */
			disagreePromptText: {
				type: String,
				default: '请先仔细阅读并同意隐私协议', // 默认提示消息
			},
			// 拒绝按钮文字
			disagreeText: {
				type: String,
				default: '不同意'
			},
			// 同意按钮文字
			agreeText: {
				type: String,
				default: '同意并继续'
			}
		},
		data() {
			return {
              showPrivacy: false,
			  privacyContractName: ''
			}
		},
		methods: {
            // 打开弹窗
            openPrivacy() {
              this.showPrivacy = true;
            },
            
            // 关闭弹窗
            closePrivacy() {
              this.showPrivacy = false;
            },
            
			// 拒绝隐私协议
			handleDisagree() {
              this.$emit('disagree')
			},

			// 同意隐私协议
			handleAgree() {
			  this.$emit('agree')
			}
		}
	}
</script>
<style lang="scss" scoped>
	.ws-privacy-popup {
		padding: 24rpx;
		box-sizing: border-box;
		overflow: hidden;
		width: 100%;
		background: #fff;
		border-radius: 10rpx;
        margin-top: 50rpx;
        
        .content-scroll {
          min-height: 400rpx;
          max-height: 750rpx;
        }

		&__header {
			display: flex;
			align-items: center;
			justify-content: center;
			width: 100%;
			height: 52rpx;
			font-size: 36rpx;
			font-family: PingFangSC-Medium, PingFang SC;
			font-weight: 550;
			color: #1a1a1a;
			line-height: 52rpx;
			margin-bottom: 48rpx;
		}

		&__container {
			width: 100%;
			box-sizing: border-box;
			font-size: 28rpx;
			font-family: PingFangSC-Regular, PingFang SC;
			font-weight: 400;
			color: #333333;
			line-height: 48rpx;
			margin-bottom: 48rpx;

			&-protocol {
				font-weight: 550;
				color: $fuint-theme;
			}
		}

		&__footer {
			display: flex;
			flex-direction: column;
            margin-top: 30rpx;
			.is-disagree,
			.is-agree {
				width: 100%;
				height: 88rpx;
                text-align: center;
				background: #ffffff;
				border-radius: 44rpx;
				font-size: 32rpx;
                display: flex;
                justify-content: center;
                align-items: center;
				font-family: PingFangSC-Regular, PingFang SC;
				font-weight: 400;
				color: #666666;
			}

			.is-agree {
				background: $fuint-theme;
				color: #ffffff;
				margin-bottom: 18rpx;
			}

			button {
				border: none;
				outline: none;

				&::after {
					border: none;
				}
			}
		}
	}
</style>