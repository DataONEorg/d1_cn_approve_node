/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dataone.cn.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.security.NoSuchAlgorithmException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;


import org.dataone.client.CNode;
import org.dataone.service.exceptions.*;
import org.dataone.service.cn.v1.CNCore;
import org.dataone.service.cn.v1.CNRead;
import org.dataone.service.types.v1.Node;
import org.dataone.service.types.v1.Session;
import org.dataone.service.util.D1Url;
import org.xml.sax.SAXException;
import org.dataone.service.cn.impl.v1.NodeRegistryService;
import org.dataone.service.types.v1.NodeReference;
import org.dataone.cn.hazelcast.ClientConfiguration;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import java.io.InputStream;
/**
 *
 * @author waltz
 */
public class NodeApproval {

    NodeRegistryService nodeRegistryService = new NodeRegistryService();
    private static String[] approvedResponses = {"Y", "N", "C"};
 
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
        nodeRegistryService.approveNode(approveNodeId);
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
        List<NodeReference> pendingNodeList = nodeRegistryService.getAllPendingNodeIds();
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
