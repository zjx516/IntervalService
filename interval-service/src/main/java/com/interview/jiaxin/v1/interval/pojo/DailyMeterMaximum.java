package com.interview.jiaxin.v1.interval.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DailyMeterMaximum {
	@JsonProperty("meter_id")
	private int meter_id;
	private String day;
	@JsonProperty("maximum_value")
	private double maximum_value;

	public DailyMeterMaximum(String meter_id, String day, Double maximum) {
		this.meter_id = Integer.parseInt(meter_id);
		this.day = day;
		this.maximum_value = maximum;
	}

	public int getMeter_id() {
		return meter_id;
	}

	public void setMeter_id(String meter_id) {
		this.meter_id = Integer.parseInt(meter_id);
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public double getMaximum_value() {
		return maximum_value;
	}

	public void setMaximum_value(double maximum_value) {
		this.maximum_value = maximum_value;
	}
}
