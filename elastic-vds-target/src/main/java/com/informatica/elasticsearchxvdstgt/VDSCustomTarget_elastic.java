package com.informatica.elasticsearchxvdstgt;

import com.informatica.vds.api.IPluginRetryPolicy;
import com.informatica.vds.api.VDSConfiguration;
import com.informatica.vds.api.VDSEvent;
import com.informatica.vds.api.VDSTarget;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import io.searchbox.indices.mapping.PutMapping;

//import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by jerry on 11/03/14.
 */
public class VDSCustomTarget_elastic implements VDSTarget {

    //private Client _client;
    //private Node _node;
    private String _indexName;
    private String _typeName;
    //private String _esHome;
    private String _mappingJson;
    private JestClient _client;

    private static final Logger _logger = LoggerFactory.getLogger(VDSCustomTarget_elastic.class);
    protected IPluginRetryPolicy pluginRetryPolicyHandler;
    public static final String INDEX_NAME = "index-name";
    public static final String TYPE_NAME = "type-name";
    public static final String ES_URL = "es_url";
    //public static final String ES_HOME = "es_home";
    public static final String MAPPING_JSON = "MappingJSON";

    public static final boolean HaveMapping = false;
    private boolean debugEnabled = false;

    @Override
    public void open(VDSConfiguration ctx) throws Exception {
        if (_logger.isDebugEnabled()) {
            debugEnabled = true;
        }
        _logger.info("Initializing ElasticSearch target");
        String es_url = ctx.getString(ES_URL).trim();
        _indexName = ctx.getString(INDEX_NAME).trim();
        _typeName = ctx.getString(TYPE_NAME).trim();
        //_esHome = ctx.getString(ES_HOME).trim();

        _logger.info("Read parameters for target from properties:"
                + "es_url='" + es_url
                + "', index=" + _indexName + ", type=" + _typeName + ")");
        if (debugEnabled) {
            _logger.debug("Will now create JestClient object _client");
        }
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(es_url)
                .multiThreaded(true)
                .build());
        _client = factory.getObject();

        if (debugEnabled) {
            _logger.debug("_client=" + _client);
            _logger.debug("_client created");
        }

        _logger.info("ElasticSearch target initialized");
        if (HaveMapping) {
            // get the mapping string
            _mappingJson = ctx.getString(MAPPING_JSON).trim();
            if (debugEnabled) {
                _logger.debug("HaveMapping=" + HaveMapping);
                _logger.debug("_mappingJson=" + _mappingJson);
                _logger.debug("will execute putMapping");
            }

            PutMapping putMapping = new PutMapping.Builder(_indexName, _typeName,
                    _mappingJson).build();
            _client.execute(putMapping);
        }
    }

    @Override
    public void write(VDSEvent strm) throws Exception {
        String event=new String(strm.getBuffer().array(), StandardCharsets.UTF_8);
        if (debugEnabled) {
            _logger.debug("write entered...");
            _logger.debug("Getting event data :'"+event+"'");
        }

        Index index = new Index.Builder(event).index(_indexName).type(_typeName).build();
        _client.execute(index);
        if (debugEnabled) {
            _logger.debug("Wrote data (" + strm.getBufferLen() + " bytes) to ElasticSearch index");
        }
    }

    @Override
    public void close() throws IOException {
//        _client.close();
//        _node.close();
        _logger.debug("Shutdown ElasticSearch target");
    }

    @Override
    public void setRetryPolicyHandler(IPluginRetryPolicy iPluginRetryPolicyHandler) {
        this.pluginRetryPolicyHandler = iPluginRetryPolicyHandler;
        this.pluginRetryPolicyHandler.setLogger(_logger);
    }
}
