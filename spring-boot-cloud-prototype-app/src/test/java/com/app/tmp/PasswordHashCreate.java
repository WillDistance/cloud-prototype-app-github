package com.app.tmp;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

/**
 * 密码哈希值生成
 *
 * @Author YL
 * @Create 2026/9/6 23:51
 */
public class PasswordHashCreate {
    public static void main(String[] args) {
        String password= "123456";
        Argon2PasswordEncoder argon2PasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        String encode = argon2PasswordEncoder.encode(password);
        boolean matches = argon2PasswordEncoder.matches(password, encode);
        System.out.println("password encode " + encode);
        System.out.println("password matches " + matches);
    }
}
