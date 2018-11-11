package com.interview.jiaxin.v1.interval.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MeterIntervalData {
	@JsonProperty("meter_id")
	private String meterId;

	@JsonProperty("earliest_dttm")
	private String earliestDttm;

	@JsonProperty("latest_dttm")
	private String latestDttm;

	public String getMeterId() {
		return meterId;
	}

	public void setMeterId(String meterId) {
		this.meterId = meterId;
	}

	public String getEarliestDttm() {
		return earliestDttm;
	}

	public void setEarliestDttm(String earliestDttm) {
		this.earliestDttm = earliestDttm;
	}

	public String getLatestDttm() {
		return latestDttm;
	}

	public void setLatestDttm(String latestDttm) {
		this.latestDttm = latestDttm;
	}
}
