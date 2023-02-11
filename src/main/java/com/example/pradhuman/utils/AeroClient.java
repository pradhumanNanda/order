package com.example.pradhuman.utils;

import com.aerospike.client.AerospikeClient;

public class AeroClient {

    static String HOST = "0.0.0.0";
    static Integer PORT = 3000;
    public static final String NS = "orders";

    public static final String ORDER_LIST = "order_list";

    public static void save(){

        AerospikeClient client = new AerospikeClient(HOST, PORT);


    }
}
