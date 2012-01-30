/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dataone.cn.client;

import java.io.FilenameFilter;
import java.io.File;


/**
 *
 * @author waltz
 */
public class CertificateFilter implements FilenameFilter {


    public boolean accept(File file, String filename) {

        String extension = Utils.getExtension(filename);
        if (extension != null) {
            if ((extension.equals(Utils.pem)) ||  (extension.equals(Utils.crt))) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }


    static class Utils {
        public final static String pem = "pem";
        public final static String crt = "crt";

        public static String getExtension(String filename){

            String ext = null;

            int i = filename.lastIndexOf('.');
            if (i > 0 && i < filename.length() - 1){
                ext = filename.substring(i+1).toLowerCase();
            }
            return ext;

        }
    }

}
