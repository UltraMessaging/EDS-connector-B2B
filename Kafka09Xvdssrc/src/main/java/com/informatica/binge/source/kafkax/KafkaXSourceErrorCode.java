package com.informatica.binge.source.kafkax;

import com.informatica.vds.api.internal.VDSErrorCodeDescription;

public class KafkaXSourceErrorCode extends VDSErrorCodeDescription {

    public KafkaXSourceErrorCode(short code, String msg) {
        super(code, msg);
    }

    // NOTE : Error codes 50900-50999 reserved for Kafka target plugin
    public static final KafkaXSourceErrorCode GENERIC_ERROR = new KafkaXSourceErrorCode((short) 50950, "Error: [%s]");

}
