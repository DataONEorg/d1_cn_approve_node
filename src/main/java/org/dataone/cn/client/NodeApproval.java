/**
 * This work was created by participants in the DataONE project, and is
 * jointly copyrighted by participating institutions in DataONE. For 
 * more information on DataONE, see our web site at http://dataone.org.
 *
 *   Copyright ${year}
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * $Id$
 */

package org.dataone.cn.client;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


import org.dataone.service.exceptions.*;
import org.dataone.service.types.v1.Node;
import org.dataone.service.cn.impl.v1.NodeRegistryService;
import org.dataone.service.types.v1.NodeReference;
import org.dataone.cn.hazelcast.ClientConfiguration;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.dataone.cn.ldap.NodeAccess;
/**
 *
 * @author waltz
 */
public class NodeApproval {

    NodeRegistryService nodeRegistryService = new NodeRegistryService();
    private static String[] approvedResponses = {"Y", "N", "C"};
    NodeAccess nodeAccess = new NodeAccess();
    HazelcastInstance hzclient = null;

    public NodeApproval() throws FileNotFoundException {
        ClasspathXmlConfig hzConfig = new ClasspathXmlConfig("org/dataone/configuration/hazelcastClientConf.xml");
        ClientConfiguration clientConfiguration = new ClientConfiguration(hzConfig);

        hzclient = HazelcastClient.newHazelcastClient(clientConfiguration.getGroup(), clientConfiguration.getPassword(),
                    clientConfiguration.getLocalhost());
    }

    public void approveNode(String nodeId) throws ServiceFailure, NotFound {
        // If the nodeId is not provided, prompt from console
        if (nodeId == null) {
            nodeId = promptPendingList();
        }
        NodeReference approveNodeId = new NodeReference();
        approveNodeId.setValue(nodeId);
        // Set the approval status to TRUE

        nodeAccess.setNodeApproved(approveNodeId, Boolean.TRUE);
        // Make certain the node can be retrieved now
        Node node = nodeRegistryService.getNode(approveNodeId);
        // This is important! Hazelcast need to know that a node is available for processing
        // perform an update so that a message is generated to d1_processing
        IMap<NodeReference, Node> hzNodes = hzclient.getMap("hzNodes");
        hzNodes.put(approveNodeId, node);
    }

    private String promptPendingList() throws ServiceFailure {
        System.console().printf("Pending Nodes to Approve\n");
        String approval = "";
        String nodeId = "";
        List<NodeReference> pendingNodeList = nodeAccess.getPendingNodeReferenceList();
        do {
            int rows = pendingNodeList.size() / 4;
            for (int i = 0; i < rows; ++i) {

                System.console().printf("%s\t%s\t%s\t%s\n", pendingNodeList.get((i * 4) + 0).getValue(),
                        pendingNodeList.get((i * 4) + 1).getValue(),
                        pendingNodeList.get((i * 4) + 2).getValue(),
                        pendingNodeList.get((i * 4) + 3).getValue());
            }
            int remainder = pendingNodeList.size() % 4;

            if (remainder > 0) {
                pendingNodeList.subList(rows, rows);
                String[] consoleArgs = new String[remainder];
                StringBuilder formatStringBuilder = new StringBuilder();
                for (int i = 0; i < remainder; ++i) {
                    formatStringBuilder.append("%s\t");
                }
                formatStringBuilder.append("\n");
                System.console().printf(formatStringBuilder.toString(), consoleArgs);
            }
            nodeId = System.console().readLine();


            List<String> validApprovalResponses = new ArrayList<String>(Arrays.asList(approvedResponses));

            do {
                System.console().printf("Do you wish to approve %s (Y=yes,N=no,C=cancel)\n", nodeId);
                approval = System.console().readLine();
                approval = approval.toUpperCase();
            } while (!validApprovalResponses.contains(approval));
            if (approval.equalsIgnoreCase("c")) {
                System.exit(0);
            }
        } while (approval.equals("N"));
        return nodeId;
    }

}
