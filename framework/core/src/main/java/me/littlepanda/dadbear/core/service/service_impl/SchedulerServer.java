package me.littlepanda.dadbear.core.service.service_impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import com.google.common.collect.Maps;
import me.littlepanda.dadbear.core.annotations.CronMethod;
import me.littlepanda.dadbear.core.util.ReflectionUtils;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class SchedulerServer extends AbstractServer {
	
	private static Log log = LogFactory.getLog(SchedulerServer.class);

	private Scheduler scheduler;
	private static final Map<String, Object> implemenets = Maps.newHashMap();
	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.service.Service#start()
	 */
	@Override
	public void startServer(final Object implement) {
		try {
			if(!CronExpression.isValidExpression(getSchedule())){
				throw new SchedulerException(getSchedule() + " is not a valid cron expression.");
			}
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			/*
			 * TODO 所有获取服务实现类的方法都需要改变，需要把配置文件注入进去，而不是现在的继承configurable接口
			 */
			final Job job = new Job() {
				@Override
				public void execute(JobExecutionContext context) throws JobExecutionException {
					Object obj = implemenets.get(context.getJobDetail().getKey().toString());
					List<Method> methods = ReflectionUtils.getDeclaredMethods(ReflectionUtils.getClass(getImplement()));
					for(Method method : methods){
						if(method.isAnnotationPresent(CronMethod.class)){
							try {
								method.setAccessible(true);
								method.invoke(obj);
								break;
							} catch (IllegalArgumentException e) {
								log.error("Illegal Argument Exception when invoke schedule service.", e);
								throw new RuntimeException("Illegal Argument Exception when invoke schedule service.", e);
							} catch (IllegalAccessException e) {
								log.error("can't access method when invoke scheduler service", e);
								throw new RuntimeException("can't access method when invoke scheduler service", e);
							} catch (InvocationTargetException e) {
								log.error("when invoke scheduler service, InvocationTargetException arise.", e);
								throw new RuntimeException("when invoke scheduler service, InvocationTargetException arise.", e);
							}
						}
					}
				}
			};
			JobFactory jobFactory = new JobFactory() {
				@Override
				public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler)
						throws SchedulerException {
					return job;
				}
			};
			
			String id = getProtocol() + ":" + UUID.randomUUID().toString();
			
			implemenets.put("SchedulerService." + id, implement);
			JobDetail jobDetail = JobBuilder.newJob(job.getClass()).
					withIdentity(id, "SchedulerService").build();
			Trigger trigger = TriggerBuilder.newTrigger().
					withIdentity(id, "SchedulerService").
					withSchedule(CronScheduleBuilder.cronSchedule(getSchedule())).
					build();
			scheduler.setJobFactory(jobFactory);
			scheduler.scheduleJob(jobDetail, trigger);
			scheduler.start();
		} catch (SchedulerException e) {
			log.error("error when create scheduler", e);
			throw new RuntimeException("error when create scheduler", e);
		}
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.service.Service#stop()
	 */
	@Override
	public void stopServer() {
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			log.error("error when shutdown scheduler", e);
			throw new RuntimeException("error when shutdown scheduler", e);
		}
	}
	
}
