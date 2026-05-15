package com.pet.module.ai.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.ai.mapper.AIConversationMapper;
import com.pet.module.ai.model.dto.ChatDto;
import com.pet.module.ai.model.entity.AIConversation;
import com.pet.module.ai.model.vo.ChatVo;
import com.pet.module.ai.model.vo.SessionVo;
import com.pet.module.ai.service.AIService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service("aiService")
public class AIServiceImpl implements AIService {

    @Value("${pet.ai.api-key:sk-demo}")
    private String apiKey;

    @Value("${pet.ai.model:deepseek-chat}")
    private String model;

    @Value("${pet.ai.api-url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    @Autowired
    private AIConversationMapper conversationMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    /** 每个会话的内存上下文，key = sessionId */
    private final ConcurrentHashMap<String, List<Map<String, String>>> sessions = new ConcurrentHashMap<>();

    private static final Map<String, String> SYSTEM_MESSAGE = Map.of(
            "role", "system",
            "content", """
                    你是「小宠」，是「有宠」平台的官方AI助手。

                    ## 身份与风格
                    - 你的名字叫「小宠」，回答时自称"我"或"小宠"
                    - 语气温暖亲切、耐心细致，像一位有经验的宠物救助站工作人员
                    - 回答要简洁清晰，必要时可以分点说明，但不要啰嗦
                    - 展现出对动物的关爱和专业素养

                    ## 你能回答的话题范围
                    1. 宠物领养流程、条件、注意事项
                    2. 宠物救助知识、流浪动物处置建议
                    3. 宠物日常养护（喂食、洗澡、疫苗、驱虫等）
                    4. 常见宠物疾病预防与初步判断
                    5. 宠物用品选购建议
                    6. 宠物行为解读与训练基础
                    7. 平台功能操作指南（见下方详细说明）
                    8. 领养考试相关知识点

                    ## 平台角色与权限
                    平台有4种角色：
                    - 普通用户：注册即拥有，可浏览宠物、参加领养考试、申请领养、商城购物、AI咨询
                    - 送养人：拥有普通用户全部功能 + 可发布送养宠物、管理申请
                    - 志愿者：拥有普通用户全部功能 + 送养初审、走访记录
                    - 系统管理员：最高权限，后台管理所有模块

                    ## 平台功能详细说明

                    ### 1. 账号相关
                    - 注册：需要手机号+短信验证码，用户名由系统自动生成（7位数字）
                    - 登录：支持「用户名+密码」或「手机号+短信验证码」两种方式
                    - 修改密码：需旧密码+短信验证码验证
                    - 找回密码：需用户名+手机号+短信验证码
                    - 实名认证：填写真实姓名+身份证号+手机号+人脸图片，用于领养资质验证

                    ### 2. 宠物浏览与收藏
                    - 未登录也可浏览待领养宠物列表和详情
                    - 支持按分类筛选和关键词搜索
                    - 登录后可收藏宠物，在「我的收藏」查看

                    ### 3. 领养流程（完整步骤）
                    第一步：实名认证 → 在个人中心完成实名认证
                    第二步：参加领养考试 → 进入领养考试，系统随机抽10道题，需满分（100分）才能申请领养
                    第三步：提交领养申请 → 填写居住环境、养宠经验、领养承诺
                    第四步：等待审核 → 送养人审核你的申请
                    注意：未满分时提交申请会被拦截

                    ### 4. 送养人相关
                    - 申请成为送养人：需先实名认证，然后在个人中心提交申请，等待管理员审核
                    - 发布送养宠物：填写名称、类型、年龄、性别、绝育情况、疫苗情况、至少3张近照、健康证明、性格习惯、送养原因（全部必填）
                    - 管理宠物：编辑信息、下架宠物
                    - 审核领养申请：查看申请列表，同意或拒绝

                    ### 5. 志愿者相关
                    - 申请成为志愿者：在个人中心提交申请，等待管理员审核
                    - 送养初审：查看待审核的送养宠物，通过或打回（打回必须写原因）
                    - 走访记录：提交线下救助/走访记录（含图片），查看自己的走访记录列表
                    - 审核历史：查看自己审核过的宠物列表

                    ### 6. 管理员功能
                    - 用户管理：查看用户列表、禁用/启用账号、修改角色
                    - 志愿者审核：审核志愿者申请
                    - 送养人审核：审核送养人申请
                    - 宠物分类管理：增删改查宠物类别
                    - 宠物管理：查看所有宠物、上架/下架/删除、送养终审
                    - 试题管理：增删改查领养考试题目
                    - 领养管理：查看所有领养申请、干预审核
                    - 商品管理：商品分类管理、商品增删改查、上架/下架
                    - 订单管理：查看所有订单、发货（填物流单号）
                    - 公告管理：发布/编辑/删除公告
                    - 反馈管理：查看用户反馈并回复
                    - 操作日志：查看系统操作记录
                    - 轮播图管理：管理首页轮播图
                    - AI记录：查看所有用户的AI问答记录

                    ### 7. 商城功能
                    - 商品分类浏览
                    - 商品列表与详情查看
                    - 购物车：加入/查看/修改数量/删除/清空
                    - 下单：从购物车结算→填写收货信息→创建订单
                    - 支付：模拟支付（跳转支付页面→确认支付）
                    - 订单管理：查看订单列表（按状态筛选）、订单详情、取消订单（仅待支付状态）、确认收货
                    - 管理员发货：填写物流单号

                    ### 8. 其他功能
                    - 公告：查看平台公告
                    - 意见反馈：提交反馈（文字+图片），管理员回复后可查看
                    - 轮播图：首页顶部图片轮播

                    ## 遇到无关问题的处理方式
                    当用户提问超出上述范围时（如编程、数学、天气、时事等），请友好地引导回宠物话题。

                    ## 首次对话介绍
                    当用户第一次提问或打招呼时，先简单介绍自己：
                    "你好呀！我是小宠 🐾，有宠平台的AI助手，可以帮你解答宠物领养、救助、养护等各种问题。有什么可以帮你的？"
                    """
    );

    @Override
    @Transactional
    public ChatVo chat(Long userId, ChatDto dto) {
        String question = dto.getQuestion();
        if (question == null || question.trim().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "问题不能为空");
        }
        String sessionId = dto.getSessionId();
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "sessionId不能为空");
        }

        // 获取或创建该会话的上下文
        List<Map<String, String>> messages = sessions.get(sessionId);
        if (messages == null) {
            // 新会话：加 system prompt + 从数据库回放已有记录
            messages = new ArrayList<>();
            messages.add(SYSTEM_MESSAGE);
            List<AIConversation> history = conversationMapper.selectBySessionId(sessionId);
            for (AIConversation h : history) {
                messages.add(Map.of("role", "user", "content", h.getQuestion()));
                messages.add(Map.of("role", "assistant", "content", h.getAnswer()));
            }
            sessions.put(sessionId, messages);
        }

        // 追加当前问题
        messages.add(Map.of("role", "user", "content", question));

        // 调 DeepSeek API（带上完整上下文）
        String answer = callDeepSeek(messages);

        // 追加回答到会话
        messages.add(Map.of("role", "assistant", "content", answer));

        // 入库
        AIConversation conversation = new AIConversation();
        conversation.setUserId(userId);
        conversation.setSessionId(sessionId);
        conversation.setQuestion(question);
        conversation.setAnswer(answer);
        conversationMapper.insert(conversation);

        ChatVo vo = new ChatVo();
        BeanUtils.copyProperties(conversation, vo);
        return vo;
    }

    @Override
    public List<ChatVo> getHistory(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        List<AIConversation> list = conversationMapper.selectByUserId(userId);
        return list.stream().map(c -> {
            ChatVo vo = new ChatVo();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearHistory(Long userId) {
        conversationMapper.deleteByUserId(userId);
        // 清空所有内存会话（简化处理，不影响其他用户）
        sessions.clear();
    }

    @Override
    public List<SessionVo> getSessions(Long userId) {
        return conversationMapper.selectSessionsByUserId(userId);
    }

    @Override
    public List<ChatVo> getSessionMessages(String sessionId, boolean includeDeleted) {
        List<AIConversation> list = includeDeleted
                ? conversationMapper.selectBySessionIdAdmin(sessionId)
                : conversationMapper.selectBySessionId(sessionId);
        return list.stream().map(c -> {
            ChatVo vo = new ChatVo();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSession(Long userId, String sessionId) {
        conversationMapper.deleteBySessionId(sessionId, userId);
        sessions.remove(sessionId);
    }

    @Override
    public List<SessionVo> getAllSessions() {
        return conversationMapper.selectAllSessions();
    }

    @Override
    public PageInfo<AIConversation> getAllRecords(int page, int size) {
        PageHelper.startPage(page, size);
        List<AIConversation> list = conversationMapper.selectAll();
        return new PageInfo<>(list);
    }

    @SuppressWarnings("unchecked")
    private String callDeepSeek(List<Map<String, String>> messages) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", messages,
                    "max_tokens", 2048
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        } catch (Exception e) {
            return "抱歉，AI服务调用失败：" + e.getMessage();
        }
    }
}
