package com.knoldus;


import model.Car;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;

class FlinkMetricsExposingMapFunction extends RichMapFunction<Car, Car> {
    private static final long serialVersionUID = 1L;

    private transient Counter eventCounter;

    @Override
    public void open(Configuration parameters) {
        eventCounter = getRuntimeContext().getMetricGroup().counter("events");
    }

    @Override
    public Car map(Car value) {
        eventCounter.inc();
        return value;
    }
}
