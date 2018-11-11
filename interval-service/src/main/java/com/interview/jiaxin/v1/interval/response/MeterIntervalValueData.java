package com.interview.jiaxin.v1.interval.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MeterIntervalValueData {
	@JsonProperty("meter_id")
	private String meterId;
	private String dttm;
	private Double value;

	public String getMeterId() {
		return meterId;
	}

	public void setMeterId(String meterId) {
		this.meterId = meterId;
	}

	public String getDttm() {
		return dttm;
	}

	public void setDttm(String dttm) {
		this.dttm = dttm;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
}
