package com.interview.jiaxin.v1.interval.rest;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.interview.jiaxin.v1.interval.ProcessingMonitor;
import com.interview.jiaxin.v1.interval.pojo.MetricData;
import com.interview.jiaxin.v1.interval.response.MeterIntervalData;
import com.interview.jiaxin.v1.interval.service.MeterProcessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.*;

@RestController
@RequestMapping("/v1")
public class MeterResource {
	@Resource
	private MeterProcessService meterProcessService;

	@GetMapping("/hello")
	public String greeting() {
		return "Hello world!";
	}

	/**
	 * Providing metric information for UI component
	 *
	 * @return metric information
	 */
	@GetMapping("/metric")
	public @ResponseBody MetricData metric() {
		return new MetricData(ProcessingMonitor.getProcessedNum(), ProcessingMonitor.getSubmittedNum());
	}

	/**
	 * Renamed thread factory for inspecting runtime information
	 */
	private static ThreadFactory builder = new ThreadFactoryBuilder()
			.setNameFormat("Meter Task Executor #%d").build();

	/**
	 * Thread pool for running meter process task
	 */
	private ExecutorService meterTaskExecutors = new ThreadPoolExecutor(5,
			20,
			100,
			TimeUnit.MILLISECONDS,
			new ArrayBlockingQueue<>(10),
			builder,
			new ThreadPoolExecutor.CallerRunsPolicy());



	@GetMapping("/trigger")
	public ResponseEntity<String> execute() {

		for (MeterIntervalData data : meterProcessService.getAllAvailableMeters()) {
			meterTaskExecutors.submit(new MaxMeterValueTask(data));
		}

		// Async return HTTP request, processing background
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Async processing for each meter
	 * Implementing Callable to make this task can be waiting for executing
	 */
	private class MaxMeterValueTask implements Callable<Void> {
		private MeterIntervalData data;

		MaxMeterValueTask(MeterIntervalData data) {
			this.data = data;
		}

		@Override
		public Void call() throws Exception {
			meterProcessService.maximumValue(data);
			return null;
		}
	}
}
