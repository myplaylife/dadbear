package me.littlepanda.dadbear.core.schedule_with_queue;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import me.littlepanda.dadbear.core.annotations.Config;
import me.littlepanda.dadbear.core.annotations.CronMethod;
import me.littlepanda.dadbear.core.annotations.InitMethod;
import me.littlepanda.dadbear.core.annotations.ServiceLog;
import me.littlepanda.dadbear.core.config.ConfigurationUtil;
import me.littlepanda.dadbear.core.service.ServiceConstants;
import me.littlepanda.dadbear.core.util.BytesUtil;
import me.littlepanda.dadbear.core.util.ReflectionUtils;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class CycleQueueProcessor implements QueueProcessor {
	
	@ServiceLog
	private Log log = LogFactory.getLog(CycleQueueProcessor.class);
	
	@Config
	private Configuration conf;
	
	private QueueSchedulerHelper queueSchedulerHelper;
	
	private QueueSchedulerService<Object> queueSchedulerService;
	
	private int max_retry;
	
	private String queue_name;
	
	private String queue_type;
	
	private String processor;
	
	private int thread_number;
	
	@InitMethod
	public void init() {
		max_retry = conf.getInt(QueueProcessor.KEY_MAX_RETRY);
		queue_name = conf.getString(QueueProcessor.KEY_QUEUE_NAME);
		queue_type = conf.getString(QueueProcessor.KEY_QUEUE_TYPE);
		processor = conf.getString(QueueProcessor.KEY_PROCESSOR);
		thread_number = conf.getInt(QueueProcessor.KEY_THREAD_NUMBER);
		
		queueSchedulerHelper = new QueueSchedulerHelper(queue_name, queue_type);
		queueSchedulerService = ReflectionUtils.newInstance(processor, conf);
		
		List<Field> fields = ReflectionUtils.getDeclaredFields(queueSchedulerService.getClass());
		for(Field field : fields){
			/*
			 * 注入相应服务的配置
			 */
			if(field.isAnnotationPresent(Config.class)){
				try {
					field.setAccessible(true);
					if(!field.getType().getName().equals(Configuration.class.getName())){
						log.error("Field with annotation Config must be " + Configuration.class.getName());
						throw new RuntimeException("Field with annotation Config must be " + Configuration.class.getName());
					}
					field.set(queueSchedulerService, conf);
				} catch (IllegalArgumentException e) {
					log.error("Illegal Argument Exception when set configuration object to service " + processor, e);
					throw new RuntimeException("Illegal Argument Exception when iset configuration object to service " + processor, e);
				} catch (IllegalAccessException e) {
					log.error("can't access field when set configuration object to service " + processor, e);
					throw new RuntimeException("can't access field when set configuration object to service " + processor, e);
				}
			}
			/*
			 * 日志句柄注入
			 */
			if(field.isAnnotationPresent(ServiceLog.class)){
				try {
					field.setAccessible(true);
					if(!field.getType().getName().equals(Log.class.getName())){
						log.error("Field with annotation Config must be " + Log.class.getName());
						throw new RuntimeException("Field with annotation Config must be " + Configuration.class.getName());
					}
					field.set(queueSchedulerService, me.littlepanda.dadbear.core.log.LogFactory.getLog(conf.getString(ServiceConstants.LOG_NAME)));
				} catch (IllegalArgumentException e) {
					log.error("Illegal Argument Exception when set LOG HANDLER to service " + processor, e);
					throw new RuntimeException("Illegal Argument Exception when set LOG HANDLER to service " + processor, e);
				} catch (IllegalAccessException e) {
					log.error("can't access field when set LOG HANDLER to service " + processor, e);
					throw new RuntimeException("can't access field when set LOG HANDLER to service " + processor, e);
				}
			}
		}
		/**
		 * 在这这里把用户编写服务类的日志句柄替换掉。
		 */
		Runnable r = new Runnable() {
			@Override
			public synchronized void run() {
				ZookeeperQueueEntityWrapper wrapper = null;;
				while(true) {
					try {
						wrapper = queueSchedulerHelper.poll();
						if(wrapper == null){
							if(queueSchedulerHelper.isNeedSleep()){
								Thread.sleep(5000);
							}
							continue;
						}
						if(!queueSchedulerHelper.pre_execute(wrapper)) {
							throw new RuntimeException("Error when move element to executing queue.");
						}
						/*
						 * 将元素类型转换为用户接口能够接受的类型 
						 */
						byte[] element_bytes = wrapper.getElement();
						Object o = BytesUtil.bytesToClass(element_bytes, ConfigurationUtil.getInitConfig(), ReflectionUtils.getClass(queue_type));
						
						queueSchedulerService.run(o);
						if(!queueSchedulerHelper.after_execute(wrapper)) {
							throw new RuntimeException("Error when delete element in executing queue after execute user method successful.");
						}
					} catch (RuntimeException re) {
						log.error(re.getMessage());
						try {
							queueSchedulerService.exception();
							queueSchedulerHelper.exception(wrapper, max_retry);
						} catch (Throwable t) {
							log.error(t.getMessage(), t);
						}
					} catch (Throwable t) {
						log.error(t.getMessage(), t);
					}
				}
				
			}
		};
		
		ExecutorService es = Executors.newFixedThreadPool(thread_number);
		for(int i=0; i<thread_number; i++){
			es.execute(r);
		}
	}

	@Override
	@CronMethod
	public void run() {
//		log.info("Cycle queue processor " + processor + " is alive!");
	}
	
}
