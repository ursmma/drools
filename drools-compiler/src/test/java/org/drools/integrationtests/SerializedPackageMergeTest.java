package org.drools.integrationtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.drools.Message;
import org.drools.audit.WorkingMemoryConsoleLogger;
import org.junit.Test;
import org.kie.KnowledgeBase;
import org.kie.KnowledgeBaseFactory;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderFactory;
import org.kie.builder.ResourceType;
import org.kie.definition.KnowledgePackage;
import org.kie.io.ResourceFactory;
import org.kie.runtime.StatelessKnowledgeSession;

public class SerializedPackageMergeTest {
    private static final DateFormat DF   = new SimpleDateFormat( "dd-MMM-yyyy", Locale.UK );
    private static final String[]   DRLs = {"HelloWorld.drl","test_Serialization1.drl"};

    @Test
    public void testRuleExecutionWithoutSerialization() {
        try {
            // without serialization, it works.
            testRuleExecution( getSession( false ) );
        } catch ( Exception e ) {
            e.printStackTrace();
            fail( "Should not have raised any exception. Message: " + e.getMessage() );
        }
    }

    @Test
    public void testRuleExecutionWithSerialization() throws Exception {
        try {
            // with serialized packages, NullPointerException
            testRuleExecution( getSession( true ) );
        } catch ( Exception e ) {
            e.printStackTrace();
            fail( "Should not have raised any exception. Message: " + e.getMessage() );
        }
    }

    private void testRuleExecution(StatelessKnowledgeSession session) throws Exception {
        List<Object> list = new ArrayList<Object>();
        session.setGlobal( "list",
                           list );

        session.execute( getObject() );

        assertEquals( 2,
                      list.size() );
    }

    private Message getObject() throws ParseException {
        Message message = new Message();

        message.setMessage( "hola" );
        message.setNumber( 50 );
        message.getList().add( "hello" );
        message.setBirthday( DF.parse( "10-Jul-1976" ) );
        return message;
    }

    private StatelessKnowledgeSession getSession(boolean serialize) throws Exception {
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        for ( String drl : DRLs ) {
            KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
            kbuilder.add( ResourceFactory.newInputStreamResource( getClass().getResourceAsStream( drl ) ),
                          ResourceType.DRL );
            
            assertFalse( kbuilder.getErrors().toString(),
                         kbuilder.hasErrors() );
            
            Collection<KnowledgePackage> kpkgs = kbuilder.getKnowledgePackages();

            Collection<KnowledgePackage> newCollection = null;
            if ( serialize ) {
                newCollection = new ArrayList<KnowledgePackage>();
                for( KnowledgePackage kpkg : kpkgs) {
                    kpkg = SerializationHelper.serializeObject( kpkg );
                    newCollection.add( kpkg );
                }
            } else {
                newCollection = kpkgs;
            }
            kbase.addKnowledgePackages( newCollection );
        }
        return kbase.newStatelessKnowledgeSession();
    }

}
