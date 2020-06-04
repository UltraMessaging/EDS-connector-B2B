package com.informatica.presales.vds.sources.kafkax;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import com.informatica.vds.api.*;
import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;

//import kafka.consumer.ConsumerIterator;
//import kafka.consumer.KafkaStream;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;

public class KafkaXSource implements VDSSource {

    private static final Logger logger = LoggerFactory.getLogger(KafkaXSource.class);

    private  Properties props = new Properties();
    private  KafkaConsumer<byte[], byte[]> consumer;

    // UI json costants
    public static final String KAFKA_DESTINATION = "kafkaDestination";
    public static final String TOPIC = "topic";
    public static final String ZKSERVER = "zkServer";
    public static final String GROUPID = "groupId";
    public static final String AUTOCOMMIT = "autoCommit";
    public static final String COMMITINTERVAL = "commitInterval";
    public static final String SESSIONTIMEOUT = "sessionTimeout";
    public static final String KEYDESERIALIZER = "keyDeserializer";
    public static final String VALUEDESERIALIZER = "valueDeserializer";

    /**
     * determines whether to retry in case of open and write failure
     */
    protected IPluginRetryPolicy pluginRetryPolicyHandler;

    private  String kafkaDestination;
    private  String topic;
    private  String groupId;
    private  String autoCommit = "true";
    private  String commitInterval = "1000";
    private  String sessionTimeout = "30000";
    private  String keyDeserializer = "key.deserializer";
    private  String valueDeserializer = "value.deserializer";

    private byte[] message;
    private boolean debugEnabled = false;

    //@Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void open(VDSConfiguration vdsc) throws Exception {
        if (logger.isDebugEnabled()) {
            debugEnabled = true;
        }
        if (debugEnabled) {
            logger.debug("open: Will call initConfig");
        }
        initConfig(vdsc);
        if (debugEnabled) {
            logger.debug("open: Will call KafkaConsumer with props=" + props);
        }

        consumer = new KafkaConsumer(props);
        consumer.subscribe(Arrays.asList(topic));

        if (debugEnabled) {
            logger.debug("open: finished");
        }

    }

    void initConfig(VDSConfiguration vdsc) throws VDSException, Exception {
        // get the configurations
        try {
            kafkaDestination = vdsc.getString(KAFKA_DESTINATION).trim();
            if (debugEnabled) {
                logger.debug("initConfig: kafkaDestination='" + kafkaDestination + "'");
            }

            topic = vdsc.getString(TOPIC).trim();
            if (debugEnabled) {
                logger.debug("initConfig: topic='" + topic + "'");
            }
            groupId = vdsc.getString(GROUPID).trim();
            if (debugEnabled) {
                logger.debug("initConfig: groupId='" + groupId + "'");
            }
            autoCommit = vdsc.getString(AUTOCOMMIT).trim();
            if (debugEnabled) {
                logger.debug("initConfig: autoCommit='" + autoCommit + "'");
            }
            commitInterval = vdsc.getString(COMMITINTERVAL).trim();
            if (debugEnabled) {
                logger.debug("initConfig: commitInterval='" + commitInterval + "'");
            }
            sessionTimeout = vdsc.getString(SESSIONTIMEOUT).trim();
            if (debugEnabled) {
                logger.debug("initConfig: sessionTimeout='" + sessionTimeout + "'");
            }
            keyDeserializer = vdsc.getString(KEYDESERIALIZER).trim();
            if (debugEnabled) {
                logger.debug("initConfig: keyDeserializer='" + keyDeserializer + "'");
            }
            valueDeserializer = vdsc.getString(VALUEDESERIALIZER).trim();
            if (debugEnabled) {
                logger.debug("initConfig: valueDeserializer='" + valueDeserializer + "'");
            }

        } catch (Exception e) {
            throw new Exception("Must provide valid Kafka Desitnation: "
                    + kafkaDestination);
        }
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("metadata.broker.list", kafkaDestination);

        props.put("bootstrap.servers", kafkaDestination);
        props.put("group.id", groupId);
        props.put("enable.auto.commit", autoCommit);
        props.put("auto.commit.interval.ms", commitInterval);
        props.put("session.timeout.ms", sessionTimeout);
        props.put("key.deserializer", keyDeserializer);
        props.put("value.deserializer", valueDeserializer);
    }

    //@Override
    public void read(VDSEventList outbound) throws Exception {
//        logger.info("read:Reading events from topic: " + topic);
        ConsumerRecords<byte[], byte[]> records = consumer.poll(100);
        for (ConsumerRecord<byte[], byte[]> record : records) {
            message = record.value();
            if (debugEnabled) {
                logger.debug("read:" + String.format("read: message='%s' (offset=%d, key='%s')", 
                		Arrays.toString(message), record.offset(), Arrays.toString(record.key())));
            }
            outbound.addEvent(message, message.length);
        }
    }

    //@Override
    public void setRetryPolicyHandler(IPluginRetryPolicy iPluginRetryPolicyHandler) {
        this.pluginRetryPolicyHandler = iPluginRetryPolicyHandler;
        this.pluginRetryPolicyHandler.setLogger(logger);
    }

    //@Override
    public void close() throws IOException {
        consumer.close();
    }

}
