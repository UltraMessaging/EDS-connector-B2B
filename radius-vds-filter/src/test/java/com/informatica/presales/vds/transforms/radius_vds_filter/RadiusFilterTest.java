package com.informatica.presales.vds.transforms.radius_vds_filter;


import java.nio.ByteBuffer;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.informatica.vds.api.VDSConfiguration;
import com.informatica.vds.api.VDSEvent;
import com.informatica.vds.api.VDSEventList;
import com.informatica.vds.api.VDSException;
import com.informatica.vds.custom.transforms.radius_vds_filter.RadiusFilter;

/**
 * Unit test for sample transform.
 */
@RunWith(JMock.class)
public class RadiusFilterTest 
{

	private static final int EVENTSIZE = 8192;
	
	RadiusFilter transform;
	VDSConfiguration vdsConfiguration;
	VDSEventList vdsEventList;
	VDSEvent vdsEvent;
    private String sampleTextFiledValue = "sampleTextFiledValue";
	private String sampleStatisticConfig = "{\"statistic\":[{\"id\":1,\"displayName\":\"Plugin Stat 1\",\"type\":\"CUMULATIVE\"},{\"id\":2,\"displayName\":\"Plugin Stat 2\",\"type\":\"CUMULATIVE\"}]}";
	
	Mockery context = new Mockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};
	
	@Before
	public void setUp() throws Exception{

        // transform = new RadiusFilter();
        // vdsConfiguration = context.mock(VDSConfiguration.class);

        vdsEventList = context.mock(VDSEventList.class);
        vdsEvent = context.mock(VDSEvent.class);

        transform = new RadiusFilter();
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
		// perform unit testing on open() method
		transform.open(vdsConfiguration);
		context.assertIsSatisfied();
	}

	@Test
	public void testApply() throws VDSException, InterruptedException, Exception{
		// perform unit testing on read() method
        final ByteBuffer buffer = ByteBuffer.allocate(EVENTSIZE);
        buffer.clear();

        final byte[] data = new byte[EVENTSIZE];
        buffer.get(data, 0, data.length);

        buffer.flip();

        context.checking(new Expectations() {
            {
                allowing(vdsConfiguration).getString("sample-text-field");
                will(returnValue(sampleTextFiledValue));
                oneOf(vdsEvent).getBufferLen();
                will(returnValue(buffer.capacity()));
                oneOf(vdsEvent).getBuffer();
                will(returnValue(buffer));
                oneOf(vdsEventList).addEvent(with(any(byte[].class)), with(any(int.class)));
            }
        });

        transform.open(vdsConfiguration);
        transform.apply(vdsEvent, vdsEventList);
	
		context.assertIsSatisfied();
	}

	@Test
	public void testClose() throws Exception {
		// perform unit testing on close() method
		transform.close();
		context.assertIsSatisfied();
	}

    @Test
    public void testGetStatistics() throws Exception {
        // perform unit testing on getStatistics(short[] statKeys) method
        short[] statisticsKeys = new short[] { 1, 2 };
        transform.open(vdsConfiguration);
        transform.getStatistics(statisticsKeys);
        context.assertIsSatisfied();
    }
}
