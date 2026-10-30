package com.kolyvanova.chatter.image;

import org.apache.tika.Tika;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(MediaProperties.class)
@Configuration
public class FileConfig {

    @Bean
    public Tika tika(){
        return new Tika();
    }
}
