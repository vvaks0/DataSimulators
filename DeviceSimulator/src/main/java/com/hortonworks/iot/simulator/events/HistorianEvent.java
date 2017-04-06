package com.hortonworks.iot.simulator.events;

import java.io.Serializable;

import com.hortonworks.iot.simulator.events.DeviceStatus;

public class HistorianEvent extends DeviceStatus implements Serializable {
	private static final long serialVersionUID = 1L;
	private long timestamp;
	private String function;
	private double value;
    
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getFunction() {
		return function;
	}
	public void setFunction(String function) {
		this.function = function;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
}