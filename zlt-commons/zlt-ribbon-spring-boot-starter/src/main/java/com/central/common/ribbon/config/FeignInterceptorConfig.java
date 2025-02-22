package com.central.common.ribbon.config;

import cn.hutool.core.util.StrUtil;
import com.central.common.constant.CommonConstant;
import com.central.common.constant.SecurityConstants;
import com.central.common.utils.TenantContextHolder;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * feign拦截器
 *
 * @author zlt
 */
public class FeignInterceptorConfig {

    /**
     * 使用feign client访问别的微服务时，将access_token、username、roles、client等信息放入header传递给下一个服务
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        RequestInterceptor requestInterceptor = template -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                //传递access_token，无网络隔离时需要传递
                /*
                String token = extractHeaderToken(request);
                if (StrUtil.isEmpty(token)) {
                    token = request.getParameter(CommonConstant.ACCESS_TOKEN);
                }
                if (StrUtil.isNotEmpty(token)) {
                    template.header(CommonConstant.TOKEN_HEADER, CommonConstant.BEARER_TYPE + " " + token);
                }
                */

                //传递userid
                String userid = request.getHeader(SecurityConstants.USER_ID_HEADER);
                if (StrUtil.isNotEmpty(userid)) {
                    template.header(SecurityConstants.USER_ID_HEADER, userid);
                }

                //传递username
                String username = request.getHeader(SecurityConstants.USER_HEADER);
                if (StrUtil.isNotEmpty(username)) {
                    template.header(SecurityConstants.USER_HEADER, username);
                }

                //传递roles
                String roles = request.getHeader(SecurityConstants.ROLE_HEADER);
                if (StrUtil.isNotEmpty(roles)) {
                    template.header(SecurityConstants.ROLE_HEADER, roles);
                }

                //传递client
                String tenant = TenantContextHolder.getTenant();
                if (StrUtil.isNotEmpty(tenant)) {
                    template.header(SecurityConstants.TENANT_HEADER, tenant);
                }

                //传递日志traceId
                String traceId = MDC.get(CommonConstant.LOG_TRACE_ID);
                if (StrUtil.isNotEmpty(traceId)) {
                    template.header(CommonConstant.TRACE_ID_HEADER, traceId);
                }
            }
        };
        return requestInterceptor;
    }

    /**
     * 解析head中的token
     * @param request
     */
    private static String extractHeaderToken(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaders(CommonConstant.TOKEN_HEADER);
        while (headers.hasMoreElements()) {
            String value = headers.nextElement();
            if ((value.toLowerCase().startsWith(CommonConstant.BEARER_TYPE.toLowerCase()))) {
                String authHeaderValue = value.substring(CommonConstant.BEARER_TYPE.length()).trim();
                int commaIndex = authHeaderValue.indexOf(',');
                if (commaIndex > 0) {
                    authHeaderValue = authHeaderValue.substring(0, commaIndex);
                }
                return authHeaderValue;
            }
        }
        return null;
    }
}
