package edu.iastate.shibboleth.profiler.primary;

/*-
 * #%L
 * shibboleth-maven-plugin
 * %%
 * Copyright (C) 2021 - 2022 Iowa State University
 * %%
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
 * #L%
 */

import edu.iastate.shibboleth.constants.ControlId;
import org.pitest.functional.SideEffect1;
import org.pitest.util.CommunicationThread;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.SafeDataOutputStream;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class PrimaryProfilerCommunicationThread extends CommunicationThread {
    private final DataReceiver receiver;

    public PrimaryProfilerCommunicationThread(final ServerSocket socket,
                                              final PrimaryProfilerArguments arguments) {
        this(socket, new DataSender(arguments), new DataReceiver());
    }

    private PrimaryProfilerCommunicationThread(final ServerSocket socket,
                                               final DataSender sender,
                                               final DataReceiver receiver) {
        super(socket, sender, receiver);
        this.receiver = receiver;
    }

    public Map<String, IV> getIVMap() {
        return this.receiver.ivMap;
    }

    public int getCoveredBranchesCount() {
        return this.receiver.coveredBranchesCount;
    }

    private static class DataSender implements SideEffect1<SafeDataOutputStream> {
        final PrimaryProfilerArguments arguments;

        DataSender(final PrimaryProfilerArguments arguments) {
            this.arguments = arguments;
        }

        @Override
        public void apply(final SafeDataOutputStream dos) {
            dos.write(this.arguments);
            dos.flush();
        }
    }

    private static class DataReceiver implements ReceiveStrategy {
        Map<String, IV> ivMap;

        int coveredBranchesCount;

        @Override
        @SuppressWarnings({"unchecked"})
        public void apply(byte code, SafeDataInputStream is) {
            switch (code) {
                case ControlId.REPORT_RECORDED_INSN_VECTOR_MAP:
                    this.ivMap = is.read(HashMap.class);
                    break;
                case ControlId.REPORT_COVERED_BRANCHES_COUNT:
                    this.coveredBranchesCount = is.readInt();
            }
        }
    }
}