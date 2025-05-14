package com.mai.ReactiveMain;

import org.junit.Test;
import org.springframework.kafka.test.context.EmbeddedKafka;

@EmbeddedKafka(
        ports = 9092
)
public class KafkaTest {
    @Test
    public void embeddedKafkaDemo(){

    }
}
