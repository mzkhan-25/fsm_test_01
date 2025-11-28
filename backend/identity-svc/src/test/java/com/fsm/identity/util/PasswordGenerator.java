package com.fsm.identity.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String[] passwords = {"admin123", "dispatcher123", "supervisor123", "tech123"};
        
        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println(password + ": " + hash);
        }
    }
}
