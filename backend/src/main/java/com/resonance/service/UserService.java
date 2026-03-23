package com.resonance.service;

import java.util.Map;

public interface UserService {

    Map<String,Object> login(String name,String password);



    Boolean register(String name,String password);


}
