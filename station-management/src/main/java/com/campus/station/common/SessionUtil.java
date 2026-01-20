package com.campus.station.common;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.campus.station.model.SysAdmin;
import com.campus.station.model.SysUser;

import jakarta.servlet.http.HttpSession;

/**
 * Session 工具类
 * 用于管理用户登录态，存取当前登录用户信息
 */
public class SessionUtil {

    private static final String SESSION_USER_KEY = "CURRENT_USER";
    private static final String SESSION_ADMIN_KEY = "CURRENT_ADMIN";

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

    public static void setCurrentUser(SysUser user) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute(SESSION_USER_KEY, user);
        }
    }

    public static SysUser getCurrentUser() {
        HttpSession session = getSession();
        if (session == null) {
            return null;
        }
        return (SysUser) session.getAttribute(SESSION_USER_KEY);
    }

    public static Long getCurrentUserId() {
        SysUser user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public static void clearCurrentUser() {
        HttpSession session = getSession();
        if (session != null) {
            session.removeAttribute(SESSION_USER_KEY);
        }
    }

    public static void setCurrentAdmin(SysAdmin admin) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute(SESSION_ADMIN_KEY, admin);
        }
    }

    public static SysAdmin getCurrentAdmin() {
        HttpSession session = getSession();
        if (session == null) {
            return null;
        }
        return (SysAdmin) session.getAttribute(SESSION_ADMIN_KEY);
    }

    public static void clearCurrentAdmin() {
        HttpSession session = getSession();
        if (session != null) {
            session.removeAttribute(SESSION_ADMIN_KEY);
        }
    }

    public static void invalidateSession() {
        HttpSession session = getSession();
        if (session != null) {
            session.invalidate();
        }
    }
}
