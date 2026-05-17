<template>
  <!-- 隐私政策弹窗 -->
  <el-dialog v-model="visible.privacy" title="隐私政策" width="720px" top="6vh">
    <div class="legal-content">
      <p>PriestLab（以下简称 &ldquo;我们&rdquo;）非常重视用户的隐私与个人信息保护。本隐私政策旨在向你说明：当你访问、注册、使用我们网站（<b>adopt.priestlab.top</b>）及相关服务时，我们如何收集、使用、存储、共享、保护你的个人信息，以及你对个人信息享有的各项权利。</p>

      <h4>一、适用范围</h4>
      <p>本政策适用于所有注册用户、浏览用户、领养人、送养人、商家、志愿者及其他访问或使用本平台的自然人用户。</p>

      <h4>二、我们收集的个人信息</h4>
      <p><b>账号注册与登录：</b>用户名、手机号、邮箱、密码（加密存储）</p>
      <p><b>实名认证：</b>真实姓名、身份证号码、人脸图片</p>
      <p><b>发布宠物信息：</b>宠物照片、品种、年龄、性别、健康状况等</p>
      <p><b>领养申请：</b>家庭情况、居住环境、养宠经验、联系方式</p>
      <p><b>商城购物：</b>收货人姓名、手机号、收货地址、订单信息</p>
      <p><b>技术信息：</b>IP地址、设备型号、浏览器类型、操作日志</p>

      <h4>三、信息使用与保护</h4>
      <p>我们仅将信息用于提供平台服务、保障安全、改善体验、合规要求。不会出售、出租、交易你的个人信息。</p>
      <p>数据加密存储，采用HTTPS传输，定期安全检测。实名信息仅在认证场景使用。</p>

      <h4>四、你的权利</h4>
      <p>你可在个人中心查看、更正信息；可申请注销账号；可撤回对非必要信息的授权。</p>

      <h4>五、联系方式</h4>
      <p>邮箱：<a href="mailto:zzb070104@163.com">zzb070104@163.com</a></p>
    </div>
  </el-dialog>

  <!-- 用户协议弹窗 -->
  <el-dialog v-model="visible.terms" title="用户协议" width="720px" top="6vh">
    <div class="legal-content">
      <p>本用户协议是您与 PriestLab 之间，关于使用平台全部服务所订立的有效合约。</p>

      <h4>一、账号注册与使用</h4>
      <p>用户需提供真实有效的手机号进行注册，用户名由系统自动生成且不可修改。每个手机号仅限注册一个账号。禁止转借、出租、出售账号。</p>

      <h4>二、用户行为规范</h4>
      <p>不得发布虚假、违法违规、低俗色情信息；不得恶意攻击他人；不得爬取接口、攻击服务器。违者平台有权封禁账号。</p>

      <h4>三、领养规范</h4>
      <p>领养人须完成实名认证并通过领养考试（满分100分）。禁止遗弃、虐待、转卖领养宠物。</p>

      <h4>四、送养规范</h4>
      <p>送养人须为宠物实际主人。发布信息须真实完整（至少3张照片+健康证明）。禁止售卖宠物。</p>

      <h4>五、志愿者规范</h4>
      <p>志愿者须公正客观审核，不得谋取私利、泄露隐私。</p>

      <h4>六、免责声明</h4>
      <p>平台仅作为信息中介，不参与线下交接，不对双方私下行为承担法律责任。</p>

      <h4>七、联系方式</h4>
      <p>邮箱：<a href="mailto:zzb070104@163.com">zzb070104@163.com</a></p>
    </div>
  </el-dialog>

  <!-- 免责声明弹窗 -->
  <el-dialog v-model="visible.disclaimer" title="免责声明" width="720px" top="6vh">
    <div class="legal-content">
      <h4>一、平台角色</h4>
      <p>有宠平台仅为宠物领养、送养、公益救助及商城服务提供网络空间与技术支持的第三方信息中介平台，不参与用户之间私下约定与线下交接。</p>

      <h4>二、信息真实性</h4>
      <p>平台无法对每一条用户发布信息进行逐一完全核验，用户需自行审慎甄别。</p>

      <h4>三、领养风险</h4>
      <p>领养产生的纠纷、损失由当事人自行承担，平台不承担任何直接或间接赔偿责任。</p>

      <h4>四、商城服务</h4>
      <p>商品质量及售后由对应商家独立承担责任。</p>

      <h4>五、AI助手</h4>
      <p>AI回答仅作参考，不具备专业医疗及法律意见资质。</p>

      <h4>六、不可抗力</h4>
      <p>因自然灾害、网络故障等导致服务中断的，平台免责。</p>
    </div>
  </el-dialog>

  <!-- 帮助中心弹窗 -->
  <el-dialog v-model="visible.help" title="帮助中心" width="700px" top="6vh">
    <div class="legal-content">
      <h4>一、账号相关</h4>
      <p><b>如何注册？</b>点击注册按钮，输入手机号、验证码、密码即可完成注册，用户名由系统自动生成。</p>
      <p><b>忘记密码？</b>在登录页点击"忘记密码"，通过用户名+手机号+验证码重置。</p>

      <h4>二、领养流程</h4>
      <p>实名认证 → 领养考试（满分100分）→ 提交申请 → 等待审核</p>

      <h4>三、如何成为送养人？</h4>
      <p>实名认证后，在个人中心申请成为送养人，等待管理员审核。</p>

      <h4>四、如何成为志愿者？</h4>
      <p>在个人中心申请成为志愿者，等待管理员审核。</p>

      <h4>五、商城购物</h4>
      <p>浏览商品 → 加入购物车 → 结算 → 支付 → 等待发货</p>

      <h4>六、联系方式</h4>
      <p>邮箱：<a href="mailto:zzb070104@163.com">zzb070104@163.com</a></p>
    </div>
  </el-dialog>
</template>

<script setup>
import { reactive } from 'vue'

const visible = reactive({ privacy: false, terms: false, disclaimer: false, help: false })

function open(name) {
  visible[name] = true
}

defineExpose({ open })
</script>

<style scoped>
.legal-content { max-height: 500px; overflow-y: auto; padding: 4px 0; line-height: 1.8; text-align: left; }
.legal-content h4 { font-size: 15px; color: #303133; margin: 18px 0 8px; }
.legal-content h4:first-child { margin-top: 0; }
.legal-content p { font-size: 14px; color: #606266; margin: 0 0 8px; }
.legal-content a { color: #409EFF; text-decoration: none; }
</style>
