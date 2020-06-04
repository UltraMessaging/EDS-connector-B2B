package com.informatica.vds.custom.transforms;

import com.informatica.vds.api.VDSConfiguration;
import com.informatica.vds.api.VDSEvent;
import com.informatica.vds.api.VDSEventList;
import com.informatica.vds.api.VDSTransform;
import com.itemfield.contentmaster.AdditionalInput;
import com.itemfield.contentmaster.CMException;
import com.itemfield.contentmaster.InputBuffer;
//import com.itemfield.contentmaster.InputFile;
import com.itemfield.contentmaster.MultipleInput;
import com.itemfield.contentmaster.OutputBuffer;
//import com.itemfield.contentmaster.OutputFile;
import com.itemfield.contentmaster.ParserEngineSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lpolycar
 */
public class B2BDT_VDSTransform implements VDSTransform {

//    String ServiceDBName = "LSE_leg3_CSV_FPML";
//        InputBuffer tmpInputBuff = new InputBuffer("124164,CWTR,GB00B7CY1H21,3/23/2012,N,1000,1,1,ML,12NJ,B7CY1H2,USD,N,Y,18,BB,BARCLAYS BANK PLC,,,BARCLAYS 22,70,100,GB,UIDW,3/23/2012 0:00,15:03.4,,");
//        OutputBuffer tmpOutputBuf = new OutputBuffer();
    private String _serviceDBName;
    private String _outputRecordDelimiter;
    private String _outputFieldDelimiter;
    private InputBuffer _tmpInputBuf;
    private OutputBuffer _tmpOutputBuf;
    private String _outputBuffer;
    private ParserEngineSession _parserSession;
    private Map<String, String> hashMap = new HashMap<String, String>();
    //output delimeter
    private static final String FIELD_NAME_SERVICEDBNAME = "ServiceDBName";
    private static final String FIELD_NAME_OUTPUTRECORDDELIMITER = "outputRecordDelimiter";
    private static final String FIELD_NAME_OUTPUTFIELDDELIMITER = "outputFieldDelimiter";

    private static final Logger _logger = LoggerFactory.getLogger(B2BDT_VDSTransform.class);

    //@Override
    public void open(VDSConfiguration vdsc) throws Exception {

        if (vdsc.contains(FIELD_NAME_SERVICEDBNAME)) {
            _serviceDBName = vdsc.getString(FIELD_NAME_SERVICEDBNAME).trim();
        } else {
            throw new IllegalStateException("No ServiceDBName specified");
        }
        _logger.info("B2B DT ServiceDBName: " + _serviceDBName);

        if (vdsc.contains(FIELD_NAME_OUTPUTRECORDDELIMITER)) {
            _outputRecordDelimiter = vdsc.getString(FIELD_NAME_OUTPUTRECORDDELIMITER).trim();
            _logger.info("B2B DT Record Delimiter before: " + _outputRecordDelimiter);
            if (_outputRecordDelimiter.equals("LF")) {
                _outputRecordDelimiter = "\n";

            } else if (_outputRecordDelimiter.equals("CRLF")) {
                _outputRecordDelimiter = "\r\n";
            }
        } else {
            throw new IllegalStateException("No output record Delimiter specified");
        }
        _logger.info("B2B DT Record Delimiter after: '" + _outputRecordDelimiter + "'");

        if (vdsc.contains(FIELD_NAME_OUTPUTFIELDDELIMITER)) {
            _outputFieldDelimiter = vdsc.getString(FIELD_NAME_OUTPUTFIELDDELIMITER).trim();
            _logger.info("B2B DT Field Delimiter before: " + _outputFieldDelimiter);
            if (_outputFieldDelimiter.equals("TAB")) {
                _outputFieldDelimiter = "\t";
            }

        } else {
            throw new IllegalStateException("No output field Delimiter specified");
        }
        _logger.info("B2B DT Field Delimiter after: '" + _outputFieldDelimiter + "'");

    }

    //@Override

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void apply(VDSEvent inbound, VDSEventList outbound) throws Exception {

        _logger.info("B2B DT apply");

        byte[] data = new byte[inbound.getBufferLen()];

        _logger.info("B2B DT got data" + Arrays.toString(data));

        inbound.getBuffer().get(data);
        _tmpInputBuf = new InputBuffer(data);
        _tmpOutputBuf = new OutputBuffer();

        AdditionalInput[] inputPorts = new AdditionalInput[2];
        inputPorts[0] = new AdditionalInput("input_OutputRecordDelimiter",
                new InputBuffer(_outputRecordDelimiter));
        inputPorts[1] = new AdditionalInput("input_OutputFieldDelimiter",
                new InputBuffer(_outputFieldDelimiter));

        String OK_STATUS = "SUCCEEDED";
        String KO_STATUS = "FAILED";

        _outputBuffer = "";
        @SuppressWarnings("unused")
		String Status_Code_p = "";
        @SuppressWarnings("unused")
		String Status_Message_p = "";

        try {

            // DT Session creation
            _parserSession = new ParserEngineSession(_serviceDBName, new MultipleInput(_tmpInputBuf, inputPorts), _tmpOutputBuf);

            _logger.info("B2B DT created ParserEngine for service: " + _serviceDBName);

            // Parsing Execution
            _parserSession.exec();

            _logger.info("B2B DT executed ParserEngine for service: " + _serviceDBName);

            // Return Code
            _outputBuffer = _tmpOutputBuf.toString();

            _logger.info("B2B DT got output: " + _outputBuffer);

            Status_Code_p = OK_STATUS;
            Status_Message_p = null;

            //        inbound.getEventInfo(); // expose certain props here
            //        outbound.addEvent(bytes, i, new HashMap());
            hashMap = new HashMap();
            hashMap.put("serviceName", _serviceDBName);
            // Output OUT buffer
//            System.out.println("OutputBuffer_p=" + _outputBuffer);
            outbound.addEvent(_outputBuffer.getBytes(), _outputBuffer.getBytes().length, hashMap);

        } catch (Throwable e) {
            final String msgError;
            if (e instanceof CMException) {
                msgError = ((CMException) e).getDescription();
            } else {
                msgError = e.getLocalizedMessage();
            }
            // Output Error Message
            _logger.error("B2B DT message error: " + msgError, e);
            // Write error in System.err
            e.printStackTrace();
            // Return Management.
            _outputBuffer = "";
            Status_Code_p = KO_STATUS;
            Status_Message_p = msgError;
        }
    }

    //@Override
    public void close() throws IOException {

        _parserSession = null;
    }
}
