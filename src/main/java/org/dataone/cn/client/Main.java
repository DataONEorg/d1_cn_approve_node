/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dataone.cn.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import org.apache.log4j.Logger;
import org.dataone.client.auth.CertificateManager;

/**
 *
 * @author waltz
 */
public class Main {

    static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            String nodeId = null;
            String clientCertificateLocation = null;
            NodeApproval nodeApproval = new NodeApproval();
// create the command line parser
            CommandLineParser parser = new PosixParser();

            // create the Options
            Options options = new Options();
            options.addOption("h", "help", false, "print options");
            options.addOption(OptionBuilder.withLongOpt("NodeId").withDescription("the id of the node to approve").hasArg().withType(String.class).withValueSeparator().withArgName("NODEID").create());
            options.addOption(OptionBuilder.withLongOpt("Certificate").withDescription("fullpath + filename of client certificate to use for authentication").hasArg().withType(String.class).withValueSeparator().withArgName("CERTIFICATE").create());
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("h") || line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("D1ApproveNode", options);
                return;
            }
            if (line.hasOption("Certificate")) {
                clientCertificateLocation = line.getOptionValue("Certificate");
            } else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("D1ApproveNode", options);
                return;
            }
            if (line.hasOption("NodeId")) {
                nodeId = line.getOptionValue("NodeId");
            } 

            CertificateManager.getInstance().setCertificateLocation(clientCertificateLocation);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
