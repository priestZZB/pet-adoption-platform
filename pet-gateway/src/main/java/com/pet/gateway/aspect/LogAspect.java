package com.pet.gateway.aspect;

import com.pet.framework.annotation.Log;
import com.pet.module.system.service.OperationLogService;
import io.swagger.annotations.ApiOperation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志切面
 * 自动拦截带 @Log 注解的 Controller 中的 POST/PUT/DELETE 操作，
 * 记录操作人、操作内容、IP 等信息到数据库和文件
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger operationLog = LoggerFactory.getLogger("OPERATION_LOG");

    @Autowired
    private OperationLogService operationLogService;

    @AfterReturning(
            pointcut = "within(@com.pet.framework.annotation.Log *) && " +
                       "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
                       "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
                       "@annotation(org.springframework.web.bind.annotation.DeleteMapping))"
    )
    public void doAfterReturning(JoinPoint joinPoint) {
        // 1. 获取当前请求
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return;
        }
        HttpServletRequest request = attrs.getRequest();

        // 2. 获取当前用户（由 AuthInterceptor 注入到 request attribute）
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            return;
        }
        Long userId = Long.valueOf(userIdAttr.toString());

        // 3. 获取模块名（从类上的 @Log 注解）
        Class<?> targetClass = joinPoint.getTarget().getClass();
        Log logAnnotation = targetClass.getAnnotation(Log.class);
        String module = (logAnnotation != null) ? logAnnotation.value() : "";

        // 4. 获取角色
        Object roleAttr = request.getAttribute("role");
        String role = (roleAttr != null) ? roleAttr.toString() : "";

        // 5. 构建人类可读的操作描述（从 @ApiOperation 取值 + 关键参数）
        String action = buildActionDescription(joinPoint, request);

        // 6. 获取客户端 IP
        String ip = getClientIp(request);

        // 7. 写入数据库（不影响主业务，失败只打 warn）
        try {
            operationLogService.addLog(userId, null, module, action, ip);
        } catch (Exception e) {
            LoggerFactory.getLogger(LogAspect.class).warn("操作日志写入数据库失败", e);
        }

        // 8. 写入文件
        operationLog.info("[{}] userId={} role={} {} ip={}", module, userId, role, action, ip);
    }

    /**
     * 构建人类可读的操作描述
     * 优先使用 @ApiOperation 的值，并提取路径参数作为补充信息
     */
    private String buildActionDescription(JoinPoint joinPoint, HttpServletRequest request) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 优先从 @ApiOperation 取描述
        ApiOperation apiOp = method.getAnnotation(ApiOperation.class);
        String actionDesc = (apiOp != null) ? apiOp.value() : (request.getMethod() + " " + request.getRequestURI());

        // 提取 @PathVariable 和 @RequestParam 参数值，追加到描述后面
        String paramInfo = extractParamInfo(joinPoint, method);
        if (!paramInfo.isEmpty()) {
            actionDesc += "(" + paramInfo + ")";
        }

        return actionDesc;
    }

    /**
     * 提取方法参数中的 @PathVariable 和 @RequestParam 值
     */
    private String extractParamInfo(JoinPoint joinPoint, Method method) {
        Object[] args = joinPoint.getArgs();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        List<String> parts = new ArrayList<>();

        for (int i = 0; i < paramAnnotations.length && i < args.length; i++) {
            for (Annotation ann : paramAnnotations[i]) {
                String paramName = null;

                if (ann instanceof PathVariable) {
                    paramName = ((PathVariable) ann).value();
                    if (paramName.isEmpty()) {
                        paramName = ((PathVariable) ann).name();
                    }
                } else if (ann instanceof RequestParam) {
                    paramName = ((RequestParam) ann).value();
                    if (paramName.isEmpty()) {
                        paramName = ((RequestParam) ann).name();
                    }
                }

                if (paramName != null && !paramName.isEmpty() && args[i] != null) {
                    String val = args[i].toString();
                    // 跳过太长的值和分页参数
                    if (val.length() < 50 && !"1".equals(val) && !"10".equals(val)) {
                        parts.add(paramName + ":" + val);
                    }
                }
                break; // 一个参数只处理一个注解
            }
        }

        return String.join(", ", parts);
    }

    /**
     * 获取客户端真实 IP（支持 X-Forwarded-For）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
