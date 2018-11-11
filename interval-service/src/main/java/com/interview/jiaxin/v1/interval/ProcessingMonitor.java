package com.interview.jiaxin.v1.interval;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Jiaxin Z.
 */
public final class ProcessingMonitor {
	private static AtomicLong processedMeters = new AtomicLong(0L);
	private static AtomicLong submittedMeterMaximum = new AtomicLong(0L);

	private ProcessingMonitor() {}


	public static long increaseAndGetProcessedNum() {
		return processedMeters.incrementAndGet();
	}

	public static long increaseAndGetSubmittedNum() {
		return submittedMeterMaximum.incrementAndGet();
	}

	public static long getProcessedNum() {
		return processedMeters.get();
	}

	public static long getSubmittedNum() {
		return submittedMeterMaximum.get();
	}
}
