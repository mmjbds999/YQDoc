package com.hy.yqdoc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    public static Map readMethodComment(String packageName, String methodName) {
        StringBuffer temp = new StringBuffer();
        Map methodComment = new HashMap();
        Map parameterComments = new LinkedHashMap();
        boolean inMethod = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(getFilePath(packageName)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (!inMethod && line.startsWith("/**")) {
                    inMethod = true;
                    temp.append(line.replace("/**", ""));
                } else if (inMethod) {
                    if (line.startsWith("* @param")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 3) {
                            String paramName = parts[2];
                            String paramComment = parts.length==4?line.substring(line.indexOf(parts[3])):"";
                            parameterComments.put(paramName, paramComment);
                        }
                    } else {
                        temp.append(line.replace("* @return", "")
                                .replace("*/", "")
                                .replace("* ", ""));
                        if (line.endsWith("*/")) {
                            inMethod = false;
                            continue;
                        }
                    }
                }
                if (!inMethod && line.startsWith("public ")) {
                    if(containsMethod(line, methodName)){
                        methodComment.put("comment", temp.toString());
                        methodComment.put("params", parameterComments);
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
                if (!inComment) {
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
        String currentPath = System.getProperty("user.dir");
        String packagePath = packageName.replace(".", File.separator);
        String fullPath = currentPath +
                File.separator + "src" + File.separator + "main" + File.separator + "java" +
                File.separator + packagePath + ".java";
        return fullPath;
    }

}
