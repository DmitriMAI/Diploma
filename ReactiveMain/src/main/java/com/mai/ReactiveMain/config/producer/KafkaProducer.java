package com.mai.ReactiveMain.config.producer;

import com.mai.ReactiveMain.ReactiveMainApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public static void main(String[] args) {
        Map<String, Object> producerConfig = new HashMap<>();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.BATCH_SIZE_CONFIG, 1_048_576); // 1MB batch
        producerConfig.put(ProducerConfig.LINGER_MS_CONFIG, 50); // 50ms linger
        producerConfig.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        producerConfig.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 134_217_728); // 128MB
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "1");
        producerConfig.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        SenderOptions<String, String> options = SenderOptions.create(producerConfig);

        int totalMessages = 1; // Сколько нагрузка
        int reportInterval = 10_000; // Макс интервал

        KafkaSender<String, String> sender = KafkaSender.create(options);

        Flux<SenderRecord<String, String, Integer>> flux = Flux.range(0, totalMessages)
                .parallel()
                .runOn(Schedulers.parallel())
                .map(i -> {
                    String value = "{\"Name\":\"buick skylark 320\", \"Cylinders\" : \"46\"}";
                    return SenderRecord.create(
                            new ProducerRecord<>("car.create", Integer.toString(i), value),
                            i
                    );
                })
                .sequential();

        sender.send(flux)
                .doOnNext(result -> {
                    if (result.correlationMetadata() % reportInterval == 0) {
                        log.info("Sent {} messages", result.correlationMetadata());
                    }
                })
                .doOnComplete(() -> {
                    log.info("All {} messages sent", totalMessages);
                    sender.close();
                })
                .doOnError(e -> log.error("Send error", e))
                .subscribe();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
