package com.mai;

import com.mai.services.CassandraService;
import com.mai.services.KafkaService;
import com.mai.services.MyMapper;
import model.Car;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.log4j.Logger;

import java.util.Objects;

public class FlinkStreamingWithKafkaAndCassandra {

    private static final Logger LOGGER = Logger.getLogger(FlinkStreamingWithKafkaAndCassandra.class);


    public static void main(String[] args) throws Exception {
        LOGGER.info("Started Flink Application.");

        LOGGER.info("Creating Flink Stream Execution Environment.");
        StreamExecutionEnvironment environment = StreamExecutionEnvironment
                .getExecutionEnvironment();

        LOGGER.info("Instantiating KafkaService Class and Consume message from kafka topic.");

        boolean enableLocalRun = false;

        KafkaService kafkaService = new KafkaService();
        DataStream<String> jsonDataStream = kafkaService.kafkaStreamConsumer(environment, enableLocalRun);
        LOGGER.info("first Kafka done");
        DataStream<String> jsonDebeziumStream = kafkaService.kafkaStreamConsumer(
                environment,
                "postgres.public.cars",
                enableLocalRun
        );
        LOGGER.info("Second kafka done");


        final DataStream<Car> carStream2 = jsonDebeziumStream.map(kafkaMessage -> {
            LOGGER.info("was here ");
            try {
                JsonNode jsonNode = new ObjectMapper().readValue(kafkaMessage, JsonNode.class);

                JsonNode payload = jsonNode.get("payload");
                // Доступ к before и after с проверкой на null
                JsonNode before = payload.get("before");
                JsonNode after = payload.get("after");
                LOGGER.info("SUCC");
                if (before == null) {
                    return Car.builder().build();
                }
                LOGGER.info("wereree succ");
                Long horsePower = null;
                if (after.has("horsepower") && before.has("horsepower") &&
                        !after.get("horsepower").equals(before.get("horsepower"))) {
                    horsePower = after.get("horsepower").asLong();
                }
                LOGGER.info("parse horse");
                Long cil = null;
                if (after.has("cylinders") && before.has("cylinders") &&
                        !after.get("cylinders").equals(before.get("cylinders"))) {
                    cil = after.get("cylinders").asLong();
                }
                LOGGER.info("parse cil");
                return Car.builder()
                        .time(System.nanoTime())
                        .Name(after.get("name").asText())
                        .Horsepower(horsePower)
                        .Cylinders(cil)
                        .build();
            } catch (Exception e) {
                LOGGER.error(String.format("Error is in devez parse", e.getMessage()), e);
                return null;
            }
        }).filter(Objects::nonNull).forward();

        LOGGER.info("Wait for cassandra");
        LOGGER.info(" Transforming Json Data from kafka into Car Pojo.");


        final DataStream<Car> carStream = jsonDataStream.map(kafkaMessage -> {
            LOGGER.info("was here ");
            try {
                JsonNode jsonNode = new ObjectMapper().readValue(kafkaMessage, JsonNode.class);
                return Car.builder()
                        .time(System.nanoTime())
                        .Name(jsonNode.get("Name").asText())
                        .Horsepower(jsonNode.has("Horsepower") && !jsonNode.get("Horsepower").isNull()
                                ? jsonNode.get("Horsepower").asLong()
                                : null)
                        //.Origin(jsonNode.get("Origin").asText())
                        //.Year(jsonNode.get("Year").asText())
                        //.Weight_in_lbs(jsonNode.get("Weight_in_lbs").asLong())
                        //.Miles_per_Gallon(jsonNode.get("Miles_per_Gallon").asDouble())
                        //.Displacement(jsonNode.get("Displacement").asDouble())
                        //.Cylinders(jsonNode.get("Cylinders").asLong())
                        .Cylinders(jsonNode.has("Cylinders") && !jsonNode.get("Cylinders").isNull()
                                ? jsonNode.get("Cylinders").asLong()
                                : null)
                        //.Acceleration(jsonNode.get("Displacement").asDouble()).build();
                        .build();
            } catch (Exception e) {
                LOGGER.error(String.format("Error is ", e.getMessage()), e);
                return null;
            }
        }).filter(Objects::nonNull).forward();

        DataStream<Car> d = carStream2.union(carStream);

        LOGGER.info("Instantiating CassandraService Class and sinking data into CassandraDB.");
        CassandraService cassandraService = new CassandraService();
        d.map(new MyMapper());
        cassandraService.sinkToCassandraDB(d, enableLocalRun);

        LOGGER.info("Sending processed data to Kafka topic.");

        DataStream<String> notNullFieldsStream = kafkaService.convertToString(d);
        kafkaService.kafkaStreamProducer(notNullFieldsStream, "car.update", enableLocalRun);

        environment.execute();

    }



}
