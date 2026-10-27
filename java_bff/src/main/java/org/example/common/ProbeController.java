package org.example.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 受保护探针端点：用于验证登录拦截器（无 token → 401，带 token → 200）。
 * <p>不对外暴露业务数据，仅用于 P0 出口门禁与单测。</p>
 */
@RestController
public class ProbeController {

    @GetMapping("/api/probe")
    public Result<String> probe() {
        return Result.success("ok");
    }
}
