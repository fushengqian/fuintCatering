<template>
	<view class="mark" :class="[show_key ? '' : 'hidden']">
		<view class="kong"></view>
		<!-- 信息框 -->
		<view class="msg">
			<!-- 关闭按钮 -->
			<view class="img iconfont icon-guanbi" @tap="closeFuc">
			</view>
			<view class="title"> {{ title ? title : "请输入您的密码" }}</view>
			<view class="subTitle" v-show="show_subTitle && price > 0">
				付款金额：{{price}}
			</view>
			<view class="pswBox">
				<view v-for="item in 6" :key="item" class="content_item">{{password[item] ? '●' : ''}}</view>
			</view>
			<view class="forget" v-if="false" @tap="forgetFuc">忘记密码？</view>
		</view>
		<!-- 数字键盘 -->
		<view class="numeric">
			<!-- 正常模式 -->
			<view class="num" v-if="mix" v-for="(item,index) in num1" :key="index" :class="item == 10 ? 'amend1' : item == 12 ? 'amend3 iconfont icon-backspace' : ''" @tap="press({num:item})">
					{{item == 10 ? '' : item == 11 ? '0' : item == 12 ? '': item}}
			</view>
			<!-- 混淆模式 -->
			<view class="num" v-else v-for="(item,index) in num" :key="index" :class="item == 10 ? 'amend1' : item == 12 ? 'amend3 iconfont icon-backspace' : ''" @tap="press({num:item})">
					{{item == 10 ? '' : item == 11 ? '0' : item == 12 ? '': item}}
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		props:{
			show_key:Boolean,
			price:String,
            title:String,
			show_subTitle:{
				default:true
			},
			mix:{
				type: Boolean,
				default: false
			}
		},
		data () {
			return { 
				num:[],	//	乱序
				num1:[1,2,3,4,5,6,7,8,9,10,11,12],//	顺序
				password:"",
			}
		},
		created() {
			// 打乱数组
			this.num = this.randomArray([1,2,3,4,5,6,7,8,9,11]);
			this.num.splice(9, 0, 10);
			this.num.splice(11, 0, 12);
			console.log(this.num);
			console.log(this.key_words);
		},
		methods:{
			// 数组混淆
			randomArray(arr){
				return arr.sort(() => Math.random() -0.5);
			},
			press (obj) {
				let num = obj.num
				if (obj.num == 10) {
					console.log('我是10我什么都不干')
				} else if (obj.num == 12) {
					this.password = this.password.slice(0,this.password.length-1);
				} else if (obj.num == 11) {
					num = '0'
					this.password += num;
				} else {
					this.password += num;
				}
				if (this.password.length == 6) {
					this.$emit('getPassword',{password:this.password})
					this.password = "";
				}
			},
			// 关闭支付页面
			closeFuc () {
				console.log('关闭支付页面');
				this.$emit("closeFuc",false)
			},
			// 找回密码
			forgetFuc () {
				uni.navigateTo({
					url:'/pages/user/findPwd'
				})
			}
		}
	}
</script>

<style>
	.mark{
		width: 750upx;
		background: rgba(0,0,0,0.7);
		padding: 0 0 700upx 0;
		position: absolute;
		top: 0upx;
		left: 0upx;
		z-index: 99;
	}
	.hidden{
		display: none;
	}
	.kong{
		width: 750upx;
		height: 250upx;
	}
	.msg{
		width: 550upx;
		height: 450upx;
		background: rgba(255,255,255,1);
		border-radius: 20upx;
		margin: 0 auto;
		animation: msgBox .2s linear;
	}
	@keyframes msgBox{
		0%{
			transform:translateY(50%);
			opacity: 0;
		}
		50%{
			transform:translateY(25%);
			opacity: 0.5;
		}
		100%{
			transform:translateY(0%);
			opacity: 1;
		}
	}
	@keyframes numBox{
		0%{
			transform:translateY(50%);
			opacity: 0;
		}
		50%{
			transform:translateY(25%);
			opacity: 0.5;
		}
		100%{
			transform:translateY(0%);
			opacity: 1;
		}
	}
	.msg>.img{
		padding: 20upx 0 0 20upx;
		font-size: 40upx;
	}
	.msg>.title{
		width: 100%;
		height: 100upx;
		line-height: 100upx;
		font-weight: 500;
		font-size: 36upx;
		text-align: center;
	}
	.msg>.subTitle{
		width: 100%;
		height: 50upx;
		line-height: 50upx;
		font-weight: 400;
		font-size: 32upx;
		text-align: center;
	}
	.pswBox{
		width: 80%;
		height: 80upx;
		margin: 50upx auto 0;
		display: flex;
	}
	.content_item{
		flex: 2;
		text-align: center;
		line-height: 80upx;
		border: 1upx solid #D6D6D6;
		border-right: 0upx solid;
	}
	.content_item:nth-child(1){
		border-radius: 10upx 0 0 10upx;
	}
	.content_item:nth-child(6){
		border-right: 1upx solid #D6D6D6;
		border-radius: 0 10upx 10upx 0;
	}
	.numeric{
		height: 480upx;
		position: fixed;
		bottom: 0;
		left: 0;
		width: 100%;
		background: #EBEBEB;
		display: flex;
		justify-content: center;
		z-index: 2;
		flex-wrap: wrap;
		animation: msgBox .2s linear;
	}
	.num{
		box-sizing: border-box;
		width: 250upx;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		background: #fff;
		font-size: 40upx;
		color: #333;
		height: 120upx;
		border: 1upx solid #F2F2F2;
		border-top:none;
		border-left:none;
	}
	.numColor{
		background: #FF0000;
	}
	.forget{
		font-size: 28upx;
		font-weight: 500;
		color: #3D84EA;
		text-align: center;
		line-height: 80upx;
	}
	.amend1{
		border: 1upx solid #CCCFD6;
		background-color: #CCCFD6;
	}
	.amend3{
		font-size: 60upx;
		border: 1upx solid #CCCFD6;
		background-color: #CCCFD6;
	}
	/* .amend11{
		position: absolute;
		top: 313upx;
		left: 0upx;
		background-color: #CCCFD6;
		border: 1upx solid #FF0000;
	}
	.amend1{
		height: 100upx !important;
		position: absolute;
		top: 306upx;
		left: 0upx;
		z-index: 99;
		background-color: #CCCFD6;
		border: 2upx solid #CCCFD6;
	}
	.amend2{
		position: absolute;
		top: 306upx;
		left: 250upx;
		z-index: 99;
	}
	.amend3{
		position: absolute;
		top: 306upx;
		left: 500upx;
		z-index: 99;
		font-size: 60upx;
		border: 0upx;
		background-color: #CCCFD6;
	} */
	
</style>