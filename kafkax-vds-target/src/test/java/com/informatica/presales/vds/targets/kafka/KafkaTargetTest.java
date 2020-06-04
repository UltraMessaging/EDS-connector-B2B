package com.informatica.presales.vds.targets.kafka;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.informatica.presales.vds.targets.kafka.KafkaTarget;
import com.informatica.vds.api.VDSConfiguration;

public class KafkaTargetTest {

    private static final String TEST_TOPIC       = "test_topic";
    private static final String TEST_DESTINATION = "test_destination";
    private KafkaTarget   targetObj = new KafkaTarget();
    private final Mockery context   = new Mockery() {
                                        {
                                            setImposteriser(ClassImposteriser.INSTANCE);
                                        }
                                    };

    @Before
    public void setUp() throws Exception {
    }

    private VDSConfiguration initVdsConfiguration(final String destination, final String topic)
            throws Exception {
        final VDSConfiguration tgtConf = context.mock(VDSConfiguration.class);
        
        context.checking(new Expectations() {
            {
                allowing(tgtConf).contains(KafkaTarget.KAFKA_DESTINATION);
                will(returnValue(true));
                allowing(tgtConf).getString(KafkaTarget.KAFKA_DESTINATION);
                will(returnValue(destination));
                allowing(tgtConf).getString(KafkaTarget.TOPIC);
                will(returnValue(topic));
            }
        });
        return tgtConf;
    }

    @Test
    public void testOpenConfigInitDefaultValues() throws Exception {

        final VDSConfiguration cnf = initVdsConfiguration(TEST_DESTINATION, TEST_TOPIC);
        targetObj.initConfig(cnf);
        // assertions done here
        context.assertIsSatisfied();

    }
}
