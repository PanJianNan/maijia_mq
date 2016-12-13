package test;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;

/**
 * AbstractControllerTest
 *
 * @author panjn
 * @date 2016/5/11
 */
@WebAppConfiguration(value = "mq-dubbo/src/main/webapp")
@ContextConfiguration(locations = {"classpath:spring/spring-*.xml"})
public class AbstractControllerTest {
    @Resource
    protected WebApplicationContext wac;

    protected MockMvc mockMvc;

}
