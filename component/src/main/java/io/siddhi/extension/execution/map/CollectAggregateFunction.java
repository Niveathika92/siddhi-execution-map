/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.siddhi.extension.execution.map;

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.ReturnAttribute;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiQueryContext;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.executor.ExpressionExecutor;
import io.siddhi.core.query.processor.ProcessingMode;
import io.siddhi.core.query.selector.attribute.aggregator.AttributeAggregatorExecutor;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.query.api.definition.Attribute;

import java.util.HashMap;

/**
 * Implementation class for map:collect()
 */
@Extension(
        namespace = "map",
        name = "collect",
        description = "Collect multiple key-value pairs and construct a map, only distinct keys are collected if a " +
                "duplicate key arrives, it overrides the old value",
        parameters = {
                @Parameter(
                        name = "key",
                        description = "Key of the map entry",
                        type = DataType.OBJECT
                ),
                @Parameter(
                        name = "value",
                        description = "Value of the map entry",
                        type = DataType.OBJECT
                )
        },
        returnAttributes = {
                @ReturnAttribute(
                        description = "Map containing all the key-value pairs.",
                        type = DataType.OBJECT
                )
        },
        examples = {
                @Example(
                        syntax = "from CSCStream#window.lengthBatch(10)\n" +
                                 "select map:collect(key, value) as studentDetails\n" +
                                 "insert into StudentSteam;",
                        description = "For the window expiry of 10 events, the collect() function will collect " +
                                "attribute values of key and value to a single map and return as studentDetails."
                )
        }
)
public class CollectAggregateFunction extends AttributeAggregatorExecutor<State> {

    @Override
    protected StateFactory<State> init(ExpressionExecutor[] expressionExecutors, ProcessingMode processingMode,
                                       boolean outputExpectsExpiredEvents, ConfigReader configReader,
                                       SiddhiQueryContext siddhiQueryContext) {
        int attributesLength = expressionExecutors.length;
        if ((attributesLength != 2)) {
            throw new SiddhiAppCreationException("map:collect() function  should have two parameters , " +
                    "but found '" + attributesLength + "' parameters.");
        }
        return MapState::new;
    }

    @Override
    public Object processAdd(Object object, State state) {
        // Cannot be reached due validation at the init()
        return null;
    }

    @Override
    public Object processAdd(Object[] objects, State state) {
        ((MapState) state).addEntry(objects[0], objects[1]);
        return ((MapState) state).getClonedMapOfValues();
    }

    @Override
    public Object processRemove(Object object, State state) {
        // Cannot be reached due validation at the init()
        return null;
    }

    @Override
    public Object processRemove(Object[] objects, State state) {
        ((MapState) state).removeEntry(objects[0]);
        return ((MapState) state).getClonedMapOfValues();
    }


    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.OBJECT;
    }


    @Override
    public Object reset(State state) {
        ((MapState) state).setMapOfValues(new HashMap<>());
        return new HashMap<>();
    }
}
