package org.xide.http.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HtmlUtil {
    public static String load(String path) throws Exception {
        try (InputStream in = HtmlUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) throw new RuntimeException("Template não encontrado: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    public static String render(String template, Map<String, String> params) {
        String result = template;
        for (var entry : params.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }


}
