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
package org.fedoraproject.candlepin.model.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.persistence.PersistenceException;

import org.fedoraproject.candlepin.model.Consumer;
import org.fedoraproject.candlepin.model.ConsumerFacts;
import org.fedoraproject.candlepin.model.ConsumerType;
import org.fedoraproject.candlepin.model.Entitlement;
import org.fedoraproject.candlepin.model.Owner;
import org.fedoraproject.candlepin.model.Pool;
import org.fedoraproject.candlepin.model.Product;
import org.fedoraproject.candlepin.test.DatabaseTestFixture;
import org.fedoraproject.candlepin.test.TestUtil;
import org.junit.Before;
import org.junit.Test;

public class ConsumerTest extends DatabaseTestFixture {

    private Owner owner;
    private Product rhel;
    private Product jboss;
    private Consumer consumer;
    private ConsumerType consumerType;
    private static final String CONSUMER_TYPE_NAME = "test-consumer-type";
    private static final String CONSUMER_NAME = "Test Consumer";

    @Before
    public void setUpTestObjects() {
        unitOfWork.beginWork();

        owner = new Owner("Example Corporation");
        rhel = new Product("rhel", "Red Hat Enterprise Linux");
        jboss = new Product("jboss", "JBoss");

        ownerCurator.create(owner);
        productCurator.create(rhel);
        productCurator.create(jboss);

        consumerType = new ConsumerType(CONSUMER_TYPE_NAME);
        consumerTypeCurator.create(consumerType);
        consumer = new Consumer(CONSUMER_NAME, owner, consumerType);
        consumer.setMetadataField("foo", "bar");
        consumer.setMetadataField("foo1", "bar1");

        consumerCurator.create(consumer);

        unitOfWork.endWork();
    }

    @Test(expected = PersistenceException.class)
    public void testConsumerTypeRequired() {
        Consumer newConsumer = new Consumer();
        newConsumer.setName("cname");
        newConsumer.setOwner(owner);

        consumerCurator.create(newConsumer);
    }

    @Test
    public void testLookup() throws Exception {
        Consumer lookedUp = consumerCurator.find(consumer.getId());
        assertEquals(consumer.getId(), lookedUp.getId());
        assertEquals(consumer.getName(), lookedUp.getName());
        assertEquals(consumer.getType().getLabel(), lookedUp.getType()
                .getLabel());
        assertNotNull(consumer.getUuid());
    }

    @Test
    public void testInfo() {
        Consumer lookedUp = consumerCurator.find(consumer.getId());
        Map<String, String> metadata = lookedUp.getFacts().getMetadata();
        assertEquals(2, metadata.keySet().size());
        assertEquals("bar", metadata.get("foo"));
        assertEquals("bar", lookedUp.getFacts().getFact("foo"));
        assertEquals("bar1", metadata.get("foo1"));
        assertEquals("bar1", lookedUp.getFacts().getFact("foo1"));
    }

    @Test
    public void testMetadataInfo() {
        Consumer consumer2 = new Consumer("consumer2", owner, consumerType);
        consumer2.setMetadataField("foo", "bar2");
        consumerCurator.create(consumer2);

        Consumer lookedUp = consumerCurator.find(consumer.getId());
        Map<String, String> metadata = lookedUp.getFacts().getMetadata();
        assertEquals(2, metadata.keySet().size());
        assertEquals("bar", metadata.get("foo"));
        assertEquals("bar", lookedUp.getFacts().getFact("foo"));
        assertEquals("bar1", metadata.get("foo1"));
        assertEquals("bar1", lookedUp.getFacts().getFact("foo1"));
        assertEquals(consumer.getId(), lookedUp.getFacts().getConsumer()
                .getId());

        Consumer lookedUp2 = consumerCurator.find(consumer2.getId());
        metadata = lookedUp2.getFacts().getMetadata();
        assertEquals(1, metadata.keySet().size());
        assertEquals("bar2", metadata.get("foo"));
    }

    @Test
    public void testModifyMetadata() {
        consumer.setMetadataField("foo", "notbar");
        consumerCurator.merge(consumer);

        Consumer lookedUp = consumerCurator.find(consumer.getId());
        assertEquals("notbar", lookedUp.getMetadataField("foo"));
    }

    @Test
    public void testMetadataDeleteCascading() {
        Consumer attachedConsumer = consumerCurator.find(consumer.getId());
        Long consumerInfoId = attachedConsumer.getFacts().getId();

        assertNotNull((ConsumerFacts) entityManager().find(ConsumerFacts.class,
                consumerInfoId));

        consumerCurator.delete(attachedConsumer);

        assertNull((ConsumerFacts) entityManager().find(ConsumerFacts.class,
                consumerInfoId));
    }

    @Test
    public void testRemoveConsumedProducts() {
        consumerCurator.delete(consumerCurator.find(consumer.getId()));
        assertNull(consumerCurator.find(consumer.getId()));
    }

    @Test
    public void testLookupByUuidNonExistent() {
        //Consumer lookedUp = 
        consumerCurator.lookupByUuid("this is not a uuid!");
    }

    @Test
    public void testLookupByUuid() {
        Consumer consumer2 = new Consumer("consumer2", owner, consumerType);
        consumerCurator.create(consumer2);

        Consumer lookedUp = consumerCurator.lookupByUuid(consumer2.getUuid());
        assertEquals(lookedUp.getUuid(), consumer2.getUuid());
    }

    @Test
    public void testConsumerHierarchy() {
        Consumer child1 = new Consumer("child1", owner, consumerType);
        child1.setMetadataField("foo", "bar");
        consumerCurator.create(child1);

        Consumer child2 = new Consumer("child2", owner, consumerType);
        child2.setMetadataField("foo", "bar");
        consumerCurator.create(child2);

        consumer.addChildConsumer(child1);
        consumer.addChildConsumer(child2);
        consumerCurator.merge(consumer);

        Consumer lookedUp = consumerCurator.find(consumer.getId());
        assertEquals(2, lookedUp.getChildConsumers().size());
    }

    @Test
    public void testChildDeleteNoCascade() {
        unitOfWork.beginWork();

        Consumer child1 = new Consumer("child1", owner, consumerType);
        child1.setMetadataField("foo", "bar");
        consumer.addChildConsumer(child1);
        consumerCurator.create(child1);
        consumerCurator.merge(consumer);

        unitOfWork.endWork();
        unitOfWork.beginWork();

        child1 = consumerCurator.find(child1.getId());
        assertNotNull(child1);
        consumerCurator.delete(child1);

        assertNull(consumerCurator.find(child1.getId()));

        Consumer lookedUp = consumerCurator.find(consumer.getId());
        assertEquals(0, lookedUp.getChildConsumers().size());
        unitOfWork.endWork();
    }

    @Test
    public void testParentDeleteCascadesToChildren() {
        Consumer child1 = new Consumer("child1", owner, consumerType);
        child1.setMetadataField("foo", "bar");
        consumer.addChildConsumer(child1);
        consumerCurator.create(child1);
        consumerCurator.merge(consumer);

        consumerCurator.delete(consumer);

        assertNull(consumerCurator.find(consumer.getId()));
        assertNull(consumerCurator.find(child1.getId()));
    }

    @Test
    public void testAddEntitlements() {
        Product newProduct = TestUtil.createProduct();
        productCurator.create(newProduct);
        Pool pool = TestUtil.createEntitlementPool(newProduct);
        entityManager().persist(pool.getOwner());
        entityManager().persist(pool);

        Entitlement e1 = TestUtil.createEntitlement(pool, consumer);
        Entitlement e2 = TestUtil.createEntitlement(pool, consumer);
        Entitlement e3 = TestUtil.createEntitlement(pool, consumer);
        entityManager().persist(e1);
        entityManager().persist(e2);
        entityManager().persist(e3);

        consumer.addEntitlement(e1);
        consumer.addEntitlement(e2);
        consumer.addEntitlement(e3);
        consumerCurator.merge(consumer);

        Consumer lookedUp = consumerCurator.find(consumer.getId());
        assertEquals(3, lookedUp.getEntitlements().size());
    }
    
    @Test
    public void testCreateWithAKnownUUID() {
        Consumer con1 = new Consumer();
        con1.setUuid("Jar Jar Binks");
        Consumer con2 = new Consumer(con1, owner, consumerType);
        assertEquals("The UUIDs should be equal", con1.getUuid(), con2.getUuid());
    }
    
    @Test
    public void testCreateWithABlankUUID() {
        Consumer con1 = new Consumer();
        con1.setUuid("");
        Consumer con2 = new Consumer(con1, owner, consumerType);
        assertNotSame("The UUIDs should not be equal", con1.getUuid(), con2.getUuid());
        assertTrue("The should be big", con2.getUuid().length() > 0);        
    }


}
