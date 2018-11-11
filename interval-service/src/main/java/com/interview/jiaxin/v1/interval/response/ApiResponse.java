package com.interview.jiaxin.v1.interval.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ApiResponse<T> {
	String code;
	String Message;

	@JsonProperty
	List<T> data;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}
}
