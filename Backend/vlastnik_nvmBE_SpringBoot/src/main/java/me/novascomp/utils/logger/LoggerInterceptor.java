package me.novascomp.utils.logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import java.util.Enumeration;
import java.util.logging.Level;
import me.novascomp.messages.config.BeansInit;

@Component
public class LoggerInterceptor extends HandlerInterceptorAdapter {

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(LoggerInterceptor.class.getName());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        LOG.log(Level.INFO, "[preHandle][{0}]{1}{2} / {3}", new Object[]{request.getMethod(), request.getRequestURI(), getParameters(request), getRemoteAddr(request)});
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        LOG.log(Level.INFO, "[postHandle][{0}][{1}{2}] STATUS: {3} / {4}", new Object[]{request.getMethod(), request.getRequestURI(), getParameters(request), response.getStatus(), getRemoteAddr(request)});
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) throws Exception {
        LOG.log(Level.INFO, "[afterCompletion][{0}][{1}{2}] STATUS: {3} / {4}", new Object[]{request.getMethod(), request.getRequestURI(), getParameters(request), response.getStatus(), getRemoteAddr(request)});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
    }

    private String getParameters(HttpServletRequest request) {
        //taken form somewhere
        StringBuffer posted = new StringBuffer();
        Enumeration<?> e = request.getParameterNames();
        if (e != null) {
            posted.append("?");
        }
        while (e.hasMoreElements()) {
            if (posted.length() > 1) {
                posted.append("&");
            }
            String curr = (String) e.nextElement();
            posted.append(curr + "=");
            if (curr.contains("password")
                    || curr.contains("pass")
                    || curr.contains("pwd")) {
                posted.append("*****");
            } else {
                posted.append(request.getParameter(curr));
            }
        }
        String ip = request.getHeader("X-FORWARDED-FOR");
        String ipAddr = (ip == null) ? getRemoteAddr(request) : ip;
        if (ipAddr != null && !ipAddr.equals("")) {
            posted.append("&_psip=" + ipAddr);
        }
        return posted.toString();
    }

    private String getRemoteAddr(HttpServletRequest request) {
        //taken form somewhere
        String ipFromHeader = request.getHeader("X-FORWARDED-FOR");
        if (ipFromHeader != null && ipFromHeader.length() > 0) {
            return ipFromHeader;
        }
        return request.getRemoteAddr();
    }
}
