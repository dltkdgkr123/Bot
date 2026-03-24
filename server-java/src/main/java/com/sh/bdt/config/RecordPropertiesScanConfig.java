package com.sh.bdt.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationPropertiesScan(basePackages = "com.sh.bdt.property") // 컴포넌트 스캔 범위 밖 이여서 명시 (현재 동등한 depth dir)
public class RecordPropertiesScanConfig { // record 형식 properties 스캔을 위함

}
