/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.informatica.elasticsearchxvdtgt.test;



import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author infa
 */
public class TestESConnect {
    
    public TestESConnect() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
//        String cluster = "quickstart.cloudera:9200";
//        String _indexName = "WebLogExt";
//        String _typeName = "log";
//        String _esHome = "/infa/elasticsearch-2.3.4";
//        
//        Node _node;
//        _node = nodeBuilder().settings(Settings.builder().put("path.home", _esHome))
//                .client(true)
//                .clusterName(cluster)
//                .node();
//        assertTrue("node is not null", _node != null );
        assertTrue("this should pass",true);
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void hello() {
        
        
    }
}
