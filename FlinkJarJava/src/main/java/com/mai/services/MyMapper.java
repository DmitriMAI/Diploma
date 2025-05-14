package com.knoldus.services;

import model.Car;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.metrics.DescriptiveStatisticsHistogram;

public class MyMapper extends RichMapFunction<Car, Car> {
    private transient DescriptiveStatisticsHistogram histogram;

    @Override
    public void open(Configuration config) {
        // Инициализация гистограммы с окном в 1000 элементов
        histogram = getRuntimeContext()
                .getMetricGroup()
                .histogram("operationDuration", new DescriptiveStatisticsHistogram(1000));
    }

    @Override
    public Car map(Car value) throws Exception {
        long startTime = value.getTime();

        long duration = System.nanoTime() - startTime;

        this.histogram.update(duration);
        value.setTime(null);
        return value;
    }
}