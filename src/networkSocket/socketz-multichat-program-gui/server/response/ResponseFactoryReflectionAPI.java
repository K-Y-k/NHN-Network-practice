/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2024. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.nhnacademy.server.method.response;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class ResponseFactory {

    private static final ArrayList<Response> responseList = new ArrayList<>();

    /* Reflection api 덕분에 responseList를 초기화 하는 코드가 필요 없어졌습니다.
        - 아래와 같은 코드들이 생략되었음을 확인할 수 있습니다.
        - response 객체가 추가시 response만 구현해 주면 됩니다.
        - 더 이상 ResponseFactory 코드가 변경 되지 않습니다.

        add(new EchoResponse());
        add(new TimeResponse());
        add(new PortResponse());
        add(new LoginResponse());
        add(new BroadCastResponse());
        add(new WhisperResponse());
        add(new WhoamiResponse());
     */


    // pom.xml에 등록한 Reflection api 덕분에 사용 가능
    // maven-repository : https://mvnrepository.com/artifact/org.reflections/reflections
    // [참고] https://www.baeldung.com/reflections-library
    static {
        //TODO#2-1 "com.nhnacademy.server" 패키지를 기준으로 class를 scan 합니다.
        Reflections reflections = new Reflections("com.nhnacademy.server");

        //TODO#2-2 reflections로 부터 Response.class를 구현한 subType을 조회 합니다.
        Set<Class<? extends Response>> classes = reflections.getSubTypesOf(Response.class);

        for (Class<? extends Response> clazz : classes) {
            try {
                //TODO#2-3 getDeclaredConstructor().newInstance() method를 호출해서 인스턴스로 생성 합니다.
                Response response = clazz.getDeclaredConstructor().newInstance();
                log.debug("response-factory init :  instance :{}", response.getClass().getName());
                
                //TODO#2-4 생성된 response instance를 responseList에 추가 합니다.
                responseList.add(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Response getResponse(String method){
        Response response = responseList.stream()
                .filter(o->o.validate(method))
                .findFirst()
                .orElse(null);

        if(Objects.isNull(response)){
            log.error("response not found : {}",method);
        }
        return response;
    }
}
