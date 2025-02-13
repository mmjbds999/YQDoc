package com.hy.yqdoc.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hy.yqdoc.entity.MethodParam;
import com.hy.yqdoc.entity.RequestToMethodItem;
import com.hy.yqdoc.util.CommentReader;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ApiDcoGen {

    /**
     * 生成OpenAPI文档
     * @param request
     * @return
     */
    public String generateOpenApiJson(HttpServletRequest request) {
        Map<String, Object> openApi = new HashMap<>();
        openApi.put("openapi", "3.0.0");

        Map<String, Object> info = new HashMap<>();
        info.put("title", "API Documentation");
        info.put("version", "1.0.0");
        openApi.put("info", info);

        Map<String, Object> paths = new HashMap<>();

        List<RequestToMethodItem> apiList = getCurrentServerApi(request, null);
        for (RequestToMethodItem item : apiList) {
            Map<String, Object> operation = new HashMap<>();
            operation.put("summary", item.getDescription());
            operation.put("description", item.getDescription());

            // 设置请求参数
            List<Map<String, Object>> parameters = new ArrayList<>();
            for (MethodParam param : item.getMethodParams()) {
                Map<String, Object> parameter = new HashMap<>();
                parameter.put("name", param.getName());
                parameter.put("in", "query");
                parameter.put("required", true);

                // 根据类型设置schema
                String type = param.getType().getSimpleName().toLowerCase();
                parameter.put("schema", Map.of("type", type));
                parameter.put("description", param.getComment()); // 添加参数的注释
                parameters.add(parameter);
            }
            operation.put("parameters", parameters);

            // 添加响应信息
            Map<String, Object> responses = new HashMap<>();
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("description", "成功");
            successResponse.put("content", Map.of(
                    "application/json", Map.of("schema", Map.of("type", "object"))
            ));
            responses.put("200", successResponse);
            operation.put("responses", responses);

            String requestMethod = item.getRequestType()[0].toLowerCase();
            String requestUrl = item.getRequestUrl();

            // 添加tags属性
            String controllerComment = item.getControllerComment();
            operation.put("tags", List.of(controllerComment)); // 使用控制器注释作为tag

            paths.put(requestUrl, Map.of(requestMethod, operation));
        }

        openApi.put("paths", paths);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(openApi);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}"; // 返回空的 JSON
        }
    }

    /**
     * 获取当前服务的API
     * @param request
     * @return
     * @throws ClassNotFoundException
     */
    public List<RequestToMethodItem> getCurrentServerApi(HttpServletRequest request, String keyWord) {
        ServletContext context = request.getSession().getServletContext();
        if (context == null) {
            return null;
        }

        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(context);
        // 请求url和处理方法的映射
        List<RequestToMethodItem> requestToMethodItemList = new ArrayList<>();
        // 获取所有的RequestMapping
        Map<String, HandlerMapping> allRequestMappings = BeanFactoryUtils.beansOfTypeIncludingAncestors(webApplicationContext, HandlerMapping.class, true, false);

        for (HandlerMapping handlerMapping : allRequestMappings.values()) {
            if (handlerMapping instanceof RequestMappingHandlerMapping) {
                RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) handlerMapping;
                Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
                for (Map.Entry<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethodEntry : handlerMethods.entrySet()) {
                    RequestMappingInfo requestMappingInfo = requestMappingInfoHandlerMethodEntry.getKey();
                    HandlerMethod mappingInfoValue = requestMappingInfoHandlerMethodEntry.getValue();

                    RequestMethodsRequestCondition methodCondition = requestMappingInfo.getMethodsCondition();
                    List<String> typeList = new ArrayList<>();
                    Set<RequestMethod> methods = methodCondition.getMethods();
                    if (methods.size() == 0) {
                        // 没指定只处理这2种
                        typeList.add("GET");
                        typeList.add("POST");
                    } else {
                        for (RequestMethod method : methods) {
                            typeList.add(method.name());
                        }
                    }
                    String[] requestType = typeList.toArray(new String[typeList.size()]);

                    PatternsRequestCondition patternsCondition = requestMappingInfo.getPatternsCondition();
                    String requestUrl = patternsCondition.getPatterns().iterator().next();

                    if(!requestUrl.contains("/api/")){
                        continue;
                    }

                    String controllerName = mappingInfoValue.getBeanType().toString().substring(6);

                    Map methodComment = CommentReader.readMethodComment(controllerName, mappingInfoValue.getMethod().getName());
                    String controllerComment = methodComment.get("classComment").toString();
                    String description = methodComment.get("comment").toString();

                    if(StringUtils.hasText(keyWord)){
                        if(!description.contains(keyWord) && !requestUrl.contains(keyWord)){
                            continue;
                        }
                    }

                    String requestMethodName = mappingInfoValue.getMethod().getName();
                    Class<?>[] methodParamTypes = mappingInfoValue.getMethod().getParameterTypes();

                    LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
                    String[] methodParamName = discoverer.getParameterNames(mappingInfoValue.getMethod());
                    List<MethodParam> methodParamList = new ArrayList<>();
                    int i = 0;

                    String contentType = "application/x-www-form-urlencoded";
                    // 获取方法的参数注解
                    Annotation[][] parameterAnnotations = mappingInfoValue.getMethod().getParameterAnnotations();
                    if (parameterAnnotations != null && parameterAnnotations.length > 0) {
                        for (Annotation[] annotations : parameterAnnotations){
                            for (Annotation annotation : annotations) {
                                if (annotation instanceof RequestBody) {
                                    contentType = "application/json";
                                    break;
                                }
                            }
                        }
                    }
                    if (methodParamTypes.length > 0) {
                        String packageName = getBasePackage(getThreadPoint(controllerName));
                        for (Class<?> type : methodParamTypes) {
                            if (!type.getSimpleName().equals("HttpServletRequest")
                                    && !type.getSimpleName().equals("HttpServletResponse")
                                    && !type.getName().equals("org.springframework.ui.Model")) {
                                if(type.getName().startsWith(packageName+".")){
                                    //如果是当前项目包下的类，则作为实体类的参数读取字段注释
                                    Field[] fields = type.getDeclaredFields();
                                    for (Field field : fields) {
                                        // 过滤掉 serialVersionUID 字段
                                        if ("serialVersionUID".equals(field.getName())) {
                                            continue;
                                        }
                                        // 过滤掉类型为 java.util.List 的字段
                                        if (field.getType().equals(java.util.List.class)) {
                                            continue;
                                        }
                                        String comment = CommentReader.readFieldComment(type.getName(), field.getName());
                                        // 类型名称包含 .entity. 的字段 comment 加上一些注释
                                        if (field.getType().getName().contains(".entity.")) {
                                            comment = comment + "（直接填入ID字段即可）";
                                        }
                                        MethodParam methodParam = new MethodParam(type.getSimpleName()+"."+field.getName(), field.getType(), comment);
                                        methodParamList.add(methodParam);
                                    }
                                }else{
                                    //否则当作正常参数解析
                                    Map commentMap = (LinkedHashMap)methodComment.get("params");
                                    MethodParam methodParam = new MethodParam(methodParamName[i], type, commentMap.get(methodParamName[i])==null?"无":commentMap.get(methodParamName[i]).toString());
                                    methodParamList.add(methodParam);
                                }
                            }
                            i++;
                        }
                    }
                    RequestToMethodItem item = new RequestToMethodItem(requestUrl, requestType, controllerName, controllerComment, requestMethodName, methodParamList, description, contentType);
                    requestToMethodItemList.add(item);
                    requestToMethodItemList = requestToMethodItemList.stream()
                            .sorted(Comparator.comparing(RequestToMethodItem::getRequestUrl))
                            .collect(Collectors.toList());
                }
                break;
            }
        }
        return requestToMethodItemList;
    }

    /**
     * 获取第三个点之前的字符串
     * @param str
     * @return
     */
    private String getThreadPoint(String str){
        int thirdDotIndex = -1;
        for (int i = 0, count = 0; i < str.length(); i++) {
            if (str.charAt(i) == '.') {
                count++;
                if (count == 3) {
                    thirdDotIndex = i;
                    break;
                }
            }
        }
        if (thirdDotIndex != -1) {
            return str.substring(0, thirdDotIndex);
        } else {
            return "";
        }
    }

    /**
     * 获取基础包名
     * @param packageName
     * @return
     */
    private String getBasePackage(String packageName){
        Pattern pattern = Pattern.compile("([^.]+)\\.([^.]+)\\.([^.]+)");
        Matcher matcher = pattern.matcher(packageName);
        if (matcher.find()) {
            return matcher.group(1) + "." + matcher.group(2) + "." + matcher.group(3);
        } else {
            return null;
        }
    }

}
