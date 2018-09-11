package com.ylli.api.base;

import com.ylli.api.base.annotation.Debug;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Created by RexQian on 2017/3/14.
 */
@RestController
@RequestMapping("/base/doc")
@Debug
public class EndpointDocController {
    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public EndpointDocController(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @GetMapping
    Object get() {
        Map<RequestMappingInfo, HandlerMethod> map
                = this.handlerMapping.getHandlerMethods();
        List<String> list = new ArrayList<>();
        for (RequestMappingInfo requestMappingInfo : map.keySet()) {
            String method = requestMappingInfo.getMethodsCondition().getMethods().toString();
            String api = requestMappingInfo.getPatternsCondition().getPatterns().toString();
            list.add(method + " " + api);
        }
        return list;
    }
}
