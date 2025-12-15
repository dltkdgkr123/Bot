package com.sh.bdt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class BdtApplication {

  public static void main(String[] args) {
    SpringApplication.run(BdtApplication.class, args);
  }
}
