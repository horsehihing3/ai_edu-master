package com.edu.platform;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class HashGeneratorTest {
    @Test
    void printHashes() {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        String h = enc.encode("Admin@1234");
        System.out.println("Admin@1234 : " + h);
        System.out.println("length     : " + h.length());
        System.out.println("valid      : " + enc.matches("Admin@1234", h));
    }
}
