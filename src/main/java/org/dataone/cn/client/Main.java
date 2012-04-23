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

import java.io.File;
import java.io.FilenameFilter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import org.apache.log4j.Logger;
import org.dataone.client.auth.CertificateManager;
import org.dataone.configuration.Settings;

/**
 *
 * @author waltz
 */
public class Main {

    static Logger logger = Logger.getLogger(Main.class.getName());
    static FilenameFilter certificateFilter = new CertificateFilter();
    static {
       String clientCertificateDirectory =  Settings.getConfiguration().getString("D1Client.certificate.directory");
           
        File certsDirectory = new File(clientCertificateDirectory);

        if (certsDirectory.exists() && certsDirectory.isDirectory()) {
            File[] certificateFiles = certsDirectory.listFiles(certificateFilter);
            if (certificateFiles.length > 0) {
                String clientCertificateLocation = null;

                if (certificateFiles.length > 1) {
                    System.console().printf("Choose the number of the Certificate to use\n");
                    for (int i = 0; i < certificateFiles.length; ++i) {
                        System.console().printf("%d)\t%s\n", i, certificateFiles[i].getName());
                    }
                    String certSelection = System.console().readLine();
                    Integer certInteger;
                   try {
                        certInteger = Integer.parseInt(certSelection);
                    } catch(NumberFormatException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                    clientCertificateLocation = certificateFiles[certInteger].getAbsolutePath();
                } else {
                    clientCertificateLocation = certificateFiles[0].getAbsolutePath();
                }

                CertificateManager.getInstance().setCertificateLocation(clientCertificateLocation);
            }
        }



    }
    public static void main(String[] args) {
        try {
            String nodeId = null;
            NodeApproval nodeApproval = new NodeApproval();
// create the command line parser
            CommandLineParser parser = new PosixParser();

            // create the Options
            Options options = new Options();
            options.addOption("h", "help", false, "print options");
            options.addOption(OptionBuilder.withLongOpt("NodeId").withDescription("the id of the node to approve").hasArg().withType(String.class).withValueSeparator().withArgName("NODEID").create());
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("h") || line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("D1ApproveNode", options);
                return;
            }
            if (line.hasOption("NodeId")) {
                nodeId = line.getOptionValue("NodeId");
            } 

            nodeApproval.approveNode(nodeId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
