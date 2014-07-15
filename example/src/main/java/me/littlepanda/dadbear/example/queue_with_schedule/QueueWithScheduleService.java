package me.littlepanda.dadbear.example.queue_with_schedule;

import org.apache.commons.logging.Log;

import me.littlepanda.dadbear.core.annotations.ServiceLog;
import me.littlepanda.dadbear.core.schedule_with_queue.QueueSchedulerService;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class QueueWithScheduleService implements QueueSchedulerService<QueueWithScheduleEntity> {
	
	@ServiceLog
	Log log;

	@Override
	public void run(QueueWithScheduleEntity t) throws RuntimeException {
		System.out.println("QueueWithScheduleService.run method.");
		System.out.println(t.getId());
		log.error("QueueWithScheduleService.run with " + t.getId());
//		throw new RuntimeException();
	}

	@Override
	public void exception() throws RuntimeException {
		System.out.println("QueueWithScheduleService.exception method.");	
	}
	
}
