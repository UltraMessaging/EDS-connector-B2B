package com.informatica.messaging.vds.sources.camel.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.informatica.vds.api.VDSConfiguration;

/**
 * This class reads VDS plugin configuration and populates values required to build a CamelRouteBuilder.
 * @author hpurohit
 *
 */
public class CamelRouteConfigDef implements VDSCamelConstants {

    // Camel from component URI
    private String camelFromURI;

    private String splitType;
    private String filterType;

    private String splitToken = "";
    private String splitXpathExpr = "";
    private String splitXmlTag = "";

    private String filterRegex = "";
    private String filterXpathExpr = "";
    private String filterContains = "";
    private String filterStartsWith = "";
    private String filterEndsWith = "";

    private String transformAppend = "";
    private String transformPrepend = "";

    private String transformAppendString = "";
    private String transformPrependString = "";

    private static final Logger _logger = LoggerFactory.getLogger(CamelRouteConfigDef.class);

    public CamelRouteConfigDef(String camelFromURI, VDSConfiguration vdsConfiguration) throws Exception {
        super();

        this.camelFromURI = camelFromURI;

        filterType = vdsConfiguration.getString(FILTER_TYPE);
        splitType = vdsConfiguration.getString(SPLIT_TYPE);


        // Filter
        if (filterType.equalsIgnoreCase(FILTER_REGEX)) {
            filterRegex = vdsConfiguration.getString(FILTER_REGEX_KEY);
        } else if (filterType.equalsIgnoreCase(FILTER_XPATH)) {
            filterXpathExpr = vdsConfiguration.getString(FILTER_XPATH_EXPR_KEY);
        } else if (filterType.equalsIgnoreCase(FILTER_CONTAINS)) {
            filterContains = vdsConfiguration.getString(FILTER_CONTAINS_KEY);
        } else if (filterType.equalsIgnoreCase(FILTER_STARTS_WITH)) {
            filterStartsWith = vdsConfiguration.getString(FILTER_STARTS_WITH_KEY);
        } else if (filterType.equalsIgnoreCase(FILTER_ENDS_WITH)) {
            filterEndsWith = vdsConfiguration.getString(FILTER_ENDS_WITH_KEY);
        }

        // Split
        if (splitType.equalsIgnoreCase(SPLIT_TOKEN)) {
            splitToken = vdsConfiguration.getString(SPLIT_TOKEN_KEY);
        } else if (splitType.equalsIgnoreCase(SPLIT_XPATH)) {
            splitXpathExpr = vdsConfiguration.getString(SPLIT_XPATH_EXPR);
        } else if (splitType.equalsIgnoreCase(SPLIT_XML_TAG)) {
            splitXmlTag = vdsConfiguration.getString(SPLIT_XML_TAG_KEY);
        }

        // Transform
        transformAppend = vdsConfiguration.getString(TRANSFORM_APPEND);
        transformPrepend = vdsConfiguration.getString(TRANSFORM_PREPEND);

        // Append
        if (transformAppend.equalsIgnoreCase(CUSTOM)) {
            transformAppendString = VDSCamelUtil
                    .getCustomTransformExpression(vdsConfiguration.getString(TRANSFORM_APPEND_CUSTOM));
        } else if (transformAppend.equalsIgnoreCase(NOTHING)) {
            transformAppendString = "";
        } else {
            transformAppendString = VDSCamelUtil.getTransformString(transformAppend);
        }

        // Prepend
        if (transformPrepend.equalsIgnoreCase(CUSTOM)) {
            transformPrependString = VDSCamelUtil
                    .getCustomTransformExpression(vdsConfiguration.getString(TRANSFORM_PREPEND_CUSTOM));
        } else if (transformPrepend.equalsIgnoreCase(NOTHING)) {
            transformPrependString = "";
        } else {
            transformPrependString = VDSCamelUtil.getTransformString(transformPrepend);
        }

        _logger.info("splitType: {} filterType: {} ", splitType, filterType);
        _logger.info("splitToken: {} splitXpathExpr: {} splitXmlTag: {}", splitToken, splitXpathExpr, splitXmlTag);
        _logger.info("filterRegex: {} filterXpathExpr: {} filterContains: {} filterStartsWith: {} filterEndsWith: {}",
                filterRegex, filterXpathExpr, filterContains, filterStartsWith, filterEndsWith);
        _logger.info("transformAppend: {} transformPrepend: {}", transformAppend, transformPrepend);
        _logger.info("transformAppendString: {} transformPrependString: {}", transformAppendString,
                transformPrependString);
    }

    public String getCamelFromURI() {
        return camelFromURI;
    }

    public void setCamelFromURI(String camelFromURI) {
        this.camelFromURI = camelFromURI;
    }


    public String getSplitType() {
        return splitType;
    }

    public void setSplitType(String splitType) {
        this.splitType = splitType;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public String getSplitToken() {
        return splitToken;
    }

    public void setSplitToken(String splitToken) {
        this.splitToken = splitToken;
    }

    public String getSplitXpathExpr() {
        return splitXpathExpr;
    }

    public void setSplitXpathExpr(String splitXpathExpr) {
        this.splitXpathExpr = splitXpathExpr;
    }

    public String getSplitXmlTag() {
        return splitXmlTag;
    }

    public void setSplitXmlTag(String splitXmlTag) {
        this.splitXmlTag = splitXmlTag;
    }

    public String getFilterRegex() {
        return filterRegex;
    }

    public void setFilterRegex(String filterRegex) {
        this.filterRegex = filterRegex;
    }

    public String getFilterXpathExpr() {
        return filterXpathExpr;
    }

    public void setFilterXpathExpr(String filterXpathExpr) {
        this.filterXpathExpr = filterXpathExpr;
    }

    public String getFilterContains() {
        return filterContains;
    }

    public void setFilterContains(String filterContains) {
        this.filterContains = filterContains;
    }

    public String getFilterStartsWith() {
        return filterStartsWith;
    }

    public void setFilterStartsWith(String filterStartsWith) {
        this.filterStartsWith = filterStartsWith;
    }

    public String getFilterEndsWith() {
        return filterEndsWith;
    }

    public void setFilterEndsWith(String filterEndsWith) {
        this.filterEndsWith = filterEndsWith;
    }

    public String getTransformAppend() {
        return transformAppend;
    }

    public void setTransformAppend(String transformAppend) {
        this.transformAppend = transformAppend;
    }

    public String getTransformPrepend() {
        return transformPrepend;
    }

    public void setTransformPrepend(String transformPrepend) {
        this.transformPrepend = transformPrepend;
    }

    public String getTransformAppendString() {
        return transformAppendString;
    }

    public void setTransformAppendString(String transformAppendString) {
        this.transformAppendString = transformAppendString;
    }

    public String getTransformPrependString() {
        return transformPrependString;
    }

    public void setTransformPrependString(String transformPrependString) {
        this.transformPrependString = transformPrependString;
    }

}
