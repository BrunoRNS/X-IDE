package org.xide.http.controllers;

import io.javalin.http.Context;
import java.util.Map;
import org.xide.http.utils.HtmlUtil;

public class HelloController {
    public static void index(Context ctx) throws Exception {
        String template = HtmlUtil.load("public/hello.html");
        String html = HtmlUtil.render(template, Map.of());
        ctx.html(html);
    }
}
