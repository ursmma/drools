/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.common;

import org.drools.Cheese;
import org.drools.base.evaluators.Operator;

import org.drools.reteoo.NodeTypeEnums;
import org.drools.spi.BetaNodeFieldConstraint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DoubleBetaConstraintsTest extends BaseBetaConstraintsTest {

    @Test
    public void testAllNoneIndexed() {
        BetaNodeFieldConstraint constraint0 = getConstraint( "cheeseType0", Operator.NOT_EQUAL, "type", Cheese.class );
        BetaNodeFieldConstraint constraint1 = getConstraint( "cheeseType1", Operator.NOT_EQUAL, "type", Cheese.class );
        BetaNodeFieldConstraint[] constraints = new BetaNodeFieldConstraint[] { constraint0, constraint1 };
        checkBetaConstraints( constraints, DoubleBetaConstraints.class );
    }
    
    @Test
    public void testOneIndexed() {
        BetaNodeFieldConstraint constraint0 = getConstraint( "cheeseType0", Operator.EQUAL, "type", Cheese.class );
        BetaNodeFieldConstraint constraint1 = getConstraint( "cheeseType1", Operator.NOT_EQUAL, "type", Cheese.class );
        BetaNodeFieldConstraint[] constraints = new BetaNodeFieldConstraint[] { constraint0, constraint1 };
        checkBetaConstraints( constraints, DoubleBetaConstraints.class );
        
        constraint0 = getConstraint( "cheeseType0", Operator.NOT_EQUAL, "type", Cheese.class );
        constraint1 = getConstraint( "cheeseType1", Operator.EQUAL, "type", Cheese.class );
        constraints = new BetaNodeFieldConstraint[] { constraint0, constraint1 };
        checkBetaConstraints( constraints, DoubleBetaConstraints.class );
    }

    @Test
    public void testOneIndexedForComparison() {
        BetaNodeFieldConstraint constraint0 = getConstraint( "cheeseType0", Operator.GREATER, "type", Cheese.class );
        BetaNodeFieldConstraint constraint1 = getConstraint( "cheeseType1", Operator.NOT_EQUAL, "type", Cheese.class );
        BetaNodeFieldConstraint[] constraints = new BetaNodeFieldConstraint[] { constraint0, constraint1 };
        checkBetaConstraints( constraints, DoubleBetaConstraints.class, NodeTypeEnums.ExistsNode );

        constraint0 = getConstraint( "cheeseType0", Operator.NOT_EQUAL, "type", Cheese.class );
        constraint1 = getConstraint( "cheeseType1", Operator.GREATER, "type", Cheese.class );
        constraints = new BetaNodeFieldConstraint[] { constraint0, constraint1 };
        checkBetaConstraints( constraints, DoubleBetaConstraints.class, NodeTypeEnums.ExistsNode );
    }

    @Test
    public void testTwoIndexed() {
        BetaNodeFieldConstraint constraint0 = getConstraint( "cheeseType0", Operator.EQUAL, "type", Cheese.class );
        BetaNodeFieldConstraint constraint1 = getConstraint( "cheeseType1", Operator.EQUAL, "type", Cheese.class );
        BetaNodeFieldConstraint[] constraints = new BetaNodeFieldConstraint[] { constraint0, constraint1 };
        checkBetaConstraints( constraints, DoubleBetaConstraints.class );
    }
    
}
