package com.blank.humanity.discordbot.config.web3;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.ens.EnsResolver;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;

@Configuration
public class Web3Config {

    @Bean
    public Web3j web3j(Web3jConfiguration configuration) {
        Web3jService service = new HttpService(configuration.getUrl());
        return Web3j.build(service);
    }
    
    @Bean
    public EnsResolver ensResolver(Web3j web3) {
        return new EnsResolver(web3);
    }

}
