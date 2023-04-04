package com.example.pradhuman.utils;

import com.aerospike.client.AerospikeClient;
import com.example.pradhuman.aerospike.AerospikeConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(AerospikeConfigurationProperties.class)
public class AeroClient {

    @Autowired
    AerospikeConfigurationProperties aerospikeConfigurationProperties;

    AerospikeClient aerospikeClient;

//    @Bean
//    AerospikeClient getClient(){
//        return new AerospikeClient(aerospikeConfigurationProperties.getHost(),
//                aerospikeConfigurationProperties.getPort());
//    }


}
