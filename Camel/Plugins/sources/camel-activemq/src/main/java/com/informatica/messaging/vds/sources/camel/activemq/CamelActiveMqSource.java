package com.informatica.messaging.vds.sources.camel.activemq;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.builder.xml.XPathBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.main.Main;
import org.apache.camel.model.ExpressionNode;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.informatica.messaging.vds.sources.camel.common.CamelRouteConfigDef;
import com.informatica.messaging.vds.sources.camel.common.VDSCamelConstants;
import com.informatica.vds.api.*;

/**
 * Camel Component for Apache Active MQ
 * @author hpurohit
 *
 */
public class CamelActiveMqSource implements VDSSource, VDSCamelConstants {

    /*** 
     * Active MQ plugin properties 
     * 
     ***/
    private static final String ACTIVE_MQ_TOPIC_NAME = "activeMqTopicName";
    private static final String ACTIVE_MQ_QUEUE_NAME = "activeMqQueueName";
    private static final String TOPIC = "topic";
    private static final String QUEUE = "queue";
    private static final String DEFAULT_BROKER_URL = "failover://tcp://localhost:61616";
    private static final String ACTIVE_MQ_PASSWORD = "activeMqPassword";
    private static final String ACTIVE_MQ_USER_NAME = "activeMqUserName";
    private static final String ACTIVE_MQ_TYPE = "activeMqType";
    private static final String ACTIVE_MQ_BROKER_URL = "activeMqBrokerUrl";

    public boolean useDefaultNameSpace;
    public String defaultNameSpace;
    public String nameSpacePrefix;

    /**
     *  VDS Event size
     */
    public static final String EVENT_SIZE = "eventSize";

    /**
     * Blocking queue used by CamelRouteBuilder to put in coming messages.
     */
    public BlockingQueue<Object> queue = null;

    private int eventSize;

    /**
     * Camel context and main to run the components
     */
    private Main main = null;
    private CamelContext context;


    /**
     * Camel Route Configuration
     */
    public CamelRouteConfigDef camelRouteDefnConfig = null;

    private static final Logger _logger = LoggerFactory.getLogger(CamelActiveMqSource.class);
	
	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {

        _logger.info("Closing Camel Active MQ Source...");

        try {
            /**
             *  Stop the routes associated with the context
             */
            List<Route> routes = context.getRoutes();
            for (Route route : routes) {
                context.stopRoute(route.getId());
            }
            context.stop();
            main.stop();
        } catch (Exception e) {
            _logger.error("Exception while closing Camel Active MQ Source: {}", e.getMessage(), e);
        }
	}

	/* (non-Javadoc)
	 * @see com.informatica.vds.api.VDSSource#open(com.informatica.vds.api.VDSConfiguration)
	 */
	public void open(VDSConfiguration vdsConfiguration) throws Exception {

        /**
         *  Initialize Queue to hold Messages
         */
        queue = new LinkedBlockingQueue<>(100);

        /**
         *  Parse the VDS plugin configuration
         */
        parseConfig(vdsConfiguration);

        _logger.info("Opening Camel Active MQ Source for reading...");

        /**
         * Thread to start camel context
         */
        Thread camelTask = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    context = new DefaultCamelContext();
                    main = new Main();
                    main.enableHangupSupport();
                    context.addRoutes(new VDSCamelRouteBuilder(camelRouteDefnConfig));
                    context.start();
                    main.run();
                } catch (Exception e) {
                    _logger.error("Exception while opening Camel Active MQ Source: {}", e.getMessage(), e);
                }

            }
        });
        camelTask.start();
        
	}

	/* (non-Javadoc)
	 * @see com.informatica.vds.api.VDSSource#read(com.informatica.vds.api.VDSEventList)
	 */
	public void read(VDSEventList vdsEventList) throws Exception {

        _logger.debug("Reading from Camel Active MQ Source...");

        final VDSEvent vdsEvent = vdsEventList.createEvent(eventSize);
        final ByteBuffer readBuf = vdsEvent.getBuffer();

        /**
         *  TODO use poll() / take() discuss.
         */
        String message = queue.take().toString();

        if (message != null && message.getBytes().length > 0) {

            _logger.debug("Consuming Message from queue: {}", message);

            readBuf.put(message.getBytes());
            vdsEvent.setBufferLen(readBuf.position());

            _logger.debug("Received bytes: {}", vdsEvent.getBufferLen());

        }
	}
	


	/**
	 * Parses the configurations defined in vdsplugin.xml and populates the values
	 * @param vdsConfiguration
	 * @throws Exception
	 */
	public void parseConfig(VDSConfiguration vdsConfiguration) throws Exception {

        _logger.info("Parsing the fields defined in vdsplugin.xml");

        eventSize = vdsConfiguration.getInt(EVENT_SIZE);

        /**
         * Using configuration construct the Camel component URI
         */
        String activeMqBrokerUrl = vdsConfiguration.getString(ACTIVE_MQ_BROKER_URL);
        String useActiveMqQueueOrTopic = vdsConfiguration.getString(ACTIVE_MQ_TYPE);
        String activeMqQueueOrTopicName = "";

        String activeMqUserName = vdsConfiguration.getString(ACTIVE_MQ_USER_NAME);
        String activeMqPassword = vdsConfiguration.getString(ACTIVE_MQ_PASSWORD);
        if (activeMqBrokerUrl == null || activeMqBrokerUrl.trim().equals("")) {
            activeMqBrokerUrl = DEFAULT_BROKER_URL;
        }
        if (useActiveMqQueueOrTopic == null || useActiveMqQueueOrTopic.trim().equals("")
                || (!useActiveMqQueueOrTopic.equalsIgnoreCase(QUEUE)
                        && !useActiveMqQueueOrTopic.equalsIgnoreCase(TOPIC))) {
            useActiveMqQueueOrTopic = QUEUE;
            activeMqQueueOrTopicName = vdsConfiguration.getString(ACTIVE_MQ_QUEUE_NAME);
        } else if (useActiveMqQueueOrTopic.equalsIgnoreCase(QUEUE)) {
            activeMqQueueOrTopicName = vdsConfiguration.getString(ACTIVE_MQ_QUEUE_NAME);
        } else if (useActiveMqQueueOrTopic.equalsIgnoreCase(TOPIC)) {
            activeMqQueueOrTopicName = vdsConfiguration.getString(ACTIVE_MQ_TOPIC_NAME);
        }

        String activeMqAuthOptions = "";
        if (activeMqUserName != null && !activeMqUserName.trim().equals("") && activeMqPassword != null
                && !activeMqPassword.trim().equals("")) {
            activeMqAuthOptions = "?username=" + activeMqUserName + "&password=" + activeMqPassword;
        }

        useDefaultNameSpace = vdsConfiguration.getBoolean(USE_DEFAULT_NAMESPACE);
        defaultNameSpace = vdsConfiguration.getString(DEFAULT_NAMESPACE);
        nameSpacePrefix = vdsConfiguration.getString(NAMESPACE_PREFIX);

        /**
         * Construct camel component from URI
         */
        String camelFromURI = "activemq:" + useActiveMqQueueOrTopic.toLowerCase() + ":" + activeMqQueueOrTopicName
                + activeMqAuthOptions;

        _logger.info("Camel Component URI: {} eventSize: {}", camelFromURI, eventSize);

        /**
         * Construct Route Definition configuration for Filter, Split and Transform operations 
         */
        camelRouteDefnConfig = new CamelRouteConfigDef(camelFromURI, vdsConfiguration);

	}

    /**
     * Camel route builder
     * @author hpurohit
     *
     */
    class VDSCamelRouteBuilder extends RouteBuilder {

        private CamelRouteConfigDef camelRouteDefnConfig;

        public VDSCamelRouteBuilder(CamelRouteConfigDef camelRouteDefnConfig) {
            super();
            this.camelRouteDefnConfig = camelRouteDefnConfig;
        }

        @Override
        public void configure() throws Exception {

            RouteDefinition routeDefinition = new RouteDefinition();
            ProcessorDefinition<RouteDefinition> marshalProcess = null;
            ExpressionNode filterAndSplitExprNode = null;

            /** From **/
            routeDefinition.from(camelRouteDefnConfig.getCamelFromURI());

            /** Marshal **/
            marshalProcess = routeDefinition.marshal().string(Charset.forName(CHARSET_UTF_8).name());

            // Default Name space used for XPath filter and XPath splitter
            Namespaces namespaces = null;

            // XPathBuilder for splitter
            XPathBuilder xpathbuilder = null;
            if (useDefaultNameSpace) {
                _logger.info("Using default name space: {} prefix: {}", defaultNameSpace, nameSpacePrefix);
                namespaces = new Namespaces(nameSpacePrefix, defaultNameSpace);

                xpathbuilder = new XPathBuilder(camelRouteDefnConfig.getSplitXpathExpr());
                xpathbuilder.setNamespaces(namespaces.getNamespaces());

            } else {
                _logger.info("No default name space selected.");
            }

            /** Filter **/
            if (camelRouteDefnConfig.getFilterType().equalsIgnoreCase(FILTER_REGEX)) {
                // Filter using Regex
                filterAndSplitExprNode = marshalProcess
                        .filter(body().convertToString().regex(camelRouteDefnConfig.getFilterRegex()))
                        .convertBodyTo(String.class, CHARSET_UTF_8);
            } else if (camelRouteDefnConfig.getFilterType().equalsIgnoreCase(FILTER_XPATH)) {
                // Filter using XPath expression
                if (useDefaultNameSpace) {
                    // Using default name space provided
                    filterAndSplitExprNode = marshalProcess.filter()
                            .xpath(camelRouteDefnConfig.getFilterXpathExpr(), namespaces)
                            .convertBodyTo(String.class, CHARSET_UTF_8);
                } else {
                    filterAndSplitExprNode = marshalProcess.filter().xpath(camelRouteDefnConfig.getFilterXpathExpr())
                            .convertBodyTo(String.class, CHARSET_UTF_8);
                }
            } else if (camelRouteDefnConfig.getFilterType().equalsIgnoreCase(FILTER_CONTAINS)) {
                // Filter using Contains
                filterAndSplitExprNode = marshalProcess
                        .filter(body().convertToString().contains(camelRouteDefnConfig.getFilterContains()))
                        .convertBodyTo(String.class,
                        CHARSET_UTF_8);
            } else if (camelRouteDefnConfig.getFilterType().equalsIgnoreCase(FILTER_STARTS_WITH)) {
                // Filter using Starts with
                filterAndSplitExprNode = marshalProcess
                        .filter(body().convertToString().startsWith(camelRouteDefnConfig.getFilterStartsWith()))
                        .convertBodyTo(String.class,
                        CHARSET_UTF_8);
            } else if (camelRouteDefnConfig.getFilterType().equalsIgnoreCase(FILTER_ENDS_WITH)) {
                // Filter using Ends with
                filterAndSplitExprNode = marshalProcess
                        .filter(body().convertToString().endsWith(camelRouteDefnConfig.getFilterEndsWith()))
                        .convertBodyTo(String.class,
                        CHARSET_UTF_8);
            }

            /** Split **/
            if (camelRouteDefnConfig.getSplitType().equalsIgnoreCase(SPLIT_TOKEN)) {
                // Split by Token (String)
                if (filterAndSplitExprNode == null) {
                    filterAndSplitExprNode = marshalProcess
                            .split(body().convertToString().tokenize(camelRouteDefnConfig.getSplitToken())).streaming()
                            .convertBodyTo(String.class);
                } else {
                    filterAndSplitExprNode = filterAndSplitExprNode
                            .split(body().convertToString().tokenize(camelRouteDefnConfig.getSplitToken())).streaming()
                            .convertBodyTo(String.class);
                }
            } else if (camelRouteDefnConfig.getSplitType().equalsIgnoreCase(SPLIT_XPATH)) {
                // Split by XPath expression
                if (filterAndSplitExprNode == null) {
                    if (useDefaultNameSpace) {
                        // Using default name space provided
                        filterAndSplitExprNode = marshalProcess.split(xpathbuilder).streaming()
                                .convertBodyTo(String.class);
                    } else {
                        filterAndSplitExprNode = marshalProcess.split(xpath(camelRouteDefnConfig.getSplitXpathExpr()))
                                .streaming().convertBodyTo(String.class);
                    }
                } else {
                    if (useDefaultNameSpace) {
                        filterAndSplitExprNode = filterAndSplitExprNode.split(xpathbuilder).streaming()
                                .convertBodyTo(String.class);
                    } else {
                        filterAndSplitExprNode = filterAndSplitExprNode
                                .split(xpath(camelRouteDefnConfig.getSplitXpathExpr())).streaming()
                                .convertBodyTo(String.class);
                    }
                }
            } else if (camelRouteDefnConfig.getSplitType().equalsIgnoreCase(SPLIT_XML_TAG)) {
                // Split by XML tag
                if (filterAndSplitExprNode == null) {
                    filterAndSplitExprNode = marshalProcess.split().tokenizeXML(camelRouteDefnConfig.getSplitXmlTag())
                            .streaming().convertBodyTo(String.class);

                } else {
                    filterAndSplitExprNode = filterAndSplitExprNode.split()
                            .tokenizeXML(camelRouteDefnConfig.getSplitXmlTag()).streaming().convertBodyTo(String.class);
                }
            }

            // when no filter and split is selected
            if (filterAndSplitExprNode == null) {
                filterAndSplitExprNode = marshalProcess.loop(1).convertBodyTo(String.class, CHARSET_UTF_8);
            }

            /** Transform **/
            // Append
            filterAndSplitExprNode.transform(body().append(camelRouteDefnConfig.getTransformAppendString()));
            // Prepend
            filterAndSplitExprNode.transform(body().prepend(camelRouteDefnConfig.getTransformPrependString()));

            /** Processor **/
            filterAndSplitExprNode.process(new VDSCamelProcessor());

            routeDefinition.configureChild(filterAndSplitExprNode);

            getContext().addRouteDefinition(routeDefinition);
        }

    }

    /**
     * Camel exchange processor
     * @author hpurohit
     *
     */
    class VDSCamelProcessor implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {

            try {
                _logger.debug("processing exchange: [{}]", exchange.toString());
                byte[] bytes = exchange.getIn().getBody(byte[].class);
                if (bytes != null) {
                    String messageBody = new String(bytes, Charset.forName(CHARSET_UTF_8));
                    queue.put(messageBody);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }

        }

    }

    @Override
    public void setRetryPolicyHandler(IPluginRetryPolicy iPluginRetryPolicyHandler) {
        // TODO Auto-generated method stub

    }
}

