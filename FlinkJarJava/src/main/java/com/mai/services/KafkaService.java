package com.knoldus.services;

import com.knoldus.FlinkStreamingWithKafkaAndCassandra;
import model.Car;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaSinkBuilder;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class KafkaService {

    private static final Logger LOGGER = Logger.getLogger(FlinkStreamingWithKafkaAndCassandra.class);
    public final DataStream<String> kafkaStreamConsumer(
            final StreamExecutionEnvironment environment,
            boolean enableLocalRun
    ) {

        LOGGER.info("Open Kafka connection and Streaming car data through topic.");
        Properties properties = new Properties();
        if (enableLocalRun) {
            properties.setProperty("bootstrap.servers", "localhost:29092");
        } else {
            properties.setProperty("bootstrap.servers", "kafka:9092");
        }
        properties.setProperty("group.id", "testKafka");

        return environment.addSource(new FlinkKafkaConsumer<>("car.create",
                        new SimpleStringSchema(), properties).setStartFromEarliest());

    }

    public final DataStream<String> kafkaStreamConsumer(
            final StreamExecutionEnvironment environment,
            final String topic,
            boolean enableLocalRun
    ) {
        LOGGER.info(String.format("Open kafka topic %s", topic));
        Properties properties = new Properties();

        if (enableLocalRun) {
            properties.setProperty("bootstrap.servers", "localhost:29092");
        } else {
            properties.setProperty("bootstrap.servers", "kafka:9092");
        }
        properties.setProperty("group.id", "testKafka2");
        return environment.addSource(
                new FlinkKafkaConsumer<>(
                        topic,
                        new SimpleStringSchema(),
                        properties
                ).setStartFromEarliest()
        );
    }

    public static class CarFieldData {
        public String name2;
        public List<String> nonNullFields = new ArrayList<>();
    }

    public DataStream<CarFieldData> transformToNotNull(DataStream<Car> cars){

        return cars.map(new MapFunction<Car, CarFieldData>() {
            @Override
            public CarFieldData map(Car car) throws Exception {
                CarFieldData fieldData = new CarFieldData();


                if (car.getName() != null) fieldData.name2 = car.getName();
                if (car.getMiles_per_Gallon() != null) fieldData.nonNullFields.add("Miles_per_Gallon");
                if (car.getCylinders() != null) fieldData.nonNullFields.add("Cylinders");
                if (car.getDisplacement() != null) fieldData.nonNullFields.add("Displacement");
                if (car.getHorsepower() != null) fieldData.nonNullFields.add("Horsepower");
                if (car.getWeight_in_lbs() != null) fieldData.nonNullFields.add("Weight_in_lbs");
                if (car.getAcceleration() != null) fieldData.nonNullFields.add("Acceleration");
                if (car.getYear() != null) fieldData.nonNullFields.add("Year");
                if (car.getOrigin() != null) fieldData.nonNullFields.add("Origin");
                return fieldData;
            }
        });


    }

    public DataStream<String> convertToString(DataStream<Car> carss){
        DataStream<CarFieldData> cars = transformToNotNull(carss);

        return cars.map(new MapFunction<CarFieldData, String>() {
            @Override
            public String map(CarFieldData carFieldData) throws Exception {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(carFieldData);
            }
        });
    }
    public final void kafkaStreamProducer(
            final DataStream<String> dataStream,
            final String topic,
            boolean enableLocalRun
    ) {
        LOGGER.info("Start kafka sender");

        KafkaRecordSerializationSchema<String> serializer = KafkaRecordSerializationSchema.builder()
                .setValueSerializationSchema(new SimpleStringSchema())
                .setTopic(topic)
                .build();

        KafkaSinkBuilder<String> builder = KafkaSink.<String>builder();

        if (enableLocalRun) {
            builder.setBootstrapServers("localhost:29092");
        } else {
            builder.setBootstrapServers("kafka:9092");
        }

        KafkaSink<String> sink = builder.setRecordSerializer(serializer)
                .build();

        LOGGER.info("Sink created");
        dataStream.sinkTo(sink);
    }
}
