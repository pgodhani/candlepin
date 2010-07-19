/**
 * Copyright (c) 2009 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.fedoraproject.candlepin.sync;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.inject.Inject;

/**
 * Meta maps to meta.json in the export
 * 
 */
public class MetaExporter {
    private class Meta {
        private static final String VERSION = "0.0.0";
        
        public String getVersion() {
            return VERSION;
        }
        
        public Date getCreated() {
            return new Date();
        }
    }
    
    @Inject
    MetaExporter() {
    }

    void export(ObjectMapper mapper, Writer writer) throws IOException {
        mapper.writeValue(writer, new Meta());
    }

}