package com.infa.HL7_vds_source;

import java.nio.ByteBuffer;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.infa.vds.custom.sources.WS_API_source;
import com.informatica.vds.api.VDSConfiguration;
import com.informatica.vds.api.VDSEvent;
import com.informatica.vds.api.VDSEventList;
import com.informatica.vds.api.VDSException;

/**
 * Unit test for simple source.
 *
 */
@RunWith(JMock.class)
public class SampleSourceTest
{
	private final static String data = "ababb ajjajajk ajkjjkjka jkkjkjka \n";
    private static final int EVENTSIZE = data.getBytes().length;

    private WS_API_source source;
    private String sampleTextFiledValue = "sampleTextFiledValue";
    private String sampleStatisticConfig = "{\"statistic\":[{\"id\":1,\"displayName\":\"Plugin Stat 1\",\"type\":\"CUMULATIVE\"},{\"id\":2,\"displayName\":\"Plugin Stat 2\",\"type\":\"CUMULATIVE\"}]}";
    
	VDSConfiguration vdsConfiguration;
	Mockery context = new Mockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};
	
	@Before
	public void setUp() throws Exception{
		source = new WS_API_source();
		vdsConfiguration = context.mock(VDSConfiguration.class);
		context.checking(new Expectations() {
            {
                allowing(vdsConfiguration).getString("sample-text-field");
                will(returnValue(sampleTextFiledValue));
                allowing(vdsConfiguration).getInt("eventSize");
                will(returnValue(EVENTSIZE));
                allowing(vdsConfiguration).getString("statistic");
                will(returnValue(sampleStatisticConfig));
            }
        });
	}

	@Test
	public void testOpen() throws Exception{
		//perform unit testing on open() method
		//simulate open()
        source.open(vdsConfiguration);
		context.assertIsSatisfied();
	}

	@Test
	public void testRead() throws VDSException, InterruptedException, Exception{
		// perform unit testing on read() method
		final VDSEventList readEvents = context.mock(VDSEventList.class);
        final VDSEvent vdsEvent = context.mock(VDSEvent.class);
        final ByteBuffer buf = ByteBuffer.allocate(EVENTSIZE);
        //simulate read() 
        source.read(readEvents);
        context.checking(new Expectations() {
            {
                allowing(readEvents).createEvent(EVENTSIZE);
                will(returnValue(vdsEvent));
                allowing(vdsEvent).getBuffer();
                will(returnValue(buf));
                allowing(vdsEvent).getMaxEventSize();
                will(returnValue(EVENTSIZE));
                allowing(vdsEvent).setBufferLen(EVENTSIZE);
                allowing(vdsEvent).getBufferLen();
                will(returnValue(EVENTSIZE));
                allowing(vdsEvent).setBufferLen(-1);
                allowing(vdsEvent).associateRequestObject(with(any(Object.class)));
            }
        });
		context.assertIsSatisfied();
	}

	@Test
	public void testClose() throws Exception {
		//perform unit testing on close() method
		//simulate close()
		source.close();
		context.assertIsSatisfied();
	}

    @Test
    public void testGetStatistics() throws Exception {
        // perform unit testing on getStatistics(short[] statKeys) method
        short[] statisticsKeys = new short[] { 1, 2 };
        source.open(vdsConfiguration);
        source.getStatistics(statisticsKeys);
        context.assertIsSatisfied();
    }
}
