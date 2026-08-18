package com.cetc28s.minichatjava.model.context;

/**
 * 用户全局上下文
 */
public class UserContext {
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();

    public static void setCurrentUser(String username) {
        currentUser.set(username);
    }

    public static String getCurrentUser() {
        return currentUser.get();
    }

    // 非常重要：请求结束后必须清理，防止内存泄漏
    public static void clear() {
        currentUser.remove();
    }
}