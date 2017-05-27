/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.query.join;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class OuterJoinTestCase {
    private static final Logger log = Logger.getLogger(OuterJoinTestCase.class);
    private int inEventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        inEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void joinTest1() throws InterruptedException {
        log.info("Outer Join test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define stream cseEventStream (symbol string, price float, volume int); " +
                "define stream twitterStream (user string, tweet string, company string); ";
        String query = "@info(name = 'query1') " +
                "from cseEventStream#window.length(3) full outer join twitterStream#window.length(1) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.price " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    inEventCount++;
                    if (inEventCount == 1) {
                        Assert.assertEquals("WSO2", inEvent.getData(0));
                        Assert.assertEquals(null, inEvent.getData(1));
                        Assert.assertEquals(55.6f, inEvent.getData(2));
                    }
                    if (inEventCount == 2) {
                        Assert.assertEquals("WSO2", inEvent.getData(0));
                        Assert.assertEquals("Hello World", inEvent.getData(1));
                        Assert.assertEquals(55.6f, inEvent.getData(2));
                    }
                    if (inEventCount == 3) {
                        Assert.assertEquals("IBM", inEvent.getData(0));
                        Assert.assertEquals(null, inEvent.getData(1));
                        Assert.assertEquals(75.6f, inEvent.getData(2));
                    }
                    if (inEventCount == 4) {
                        Assert.assertEquals("WSO2", inEvent.getData(0));
                        Assert.assertEquals("Hello World", inEvent.getData(1));
                        Assert.assertEquals(57.6f, inEvent.getData(2));
                    }
                }
                eventArrived = true;
            }
        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
        executionPlanRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
        cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(500);
        Assert.assertEquals(4, inEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void joinTest2() throws InterruptedException {
        log.info("Outer Join test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define stream cseEventStream (symbol string, price float, volume int); " +
                "define stream twitterStream (user string, tweet string, company string); ";
        String query = "@info(name = 'query1') " +
                "from cseEventStream#window.length(1) right outer join twitterStream#window.length(2) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.price,twitterStream" +
                ".company as company " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    inEventCount++;
                    if (inEventCount == 1) {
                        Assert.assertEquals(null, inEvent.getData(0));
                        Assert.assertEquals("Hello World", inEvent.getData(1));
                        Assert.assertEquals(null, inEvent.getData(2));
                        Assert.assertEquals("WSO2", inEvent.getData(3));
                    }
                    if (inEventCount == 2) {
                        Assert.assertEquals(null, inEvent.getData(0));
                        Assert.assertEquals("Welcome", inEvent.getData(1));
                        Assert.assertEquals(null, inEvent.getData(2));
                        Assert.assertEquals("IBM", inEvent.getData(3));
                    }
                    if (inEventCount == 3) {
                        Assert.assertEquals("WSO2", inEvent.getData(0));
                        Assert.assertEquals("Hello World", inEvent.getData(1));
                        Assert.assertEquals(57.6f, inEvent.getData(2));
                        Assert.assertEquals("WSO2", inEvent.getData(3));
                    }
                }
                eventArrived = true;
            }

        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
        executionPlanRuntime.start();
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        cseEventStreamHandler.send(new Object[]{"BMW", 57.6f, 100});
        twitterStreamHandler.send(new Object[]{"User2", "Welcome", "IBM"});
        cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(500);
        Assert.assertEquals(3, inEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void joinTest3() throws InterruptedException {
        log.info("Outer Join test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define stream cseEventStream (symbol string, price float, volume int); " +
                "define stream twitterStream (user string, tweet string, company string); ";
        String query = "@info(name = 'query1') " +
                "from cseEventStream#window.length(2) left outer join twitterStream#window.length(1) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.price,twitterStream" +
                ".company as company " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    inEventCount++;
                    if (inEventCount == 1) {
                        Assert.assertEquals("WSO2", inEvent.getData(0));
                        Assert.assertEquals(null, inEvent.getData(1));
                        Assert.assertEquals(57.6f, inEvent.getData(2));
                        Assert.assertEquals(null, inEvent.getData(3));
                    }
                    if (inEventCount == 2) {
                        Assert.assertEquals("IBM", inEvent.getData(0));
                        Assert.assertEquals(null, inEvent.getData(1));
                        Assert.assertEquals(47.6f, inEvent.getData(2));
                        Assert.assertEquals(null, inEvent.getData(3));
                    }
                    if (inEventCount == 3) {
                        Assert.assertEquals("WSO2", inEvent.getData(0));
                        Assert.assertEquals("Hello World", inEvent.getData(1));
                        Assert.assertEquals(57.6f, inEvent.getData(2));
                        Assert.assertEquals("WSO2", inEvent.getData(3));
                    }
                }
                eventArrived = true;
            }

        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
        executionPlanRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
        twitterStreamHandler.send(new Object[]{"User2", "Welcome", "BMW"});
        cseEventStreamHandler.send(new Object[]{"IBM", 47.6f, 200});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        Thread.sleep(500);
        Assert.assertEquals(3, inEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = SiddhiParserException.class)
    public void joinTest4() throws InterruptedException {
        log.info("Outer Join test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define stream cseEventStream (symbol string, price float, volume int); " +
                "define stream twitterStream (user string, tweet string, symbol string); ";
        String query = "@info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) outer join twitterStream#window.time(1 sec) " +
                "on cseEventStream.symbol== twitterStream.symbol " +
                "select * " +
                "insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.start();
        executionPlanRuntime.shutdown();
    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void joinTest5() throws InterruptedException {
        log.info("Outer Join test5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define stream cseEventStream (symbol string, price float, volume int); " +
                "define stream twitterStream (user string, tweet string, symbol string); ";
        String query = "@info(name = 'query1') " +
                "from cseEventStream#window.time(1 sec) full outer join twitterStream#window.time(1 sec) " +
                "on cseEventStream.symbol== twitterStream.symbol " +
                "select * " +
                "insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.start();
        executionPlanRuntime.shutdown();
    }

    @Test
    public void joinTest6() throws InterruptedException {
        log.info("Outer Join test6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define stream cseEventStream (symbol string, price float, volume int); " +
                "define stream twitterStream (user string, tweet string, company string); ";
        String query = "@info(name = 'query1') " +
                "from cseEventStream#window.length(2) right outer join twitterStream " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, twitterStream.tweet " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
            }

        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        cseEventStreamHandler.send(new Object[]{"IBM", 57.6f, 100});
        cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(500);
        Assert.assertFalse(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void joinTest7() throws InterruptedException {
        log.info("Outer Join test7");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define stream cseEventStream (symbol string, price float, volume int); " +
                "define stream twitterStream (user string, tweet string, company string); ";
        String query = "@info(name = 'query1') " +
                "from cseEventStream left outer join twitterStream#window.length(2) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, twitterStream.tweet " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
            }

        });
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
        executionPlanRuntime.start();
        twitterStreamHandler.send(new Object[]{"User2", "Welcome", "BMW"});
        twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
        Thread.sleep(500);
        Assert.assertFalse(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void joinTest8() throws InterruptedException {
        log.info("Outer Join test8");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define stream cseEventStream (symbol string, price float, volume int); " +
                "define stream twitterStream (user string, tweet string, company string); ";
        String query = "@info(name = 'query1') " +
                "from cseEventStream#window.length(3) inner join twitterStream#window.length(1) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.price " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    inEventCount++;
                    if (inEventCount == 1) {
                        Assert.assertEquals("WSO2", inEvent.getData(0));
                        Assert.assertEquals("Hello World", inEvent.getData(1));
                        Assert.assertEquals(55.6f, inEvent.getData(2));
                    }
                    if (inEventCount == 2) {
                        Assert.assertEquals("WSO2", inEvent.getData(0));
                        Assert.assertEquals("Hello World", inEvent.getData(1));
                        Assert.assertEquals(57.6f, inEvent.getData(2));
                    }
                }
                eventArrived = true;
            }

        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
        executionPlanRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
        twitterStreamHandler.send(new Object[]{"User", "Hello World", "WSO2"});
        cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
        Thread.sleep(500);
        cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(2000);
        Assert.assertEquals(2, inEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void joinTest9() throws InterruptedException {
        log.info("Outer Join test9");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "define stream cseEventStream (symbol string, price float, volume int); " +
                "define stream twitterStream (user string, tweet string, company string); ";
        String query = "@info(name = 'query1') " +
                "from cseEventStream#window.length(3) join twitterStream#window.length(1) " +
                "on cseEventStream.symbol== twitterStream.company " +
                "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.price " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                eventArrived = true;
            }
        });

        InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
        executionPlanRuntime.start();
        cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
        twitterStreamHandler.send(new Object[]{"User", "Hello World", "BMW"});
        cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
        cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(500);
        Assert.assertFalse(eventArrived);
        executionPlanRuntime.shutdown();
    }
}
