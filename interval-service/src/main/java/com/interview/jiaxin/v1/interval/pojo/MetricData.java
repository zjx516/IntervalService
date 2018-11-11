package com.interview.jiaxin.v1.interval.pojo;

public class MetricData {
	private long processed;
	private long submitted;

	public MetricData(long processed, long submitted) {
		this.processed = processed;
		this.submitted = submitted;
	}

	public long getProcessed() {
		return processed;
	}

	public void setProcessed(long processed) {
		this.processed = processed;
	}

	public long getSubmitted() {
		return submitted;
	}

	public void setSubmitted(long submitted) {
		this.submitted = submitted;
	}
}
