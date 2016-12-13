package redis;

import com.maijia.mq.cache.ICacheService;
import com.maijia.mq.cahce.redis.RedisCacheImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import test.AbstractControllerTest;

import javax.annotation.Resource;

/**
 * RedisCacheImplTest
 *
 * @author panjn
 * @date 2016/12/5
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class RedisCacheImplTest extends AbstractControllerTest {

//    @Mock （构建实例，所有实例方法都mock掉，不执行）
//    @InjectMocks （构建实例，实例方法会执行）
//    RedisCacheImpl redisCacheImpl;
    @Resource
    ICacheService cacheService;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        //初始化对象的注解
        //MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testSet() throws Exception {
        cacheService.set("maijia_mq:unit_test:set", new String("douni"), 60L);
    }

    @Test
    public void testGet() throws Exception {
        cacheService.set("maijia_mq:unit_test:set", new String("douni"), 60L);
        cacheService.get("maijia_mq:unit_test:set");
    }

    @Test
    public void testLPush() throws Exception {
    }

    @Test
    public void testBLPop() throws Exception {

    }

    @Test
    public void testBRPop() throws Exception {

    }
}