package com.informatica.presales.vds.targets.kafka;

import com.informatica.vds.api.internal.VDSErrorCodeDescription;

public class KafkaTargetErrorCode extends VDSErrorCodeDescription {

    public KafkaTargetErrorCode(short code, String msg) {
        super(code, msg);
    }

    // NOTE : Error codes 50900-50999 reserved for Kafka target plugin
    public static final KafkaTargetErrorCode GENERIC_ERROR = new KafkaTargetErrorCode((short) 50900, "Error: [%s]");

}
