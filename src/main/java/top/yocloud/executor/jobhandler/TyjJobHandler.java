package top.yocloud.executor.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO say something here...
 * <br/>
 *
 * @author 谭亚军 at 2023/6/8 19:01
 */
@Component
@Slf4j
public class TyjJobHandler {
    @XxlJob("tyjJobHandler")
    public void tyjJobHandler() throws Exception {
        XxlJobHelper.log("XXL-JOB, Hello World. by tyj: 【{}】即将睡眠5秒", Thread.currentThread().getName());
        String mainName = Thread.currentThread().getName();
        log.info("XXL-JOB, Hello World. by tyj: 【{}】即将睡眠5秒", mainName);
        ReentrantLock lock = new ReentrantLock();
        Thread t1 = new Thread(asyncTask(mainName, lock), "t1");

        Thread t2 = new Thread(asyncTask(mainName, lock), "t2");

        t1.start();
        t2.start();
        Thread.sleep(5000);
        XxlJobHelper.log("XXL-JOB, Hello World. by tyj: 【{}】睡眠结束", mainName);
        log.info("XXL-JOB, Hello World. by tyj: 【{}】睡眠结束", mainName);
    }

    private static Runnable asyncTask(String mainName, ReentrantLock lock) {
        return () -> {
            try {
                if (!lock.tryLock(2, TimeUnit.SECONDS)) {
                    XxlJobHelper.log("获取锁失败:" + Thread.currentThread().getName());
                    return;
                }
                Thread jobAsync = Thread.currentThread();
                if("t1".equals(Thread.currentThread().getName())){
                    XxlJobHelper.handleFail("失败了：" + jobAsync.getName());
                } else {
                    XxlJobHelper.handleSuccess("成功了：" + jobAsync.getName());
                }
                jobAsync.setName(String.format("%s:【%s】", jobAsync.getName(), mainName));
                log.info("异步触发: 设置线程名称={}", jobAsync.getName());
                XxlJobHelper.log("{}执行完毕, 准备解锁", Thread.currentThread().getName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("加锁失败被中断", e);
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        };
    }
}
