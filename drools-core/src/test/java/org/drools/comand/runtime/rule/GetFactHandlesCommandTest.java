package org.drools.comand.runtime.rule;

import static org.junit.Assert.*;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.drools.command.impl.ContextImpl;
import org.drools.command.impl.DefaultCommandService;
import org.drools.command.impl.FixedKnowledgeCommandContext;
import org.drools.command.runtime.rule.GetFactHandlesCommand;
import org.drools.common.InternalFactHandle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.KnowledgeBase;
import org.kie.KnowledgeBaseFactory;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.rule.FactHandle;

@SuppressWarnings("unchecked")
public class GetFactHandlesCommandTest {

    private StatefulKnowledgeSession ksession;
    private DefaultCommandService commandService;
    private Random random = new Random();
    
    @Before
    public void setup() { 
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        ksession = kbase.newStatefulKnowledgeSession();
        FixedKnowledgeCommandContext kContext 
            = new FixedKnowledgeCommandContext( new ContextImpl( "ksession", null ), null, null, this.ksession, null );
        commandService = new DefaultCommandService(kContext);
        
    }
    
    @After
    public void cleanUp() { 
       ksession.dispose(); 
    }
    
    @Test
    public void getEmptyFactHandlesTest() { 
        GetFactHandlesCommand command = new GetFactHandlesCommand();
        Object result = commandService.execute(command);
        if( result instanceof Collection<?> ) { 
            assertNotNull(result);
            assertTrue(((Collection<?>) result).isEmpty());
        }
        else { 
           fail("result of command was NOT a collection of FactHandles"); 
        }
    }
    
    @Test
    public void getOneFactHandleTest() { 
        String randomFact = "" + random.nextLong();
        ksession.insert(randomFact);
        GetFactHandlesCommand command = new GetFactHandlesCommand();
        Object result = commandService.execute(command);
        
        verifyThatCollectionContains1FactHandleWithThisFact(randomFact, result);
    }

    @Test
    public void getMultipleFactHandleTest() { 
        HashSet<String> factSet = new HashSet<String>();
        int numFacts = 4;
        for( int i = 0; i < numFacts; ++i ) { 
            factSet.add("" + random.nextInt());
        }
        for( String fact : factSet ) { 
            ksession.insert(fact);
        }
        
        GetFactHandlesCommand command = new GetFactHandlesCommand();
        Object result = commandService.execute(command);
        
        verifyThatCollectionContainsTheseFactHandle(factSet, result);
    }

    @Test
    public void getEmptyDisconnectedFactHandlesTest() { 
        GetFactHandlesCommand command = new GetFactHandlesCommand(true);
        Object result = commandService.execute(command);
        if( result instanceof Collection<?> ) { 
            assertNotNull(result);
            assertTrue(((Collection<?>) result).isEmpty());
        }
        else { 
           fail("result of command was NOT a collection of FactHandles"); 
        }
    }

    @Test
    public void getOneDisconnectedFactHandleTest() { 
        System.out.println( Thread.currentThread().getStackTrace()[1].getMethodName() );
        String randomFact = "" + random.nextLong();
        ksession.insert(randomFact);
        
        // Retrieve and verify fact handle collections
        GetFactHandlesCommand command = new GetFactHandlesCommand(false);
        Object result = commandService.execute(command);
        verifyThatCollectionContains1FactHandleWithThisFact(randomFact, result);
        FactHandle factHandle = (FactHandle) ((Collection<FactHandle>) result).toArray()[0];
        
        command = new GetFactHandlesCommand(false);
        result = commandService.execute(command);
        verifyThatCollectionContains1FactHandleWithThisFact(randomFact, result);
        FactHandle connectedFactHandle = (FactHandle) ((Collection<FactHandle>) result).toArray()[0];
        
        command = new GetFactHandlesCommand(true);
        result = commandService.execute(command);
        verifyThatCollectionContains1FactHandleWithThisFact(randomFact, result);
        FactHandle disconnectedFactHandle = (FactHandle) ((Collection<FactHandle>) result).toArray()[0];
      
        // Test fact handle collections
        assertTrue( factHandle == connectedFactHandle );
        assertTrue( ! (factHandle == disconnectedFactHandle) );
    }

    @Test
    public void getMultipleDisconnectedFactHandleTest() { 
        System.out.println( "\nTest: " + Thread.currentThread().getStackTrace()[1].getMethodName() );
        HashSet<String> factSet = new HashSet<String>();
        int numFacts = 4;
        for( int i = 0; i < numFacts; ++i ) { 
            factSet.add("" + random.nextInt());
        }
        for( String fact : factSet ) { 
            ksession.insert(fact);
        }
        
        GetFactHandlesCommand command = new GetFactHandlesCommand(false);
        Object result = commandService.execute(command);
        verifyThatCollectionContainsTheseFactHandle(factSet, result);
        Collection<FactHandle> factHandles = ((Collection<FactHandle>) result);
        
        command = new GetFactHandlesCommand(false);
        result = commandService.execute(command);
        verifyThatCollectionContainsTheseFactHandle(factSet, result);
        Collection<FactHandle> connectedFactHandles = ((Collection<FactHandle>) result);
        
        command = new GetFactHandlesCommand(true);
        result = commandService.execute(command);
        verifyThatCollectionContainsTheseFactHandle(factSet, result);
        Collection<FactHandle> disconnectedFactHandles = ((Collection<FactHandle>) result);
       
        // Test fact handle collections
        HashSet<FactHandle> factHandlesCopy = new HashSet<FactHandle>(factHandles);
        for( int i = 0; i < connectedFactHandles.size(); ++i ) { 
            for( Object connectedFact : connectedFactHandles ) { 
               Iterator<FactHandle> iter = factHandlesCopy.iterator();
               while(iter.hasNext() ) { 
                  Object fact = iter.next();
                  if( fact == connectedFact ) { 
                      iter.remove();
                  }
               }
            }
        }
        assertTrue( factHandlesCopy.isEmpty() );
        
        for( int i = 0; i < disconnectedFactHandles.size(); ++i ) { 
            for( Object disconnectedFact : disconnectedFactHandles ) { 
               for( Object fact : factHandles ) { 
                  assertTrue( ! (fact == disconnectedFact) );
               }
            }
        }
        assertTrue( factHandles.size() == disconnectedFactHandles.size() );
        
    }

    /**
     * Helper methods
     */
    private void verifyThatCollectionContains1FactHandleWithThisFact(String fact, Object collection) { 
        if( collection instanceof Collection<?> ) { 
            Collection<FactHandle> factHandles = null;
            try { 
                factHandles = (Collection<FactHandle>) collection;
            }
            catch( Exception e ) { 
               fail( "Collection was not a Colleciton<FactHandle> " + e.getMessage()); 
            }
            
            assertTrue(! factHandles.isEmpty());
            assertTrue(factHandles.size() == 1);
            InternalFactHandle factHandle = (InternalFactHandle) factHandles.toArray()[0];
            assertTrue(fact.equals(factHandle.getObject()));
         }
         else { 
            fail("result of command was NOT a collection of FactHandles"); 
         }
    }

    private void verifyThatCollectionContainsTheseFactHandle(HashSet<String> factSet, Object collection) { 
        factSet = (HashSet<String>) factSet.clone();
        if( collection instanceof Collection<?> ) { 
            Collection<FactHandle> factHandles = (Collection<FactHandle>) collection;
            assertTrue(! factHandles.isEmpty());
            assertTrue(factSet.size() + "inserted but only " + factHandles.size() + " facts retrieved", factHandles.size() == factSet.size());
            Object [] internalFactHandles = factHandles.toArray();
            for( int i = 0; i < internalFactHandles.length; ++i ) { 
                Object factObject = ((InternalFactHandle) internalFactHandles[i]).getObject();
                assertTrue(factSet.contains(factObject));
                factSet.remove(factObject);
            }
            assertTrue( "Additional facts found that weren't inserted.", factSet.isEmpty() );
        }
        else { 
            fail("result of command was NOT a collection of FactHandles"); 
        }
    }

}
