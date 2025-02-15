package com.yu.market.common.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.IOException;
import java.nio.file.Files;

public class ScriptUtil {

    /**
     * 从 ClassPath 加载 Lua 脚本内容
     *
     * @param scriptPath Lua 脚本文件路径
     * @return Lua 脚本内容
     */
    public static String loadLuaScript(String scriptPath) {
        Resource resource = new ClassPathResource(scriptPath);
        try {
            ResourceScriptSource scriptSource = new ResourceScriptSource(resource);
            return Files.readString(resource.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException("加载 Lua 脚本失败: " + scriptPath, e);
        }
    }
}