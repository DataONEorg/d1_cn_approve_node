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
