package com.mednear;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing   // @CreatedDate / @LastModifiedDate on entities
@EnableCaching       // Redis @Cacheable on MedicineService
public class MednearApplication {
    public static void main(String[] args) {
        SpringApplication.run(MednearApplication.class, args);
    }
}
