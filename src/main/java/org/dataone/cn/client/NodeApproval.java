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
import org.dataone.service.cn.v2.impl.NodeRegistryServiceImpl;
import org.dataone.service.types.v1.NodeReference;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.ITopic;
import java.io.IOException;
import org.dataone.cn.hazelcast.HazelcastClientFactory;
import org.dataone.configuration.Settings;
import org.dataone.service.types.v2.Node;
import org.dataone.service.types.v2.NodeList;

/**
 * Set the node to approved in ldap and inform hazelcast of the change
 * 
 * @author waltz
 */
public class NodeApproval {

    NodeRegistryServiceImpl nodeRegistryService = new NodeRegistryServiceImpl();
    private static String[] approvedResponses = {"Y", "N", "C"};
    ITopic<NodeReference> hzNodeTopic = null;
    static final String hzNodeTopicName = Settings.getConfiguration().getString("dataone.hazelcast.nodeTopic");
    
    public NodeApproval() throws FileNotFoundException {
        HazelcastClient hzclient = HazelcastClientFactory.getProcessingClient();
        hzNodeTopic = hzclient.getTopic(hzNodeTopicName);
    }

    public void approveNode(String nodeId) throws ServiceFailure, NotFound, IOException, InterruptedException, NotImplemented {
        // If the nodeId is not provided, prompt from console
        if (nodeId == null) {
            nodeId = promptPendingList();
        }
        if (nodeId == null) {
            return;
        }
        NodeReference approveNodeId = new NodeReference();
        approveNodeId.setValue(nodeId);
        // Set the approval status to TRUE in LDAP

        nodeRegistryService.approveNode(approveNodeId);
        System.console().printf("Node Approved in LDAP\n");
       
        // XXX This is bad. setting the node approval should trigger the nodelist to recache
        // but I am making calls directly against nodeAccess instead of through
        // the NodeRegistryService
        // So, lets guess the refresh rate on nodeList is still 3 seconds
        // to Make certain the node can be retrieved by the scheduler of synchronization
        //
        Thread.sleep(3500L);

        hzNodeTopic.publish(approveNodeId);
        System.console().printf("Hazelcast Node Topic published to. Approval Complete\n");
        System.console().writer().flush();
        System.console().writer().close();
        System.console().reader().close();
        
    }

    private String promptPendingList() throws ServiceFailure, NotImplemented, NotFound {
        String approval = "N";
        String nodeId = null;
        NodeList pendingNodes = nodeRegistryService.listPendingNodes();
        List<Node> pendingNodeList = pendingNodes.getNodeList();
        //List<NodeReference> pendingNodeList = nodeAccess.getPendingNodeReferenceList();
        if (!pendingNodeList.isEmpty()) {
            System.console().printf("Pending Nodes to Approve\n");
            do {
                int rows = pendingNodeList.size() / 4;
                for (int i = 0; i < rows; ++i) {

                    System.console().printf("%d) %s\t%d) %s\t%d) %s\t%d) %s\n", 
                            ((i * 4) + 0), pendingNodeList.get((i * 4) + 0).getIdentifier().getValue(),
                            ((i * 4) + 1), pendingNodeList.get((i * 4) + 1).getIdentifier().getValue(),
                            ((i * 4) + 2), pendingNodeList.get((i * 4) + 2).getIdentifier().getValue(),
                            ((i * 4) + 3), pendingNodeList.get((i * 4) + 3).getIdentifier().getValue());
                }
                int remainder = pendingNodeList.size() % 4;

                if (remainder > 0) {
                    String[] consoleArgs = new String[remainder*2];
                    StringBuilder formatStringBuilder = new StringBuilder();
        
                    for (int i = 0, j = 0; i < remainder; i++, j = j +2) {
                        formatStringBuilder.append("%s) %s\t");
                        consoleArgs[j] = new Integer((rows * 4) + i).toString();
                        consoleArgs[j + 1] =  pendingNodeList.get((rows * 4) + i).getIdentifier().getValue();
                    }
                    formatStringBuilder.append("\n");
                    System.console().printf(formatStringBuilder.toString(), consoleArgs);
                }
                System.console().printf("Type the number of the Node to verify and press enter (return): \n");
                String nodeIdIndex = System.console().readLine();
                try {
                    int nodeListIndex = Integer.parseInt(nodeIdIndex);
                    nodeId = pendingNodeList.get(nodeListIndex).getIdentifier().getValue();
                    List<String> validApprovalResponses = new ArrayList<String>(Arrays.asList(approvedResponses));

                    do {
                        System.console().printf("Do you wish to approve %s (Y=yes,N=no,C=cancel)\n", nodeId);
                        approval = System.console().readLine();
                        approval = approval.toUpperCase();
                    } while (!validApprovalResponses.contains(approval));
                    if (approval.equalsIgnoreCase("c")) {
                        System.exit(0);
                    }
                } catch (NumberFormatException ex) {
                    System.console().printf(nodeIdIndex + " is not a valid number. Please try again.\n");
                } catch (IndexOutOfBoundsException ex) {
                    System.console().printf(nodeIdIndex + " is out of range Please select a number from 0 to " + (pendingNodeList.size() -1) + ". Please try again.\n");
                }
            } while (approval.equals("N"));
        } else {
            System.console().printf("There are No Pending Nodes to Approve\n");
        }
        return nodeId;
    }
}
