package com.hy.yqdoc.controller;

import com.alibaba.fastjson.JSON;
import com.hy.yqdoc.component.ApiDcoGen;
import com.hy.yqdoc.entity.RequestToMethodItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class ApiController {

    Map map = new HashMap();

    @Resource
    ApiDcoGen apiDcoGen;

    @GetMapping("/doc")
    public String doc(Model model, HttpServletRequest request, String keyWord, String token){
        if (!isAllowedAccess()) {
            return "error";
        }
        if(StringUtils.hasText(token)){
            map.put("token", token);
        }else{
            token = map.get("token")==null?"":(String) map.get("token");
        }
        Map<String, List<RequestToMethodItem>> map = getServerMaps(request, keyWord);

        Object obj = JSON.toJSON(map);
        model.addAttribute("keyWord", keyWord==null?"":keyWord);
        model.addAttribute("token", token);
        model.addAttribute("allApi", obj);
        model.addAttribute("baseUrl", getBaseUrl(request));
        return "apis";
    }

    /**
     * 下载openapi.json
     * @param request
     * @return
     */
    @GetMapping("/doc/openapi")
    public String downloadOpenApiJson(Model model, HttpServletRequest request) {
        if (!isAllowedAccess()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "prod和production标记的生产环境不允许访问DOC！");
        }
        String openApiJson = apiDcoGen.generateOpenApiJson(request);
        model.addAttribute("openapi", openApiJson);
        return "openapi";
    }

    @GetMapping("/doc/saveToken")
    public void saveToken(String token) {
        if (!isAllowedAccess()) {
            return;
        }
        map.put("token", token);
    }

    @GetMapping("/doc/removeToken")
    public void removeToken() {
        if (!isAllowedAccess()) {
            return;
        }
        map.remove("token");
    }

    private String getBaseUrl(HttpServletRequest request){
        String baseUrl = request.getScheme() + "://" + request.getServerName() +
                (request.getServerPort() == 80 ? "" : ":" + request.getServerPort()) +
                request.getContextPath();
        return baseUrl;
    }

    /**
     * 获取所有服务接口【放到map中主要是为了spring cloud项目】
     * 1.获取到当前服务器的接口
     * 2.获取后放进map中
     * @param request
     * @return
     */
    private Map<String, List<RequestToMethodItem>> getServerMaps(HttpServletRequest request, String keyWord) {
        Map<String, List<RequestToMethodItem>> map = new HashMap<>();
        List<RequestToMethodItem> list;
        try {
            list = apiDcoGen.getCurrentServerApi(request, keyWord);
        } catch (Exception e) {
            list = new ArrayList<>();
            log.error("获取接口失败", e);
        }
        map.put("YQCode", list);
        return map;
    }

    @Resource
    private Environment environment;

    /**
     * 判断是否是允许访问
     * @return
     */
    private boolean isAllowedAccess() {
        String activeProfile = environment.getProperty("spring.profiles.active", "default");
        System.out.println("当前环境:"+activeProfile);
        return !"prod".equals(activeProfile) && !"production".equals(activeProfile);
    }

}
