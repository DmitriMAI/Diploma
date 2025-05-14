package com.mai.ReactiveMain;

import com.mai.ReactiveMain.config.producer.KafkaProducer;
import kafka.tools.ConsoleConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

@SpringBootTest
@Testcontainers
class ReactiveMainApplicationTests {

	@Container
	static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0").asCompatibleSubstituteFor("apache/kafka"));

	@DynamicPropertySource
	public static void setup(DynamicPropertyRegistry dynamicPropertyRegistry){
		Startables.deepStart(kafkaContainer).join();

		dynamicPropertyRegistry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);


	}

	@Autowired
	private KafkaConsumer<String, String> reader;

	private Properties consumerProperties;

	@Autowired
	private Environment environment;

	@BeforeEach
	void setUp(){
		String kafkaServers = this.environment.getProperty("spring.kafka.bootstrap-servers");
		this.consumerProperties = new Properties();
		this.consumerProperties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
		this.consumerProperties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "1");
		this.consumerProperties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		this.consumerProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		this.consumerProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//		this.consumerProperties.setProperty(JsonDeer.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

	}
	@Autowired
	KafkaProducer producer;

	@Test
	void contextLoads() {

		this.reader = new KafkaConsumer<>(this.consumerProperties);

		reader.subscribe(Arrays.asList("dima"));

		//producer.sendMessage("Hi");
		System.out.println("Hello");

		ConsumerRecords<String, String> records = reader.poll(Duration.ofSeconds(5));
		//producer.sendMessage("Hi");
		System.out.println(records);
	}

}
