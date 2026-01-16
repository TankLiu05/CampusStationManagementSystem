package com.campus.station.common;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.campus.station.model.SysUser;

import jakarta.servlet.http.HttpSession;

/**
 * Session 工具类
 * 用于管理用户登录态，存取当前登录用户信息
 */
public class SessionUtil {

    private static final String SESSION_USER_KEY = "CURRENT_USER";

    /**
     * 获取当前 HttpSession
     */
    private static HttpSession getSession() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest().getSession();
    }

    /**
     * 将用户信息存入 Session（登录时调用）
     */
    public static void setCurrentUser(SysUser user) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute(SESSION_USER_KEY, user);
        }
    }

    /**
     * 从 Session 中获取当前登录用户
     * @return 当前登录用户，未登录则返回 null
     */
    public static SysUser getCurrentUser() {
        HttpSession session = getSession();
        if (session == null) {
            return null;
        }
        return (SysUser) session.getAttribute(SESSION_USER_KEY);
    }

    /**
     * 获取当前登录用户的 ID
     * @return 用户 ID，未登录则返回 null
     */
    public static Long getCurrentUserId() {
        SysUser user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 判断当前是否已登录
     */
    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * 清除 Session 中的用户信息（登出时调用）
     */
    public static void clearCurrentUser() {
        HttpSession session = getSession();
        if (session != null) {
            session.removeAttribute(SESSION_USER_KEY);
        }
    }

    /**
     * 销毁整个 Session
     */
    public static void invalidateSession() {
        HttpSession session = getSession();
        if (session != null) {
            session.invalidate();
        }
    }
}
