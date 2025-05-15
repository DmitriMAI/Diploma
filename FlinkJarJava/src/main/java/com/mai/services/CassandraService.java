package com.mai.services;

import com.datastax.driver.mapping.Mapper;
import com.mai.FlinkStreamingWithKafkaAndCassandra;
import model.Car;
import model.CarTable;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.connectors.cassandra.CassandraSink;
import org.apache.flink.util.Collector;
import org.apache.log4j.Logger;
import org.apache.flink.metrics.Counter;

import java.util.ArrayList;
import java.util.List;

import static org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpHeaders.setHost;
public final class CassandraService {

    private static final Logger LOGGER = Logger.getLogger(FlinkStreamingWithKafkaAndCassandra.class);
    public final void sinkToCassandraDB(final DataStream<Car> sinkCarStream, boolean enableLocalRun) throws Exception {


        LOGGER.info("Creating car data to sink into cassandraDB.");

        DataStream<CarTable> result = sinkCarStream.flatMap(new FlatMapFunction<Car, CarTable>() {
            @Override
            public void flatMap(Car car, Collector<CarTable> collector) throws Exception {
                collector.collect(new CarTable(car.getName(), car.getHorsepower(), car.getCylinders()));
            }
        });

        LOGGER.info("Open Cassandra connection and Sinking car data into cassandraDB.");
        CassandraSink.CassandraSinkBuilder<CarTable> carTableCassandraSinkBuilder = CassandraSink.addSink(result);
        if (enableLocalRun) {
            carTableCassandraSinkBuilder.setHost("localhost");
        } else {
            carTableCassandraSinkBuilder.setHost("cassandra-container");
        }
        carTableCassandraSinkBuilder.setMapperOptions(() -> new Mapper.Option[]{Mapper.Option.saveNullFields(false)})
                        .build();
    }

    public final void sinkCreate(final DataStream<Car> sinkCarStream) throws Exception {

        LOGGER.info("Creating car data to sink into cassandraDB.");
        SingleOutputStreamOperator<Tuple3<Long, Long, String>> sinkCarDataStream = sinkCarStream.map((MapFunction<Car, Tuple3<Long, Long, String>>) car ->
                        new Tuple3<>(car.getCylinders(), car.getHorsepower(), car.getName()))
                .returns(new TupleTypeInfo<>(TypeInformation.of(Long.class), TypeInformation.of(Long.class), TypeInformation.of(String.class)));


        LOGGER.info("Open Cassandra connection and Sinking car data into cassandraDB.");
        CassandraSink.addSink(sinkCarDataStream)
                .setHost("localhost")
                .setQuery("UPDATE example.car SET cylinders = ?, horsepower = ? WHERE name=?")
                .setMapperOptions(() -> new Mapper.Option[]{Mapper.Option.saveNullFields(true)})
                .build();
    }
}
