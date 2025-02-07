package com.hy.yqdoc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 注释读取工具
 */
public class CommentReader {

    /**
     * 读取方法注释
     * @param packageName 包名+类名
     * @param methodName  方法名
     * @return 方法注释
     */
    public static Map<String, Object> readMethodComment(String packageName, String methodName) {
        StringBuilder methodCommentBuilder = new StringBuilder();
        StringBuilder classCommentBuilder = new StringBuilder();
        Map<String, Object> methodComment = new HashMap<>();
        Map<String, String> parameterComments = new LinkedHashMap<>();
        boolean inMethod = false;
        boolean foundClassDefinition = false; // 标记是否找到类定义
        boolean classCommentRead = false; // 标记是否已读取类注释

        try (BufferedReader reader = new BufferedReader(new FileReader(getFilePath(packageName)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 检查类定义
                if (!foundClassDefinition && (line.startsWith("public class") || line.contains("@RestController"))) {
                    foundClassDefinition = true; // 找到类定义
                }

                // 读取类注释，读取第一行
                if (!foundClassDefinition && !classCommentRead) {
                    if (line.startsWith("/**")) {
                        classCommentRead = true; // 已读取类注释
                    }
                } else if (!foundClassDefinition && classCommentRead) {
                    // 读取类注释的第一行
                    if (line.startsWith("*")) {
                        classCommentBuilder.append(line.replace("*", "").trim());
                        classCommentRead = false; // 读取完第一行，退出
                    }
                }

                if (!inMethod && line.startsWith("/**")) {
                    inMethod = true;
                    methodCommentBuilder.append(line.replace("/**", ""));
                } else if (inMethod) {
                    if (line.startsWith("* @param")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 3) {
                            String paramName = parts[2];
                            String paramComment = parts.length == 4 ? line.substring(line.indexOf(parts[3])) : "";
                            parameterComments.put(paramName, paramComment);
                        }
                    } else if (line.startsWith("* @throws")) {
                        // 排除 @throws 注释
                        continue;
                    } else {
                        methodCommentBuilder.append(line.replace("* @return", "")
                                .replace("*/", "")
                                .replace("* ", ""));
                        if (line.endsWith("*/")) {
                            inMethod = false;
                            continue;
                        }
                    }
                }

                if (!inMethod && foundClassDefinition && line.startsWith("public ")) {
                    if (containsMethod(line, methodName)) {
                        methodComment.put("comment", methodCommentBuilder.toString().trim());
                        methodComment.put("params", parameterComments);
                        methodComment.put("classComment", classCommentBuilder.length() > 0
                                ? classCommentBuilder.toString().trim() : packageName.substring(packageName.lastIndexOf(".") + 1));
                        break;
                    } else {
                        // 清空临时数据
                        methodCommentBuilder.delete(0, methodCommentBuilder.length());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return methodComment;
    }


    /**
     * 读取字段注释
     * @param packageName 包名+类名
     * @param fieldName  字段名
     * @return 方法注释
     */
    public static String readFieldComment(String packageName, String fieldName) {
        StringBuffer temp = new StringBuffer();
        StringBuffer methodComment = new StringBuffer();
        boolean inComment = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(getFilePath(packageName)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (!inComment && line.startsWith("/**")) {
                    inComment = true;
                    temp.append(line.replace("/**", ""));
                } else if (inComment) {
                    temp.append(line.replace("* @return", "")
                            .replace("*/", "")
                            .replace("* ", ""));
                }
                if (line.endsWith("*/")) {
                    temp = new StringBuffer(temp.toString().replace("*/", ""));
                    inComment = false;
                    continue;
                }
                if (!inComment && !line.contains("@")) {
                    if(line.endsWith(fieldName+";")){
                        methodComment.append(temp);
                        break;
                    }else{
                        // 清空临时数据
                        temp.delete(0, temp.length());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return methodComment.toString();
    }

    /**
     * 判断当前行是否包含方法名
     * @param line      当前行
     * @param methodName 方法名
     * @return 包含返回true，否则返回false
     */
    private static boolean containsMethod(String line, String methodName) {
        String regex = "\\s+" + methodName + "\\s*\\(";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        return matcher.find();
    }

    /**
     * 获取当前项目的文件路径
     * @param packageName 包名
     * @return 当前项目的路径
     */
    private static String getFilePath(String packageName) {
        String fullPath = null;
        String resourcePath = packageName.replace(".", "/") + ".java";  // 转换成资源路径

        // 判断是否在 JAR 中运行
        String currentPath = System.getProperty("user.dir");
        File file = new File(currentPath + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + resourcePath);

        if (file.exists()) {
            // 在开发环境中直接读取文件
            fullPath = file.getAbsolutePath();
        } else {
            System.out.println("当前项目不在开发环境中，将使用类加载器读取资源");
            // 如果在 JAR 中运行，则使用类加载器读取
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(resourcePath);

            if (resource != null) {
                fullPath = resource.getPath();  // 获取资源的路径
            } else {
                throw new RuntimeException("找不到资源路径: " + resourcePath);
            }
        }

        return fullPath;
    }


    // 写个方法测试下，测试2种读取方式
    public static void main(String[] args) {
        String packageName = "com.hy.yqdoc.util.CommentReader";
        String methodName = "readMethodComment";
        String fieldName = "packageName";

        // 读取方法注释
        Map<String, Object> methodComment = readMethodComment(packageName, methodName);
        System.out.println("方法注释：" + methodComment);

        // 读取字段注释
        String fieldComment = readFieldComment(packageName, fieldName);
        System.out.println("字段注释：" + fieldComment);
    }

}
