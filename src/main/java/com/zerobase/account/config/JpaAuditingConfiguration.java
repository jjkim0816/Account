package com.zerobase.account.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 서버 기동 시 자동으로 bean 에 등록되어 JPA 처리
 * 시 @EntityListeners(AuditingEntityListener) @CreatedDate @LastModifiedDate 에
 * 선언된 속성에 값을 자동으로 셋팅한다.
 */

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
}
