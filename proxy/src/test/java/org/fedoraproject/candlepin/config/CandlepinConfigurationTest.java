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
package org.fedoraproject.candlepin.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.junit.Test;

public class CandlepinConfigurationTest {

    @Test
    public void returnAllKeysWithAPrefixFromHead() {
        Config config = new CandlepinConfigurationForTesting(
                new HashMap<String, String>() {

                    /**
                     * 
                     */
                    private static final long serialVersionUID = 1L;

                    {
                        put("a.b.a.b", "value");
                        put("a.b.c.d", "value");
                        put("a.b.e.f", "value");
                        put("a.c.a.a", "value");
                    }
                });

        Map<String, String> withPrefix = config.configurationWithPrefix("a.b");
        assertEquals(3, withPrefix.size());
        assertTrue(withPrefix.containsKey("a.b.a.b"));
        assertTrue(withPrefix.containsKey("a.b.c.d"));
        assertTrue(withPrefix.containsKey("a.b.e.f"));
    }

    @SuppressWarnings("serial")
    @Test
    public void returnAllKeysWithAPrefixInTheMiddle() {
        Config config = new CandlepinConfigurationForTesting(
                new HashMap<String, String>() {

                    {
                        put("a.b.a.b", "value");
                        put("a.b.c.d", "value");
                        put("a.c.a.b", "value");
                        put("a.c.c.d", "value");
                        put("a.c.e.f", "value");
                        put("a.d.a.b", "value");
                    }
                });

        Map<String, String> withPrefix = config.configurationWithPrefix("a.c");
        assertEquals(3, withPrefix.size());
        assertTrue(withPrefix.containsKey("a.c.a.b"));
        assertTrue(withPrefix.containsKey("a.c.c.d"));
        assertTrue(withPrefix.containsKey("a.c.e.f"));
    }

    @SuppressWarnings("serial")
    @Test
    public void returnAllKeysWithAPrefixFromTail() {
        Config config = new CandlepinConfigurationForTesting(
                new HashMap<String, String>() {

                    {
                        put("a.b.a.b", "value");
                        put("a.b.c.d", "value");
                        put("a.c.a.b", "value");
                        put("a.c.c.d", "value");
                        put("a.c.e.f", "value");
                    }
                });

        Map<String, String> withPrefix = config.configurationWithPrefix("a.c");
        assertEquals(3, withPrefix.size());
        assertTrue(withPrefix.containsKey("a.c.a.b"));
        assertTrue(withPrefix.containsKey("a.c.c.d"));
        assertTrue(withPrefix.containsKey("a.c.e.f"));
    }

    @SuppressWarnings("serial")
    @Test
    public void returnStringArray() {
        Config config = new CandlepinConfigurationForTesting(
            new HashMap<String, String>() {

                    {
                        put("a.b.a.b", "1,2,3,4");
                    }
                });

        String[] value = config.getStringArray("a.b.a.b");
        assertEquals(4, value.length);
        assertEquals("1", value[0]);
        assertEquals("2", value[1]);
        assertEquals("3", value[2]);
        assertEquals("4", value[3]);
    }

    @SuppressWarnings("serial")
    @Test
    public void returnNamespaceProperties() {
        Config config = new CandlepinConfigurationForTesting(
                new HashMap<String, String>() {

                    {
                        put("a.b.a.b", "value1");
                        put("a.b.c.d", "value2");
                        put("a.c.a.b", "value3");
                        put("a.c.c.d", "value4");
                        put("a.c.e.f", "value5");
                    }
                });

        Properties withPrefix = config.getNamespaceProperties("a.c");
        assertEquals(3, withPrefix.size());
        assertTrue(withPrefix.containsKey("a.c.a.b"));
        assertTrue(withPrefix.containsKey("a.c.c.d"));
        assertTrue(withPrefix.containsKey("a.c.e.f"));
    }

    @SuppressWarnings("serial")
    @Test
    public void returnNamespacePropsWithDefaults() {
        Map<String, String> defaults = new HashMap<String, String>();
        defaults.put("a.c.a.b", "defaultvalue");
        defaults.put("a.c.not.e", "should have a value");
        defaults.put("not.here", "is.ignored");

        Config config = new CandlepinConfigurationForTesting(
            new HashMap<String, String>() {

                {
                    put("a.b.a.b", "value1");
                    put("a.b.c.d", "value2");
                    put("a.c.a.b", "value3");
                    put("a.c.c.d", "value4");
                    put("a.c.e.f", "value5");
                }
            });

        Properties withPrefix = config.getNamespaceProperties("a.c", defaults);
        assertEquals(4, withPrefix.size());
        assertTrue(withPrefix.containsKey("a.c.a.b"));
        assertTrue(withPrefix.containsKey("a.c.c.d"));
        assertTrue(withPrefix.containsKey("a.c.e.f"));
        assertTrue(withPrefix.containsKey("a.c.not.e"));
        assertEquals("value3", withPrefix.getProperty("a.c.a.b"));
        assertEquals("should have a value", withPrefix.getProperty("a.c.not.e"));
        assertFalse(withPrefix.containsKey("not.here"));
    }

    @SuppressWarnings("serial")
    @Test
    public void namespaceWithNull() {
        Map<String, String> defaults = new HashMap<String, String>();
        defaults.put(null, null);

        Config config = new CandlepinConfigurationForTesting(
            new HashMap<String, String>() {

                {
                    put("a.c.a.b", "value3");
                    put("a.c.c.d", "value4");
                    put("a.c.e.f", "value5");
                }
            });

        try {
            Properties withPrefix = config.getNamespaceProperties("a.c", defaults);
            assertEquals(3, withPrefix.size());
            assertTrue(withPrefix.containsKey("a.c.a.b"));
            assertTrue(withPrefix.containsKey("a.c.c.d"));
            assertTrue(withPrefix.containsKey("a.c.e.f"));
            assertEquals("value3", withPrefix.getProperty("a.c.a.b"));

            withPrefix = config.getNamespaceProperties("a.c", null);
            assertEquals(3, withPrefix.size());
            assertTrue(withPrefix.containsKey("a.c.a.b"));
            assertTrue(withPrefix.containsKey("a.c.c.d"));
            assertTrue(withPrefix.containsKey("a.c.e.f"));
            assertEquals("value3", withPrefix.getProperty("a.c.a.b"));
        }
        catch (NullPointerException npe) {
            fail("getNamespaceProperties didn't check for null");
        }

    }

    public static class CandlepinConfigurationForTesting extends Config {

        public CandlepinConfigurationForTesting(Map<String, String> inConfig) {
            configuration = new TreeMap<String, String>(inConfig);
        }
    }
}
