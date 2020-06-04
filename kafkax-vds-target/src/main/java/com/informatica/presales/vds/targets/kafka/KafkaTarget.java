package com.informatica.presales.vds.targets.kafka;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.informatica.vds.api.*;

public class KafkaTarget implements VDSTarget {
    private static final Logger              logger            = LoggerFactory.getLogger(KafkaTarget.class);

    private static Properties                props             = new Properties();
    private static Producer<Integer, String> producer;

    // UI json costants
    public static final String               KAFKA_DESTINATION = "kafkaDestination";
    public static final String               TOPIC             = "topic";
    public static final String MESSAGE_PROPERTY_KEY = "DESTINATION";
    /**
     * determines whether to retry in case of open and write failure
     */
    protected IPluginRetryPolicy             pluginRetryPolicyHandler;

    private static String                    kafkaDestination;
    private static String                    topic;

    @Override
    public void open(VDSConfiguration vdsc) throws Exception {
        initConfig(vdsc);
        producer = new Producer<Integer, String>(new ProducerConfig(props));
    }

    void initConfig(VDSConfiguration vdsc) throws VDSException, Exception {
        // get the configurations
        try {
            kafkaDestination = vdsc.getString(KAFKA_DESTINATION.trim());
            topic = vdsc.getString(TOPIC.trim());
        } catch (Exception e) {
            throw new VDSException(KafkaTargetErrorCode.GENERIC_ERROR, "Must provide valid Kafka Destination: "
                    + kafkaDestination);
        }
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("metadata.broker.list", kafkaDestination);
    }

    @Override
    public void write(VDSEvent inputEvent) throws Exception {
        ByteBuffer src = inputEvent.getBuffer();
        int srcLength = inputEvent.getBufferLen();
        byte[] srcByte = new byte[srcLength];

        if (srcLength == 0) {
            logger.debug("Received data of size {}, returning without writing to Kafka", srcLength);
            return;
        } else {
            src.get(srcByte);
            // write to Kafka here
            String messageStr = new String(srcByte);
            String destTopic = inputEvent.getProperty(MESSAGE_PROPERTY_KEY);
            if (destTopic == null) {
            	destTopic = topic;	// default topic
            	logger.info("Using DEFAULT destination topic {}", destTopic);
            } else {
            	logger.info("Found destination topic {}", destTopic);
            }

            try {
            	producer.send(new KeyedMessage<Integer, String>(destTopic, messageStr));
            } catch (Exception e) {
            	logger.error("Error writing to topic {}, sending to default topic {}", destTopic, topic, e);
            	producer.send(new KeyedMessage<Integer, String>(topic, messageStr));
            }
        }
    }

    @Override
    public void setRetryPolicyHandler(IPluginRetryPolicy iPluginRetryPolicyHandler) {
        this.pluginRetryPolicyHandler = iPluginRetryPolicyHandler;
        this.pluginRetryPolicyHandler.setLogger(logger);
    }

    @Override
    public void close() throws IOException {
        producer.close();
    }
}
