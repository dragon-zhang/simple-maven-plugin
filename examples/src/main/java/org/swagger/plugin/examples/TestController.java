package org.swagger.plugin.examples;

import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 类上的描述
 *
 * @author zhangzicheng
 * @version 1.0.0
 * @date 2021/03/01
 * @exception Exception
 * @throws Exception
 * @link Exception
 * @see Exception
 * @since 1.0.0
 */
@RestController
public class TestController implements BeanNameAware {

    /**
     * beanName
     */
    private String name;

    /**
     * 方法上的描述
     *
     * @param param 参数
     * @return 返回值
     */
    @GetMapping("/test")
    public String test(@RequestParam String param) {
        return name + " say hello, " + param;
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }
}
