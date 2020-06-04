package com.informatica.messaging.vds.sources.camel.common;

/**
 * Common constants used by Camel plugins.
 * @author hpurohit
 *
 */
public interface VDSCamelConstants {

    // vdsplugin.xml plugin fields properties

    /*** Filter ***/
    public static final String FILTER_TYPE = "filterType";

    // Filter types
    public static final String FILTER_REGEX = "FILTER_REGEX";
    public static final String FILTER_XPATH = "FILTER_XPATH";
    public static final String FILTER_ENDS_WITH = "FILTER_ENDS_WITH";
    public static final String FILTER_STARTS_WITH = "FILTER_STARTS_WITH";
    public static final String FILTER_CONTAINS = "FILTER_CONTAINS";

    public static final String FILTER_REGEX_KEY = "filterRegex";
    public static final String FILTER_XPATH_EXPR_KEY = "filterXpathExpr";
    public static final String FILTER_ENDS_WITH_KEY = "filterEndsWith";
    public static final String FILTER_STARTS_WITH_KEY = "filterStartsWith";
    public static final String FILTER_CONTAINS_KEY = "filterContains";

    /*** Split ***/
    public static final String SPLIT_TYPE = "splitType";

    // Split Types
    public static final String SPLIT_TOKEN = "SPLIT_TOKEN";
    public static final String SPLIT_XPATH = "SPLIT_XPATH";
    public static final String SPLIT_XML_TAG = "SPLIT_XML_TAG";

    public static final String SPLIT_TOKEN_KEY = "splitToken";
    public static final String SPLIT_XPATH_EXPR = "splitXpathExpr";
    public static final String SPLIT_XML_TAG_KEY = "splitXmlTag";

    /*** Transform ***/
    // Transform Type
    public static final String TRANSFORM_PREPEND = "transformPrepend";
    public static final String TRANSFORM_APPEND = "transformAppend";

    // Default namespace for xpath expressions
    public static final String USE_DEFAULT_NAMESPACE = "useDefaultNameSpace";
    public static final String DEFAULT_NAMESPACE = "defaultNameSpace";
    public static final String NAMESPACE_PREFIX = "nameSpacePrefix";

    // Transform fields
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String HOSTIP = "HOSTIP";
    public static final String HOSTNAME = "HOSTNAME";
    public static final String LF = "LF";
    public static final String CRLF = "CRLF";
    public static final String NOTHING = "NOTHING";
    public static final String CUSTOM = "CUSTOM";

    public static final String TRANSFORM_PREPEND_CUSTOM = "transformPrependCustom";
    public static final String TRANSFORM_APPEND_CUSTOM = "transformAppendCustom";

    // Transform tokens
    public static final String TRANSFORM_TOKEN_LF = "#LF";
    public static final String TRANSFORM_TOKEN_CRLF = "#CRLF";
    public static final String TRANSFORM_TOKEN_TIMESTAMP = "#TIMESTAMP";
    public static final String TRANSFORM_TOKEN_HOSTIP = "#HOSTIP";
    public static final String TRANSFORM_TOKEN_HOSTNAME = "#HOSTNAME";

    // CRLF and LF byte value
    public static final byte LF_SEPARATOR = 0x0A;
    public static final byte CR_SEPARATOR = 0x0D;

    public static final String CHARSET_UTF_8 = "utf-8";

}
