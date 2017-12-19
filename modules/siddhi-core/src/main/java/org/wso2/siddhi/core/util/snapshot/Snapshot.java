/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.util.snapshot;

import java.io.Serializable;

/**
 * The class which encloses the state to be serialized.
 */
public class Snapshot implements Serializable {
    private Object state;
    private boolean isIncrementalSnapshot;
    private long timeDuration = -1L; //The time period until which this snapshot is valid from the time of its creation.

    public Snapshot() {

    }

    public Snapshot(Object state) {
        this.state = state;
        this.isIncrementalSnapshot = false;
    }

    public Snapshot(Object state, boolean isIncrementalSnapshot) {
        this.state = state;
        this.isIncrementalSnapshot = isIncrementalSnapshot;
    }

    public Snapshot(Object state, boolean isIncrementalSnapshot, long timeDuration) {
        this.state = state;
        this.isIncrementalSnapshot = isIncrementalSnapshot;
        this.timeDuration = timeDuration;
    }

    public boolean isIncrementalSnapshot() {
        return isIncrementalSnapshot;
    }

    public long getTimeDuration() {
        return timeDuration;
    }

    public Object getState() {
        return state;
    }
}