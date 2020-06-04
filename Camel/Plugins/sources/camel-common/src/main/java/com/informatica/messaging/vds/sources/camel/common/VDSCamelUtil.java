package com.informatica.messaging.vds.sources.camel.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class VDSCamelUtil implements VDSCamelConstants {

    /**
     * Returns string form of Transform expression i.e. LF, CRLF, HOSTNAME, HOSTIP, TIMESTAMP
     * 
     * @param transformAppendPrepend
     * @return
     * @throws UnknownHostException
     */
    public static String getTransformString(String transformAppendPrepend) throws UnknownHostException {

        StringBuffer transformStr = new StringBuffer();
        InetAddress inetAddress = InetAddress.getLocalHost();
        switch (transformAppendPrepend) {
            case CRLF:
                byte[] seqCrLf = null;
                seqCrLf = new byte[2];
                seqCrLf[0] = CR_SEPARATOR;
                seqCrLf[1] = LF_SEPARATOR;
                transformStr.append(new String(seqCrLf, StandardCharsets.UTF_8));
                break;
            case LF:
                byte[] seqLf = null;
                seqLf = new byte[1];
                seqLf[0] = LF_SEPARATOR;
                transformStr.append(new String(seqLf, StandardCharsets.UTF_8));
                break;
            case HOSTNAME:
                transformStr.append(inetAddress.getHostName());
                break;
            case HOSTIP:

                transformStr.append(inetAddress.getHostAddress());
                break;
            case TIMESTAMP:
                final Long currentTimeMillis = System.currentTimeMillis();
                transformStr.append(currentTimeMillis.toString());
                break;
            default:
                transformStr.append(transformAppendPrepend);
                break;
        }

        return transformStr.toString();
    }

    /**
     * Substitute values in custom transform expression 
     * 
     * @param customTransformExpr
     * @return
     * @throws UnknownHostException
     */
    public static String getCustomTransformExpression(String customTransformExpr) throws UnknownHostException {

        if (customTransformExpr.contains(TRANSFORM_TOKEN_HOSTNAME)) {
            customTransformExpr = customTransformExpr.replaceAll(TRANSFORM_TOKEN_HOSTNAME,
                    getTransformString(HOSTNAME));
        }

        if (customTransformExpr.contains(TRANSFORM_TOKEN_HOSTIP)) {
            customTransformExpr = customTransformExpr.replaceAll(TRANSFORM_TOKEN_HOSTIP, getTransformString(HOSTIP));
        }

        if (customTransformExpr.contains(TRANSFORM_TOKEN_TIMESTAMP)) {
            customTransformExpr = customTransformExpr.replaceAll(TRANSFORM_TOKEN_TIMESTAMP,
                    getTransformString(TIMESTAMP));
        }

        if (customTransformExpr.contains(TRANSFORM_TOKEN_CRLF)) {
            customTransformExpr = customTransformExpr.replaceAll(TRANSFORM_TOKEN_CRLF, getTransformString(CRLF));
        }

        if (customTransformExpr.contains(TRANSFORM_TOKEN_LF)) {
            customTransformExpr = customTransformExpr.replaceAll(TRANSFORM_TOKEN_LF, getTransformString(LF));
        }

        return customTransformExpr;
    }
}
