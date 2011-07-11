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
package org.fedoraproject.candlepin.policy.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

//import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.fedoraproject.candlepin.controller.PoolManager;
import org.fedoraproject.candlepin.model.Pool;
import org.fedoraproject.candlepin.model.Product;
import org.fedoraproject.candlepin.model.ProvidedProduct;
import org.fedoraproject.candlepin.model.Subscription;
import org.fedoraproject.candlepin.policy.js.pool.PoolHelper;
import org.fedoraproject.candlepin.service.ProductServiceAdapter;
import org.fedoraproject.candlepin.test.TestUtil;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * PoolHelperTest
 */
public class PoolHelperTest {

    private Pool pool;
    private Subscription sub;
    private Product product;
    private PoolManager pm;
    private ProductServiceAdapter psa;

    @Before
    public void init() {
        pool = mock(Pool.class);
        sub = mock(Subscription.class);
        product = mock(Product.class);
        pm = mock(PoolManager.class);
        psa = mock(ProductServiceAdapter.class);

        // default to an empty list, override in the test
        when(pool.getProvidedProducts()).thenReturn(Collections.EMPTY_SET);
        when(sub.getProvidedProducts()).thenReturn(Collections.EMPTY_SET);
    }

    @Test
    public void productsDidnotChange() {
        when(pool.getProductId()).thenReturn("prodid123");
        when(pool.getProductName()).thenReturn("Awesome Product");
        when(product.getId()).thenReturn("prodid123");
        when(product.getName()).thenReturn("Awesome Product");
        when(sub.getProduct()).thenReturn(product);

        PoolHelper ph = new PoolHelper(pm, psa, null);
        assertFalse(ph.checkForChangedProducts(pool, sub));
    }

    @Test
    public void nameChanged() {
        when(pool.getProductId()).thenReturn("prodid123");
        when(pool.getProductName()).thenReturn("Awesome Product");
        when(product.getId()).thenReturn("prodid123");
        when(product.getName()).thenReturn("Awesome Product Changed");
        when(sub.getProduct()).thenReturn(product);

        PoolHelper ph = new PoolHelper(pm, psa, null);
        assertTrue(ph.checkForChangedProducts(pool, sub));
    }

    @Ignore
    @Test
    public void productIdDifferent() {
        when(pool.getProductId()).thenReturn("prodid123");
        when(pool.getProductName()).thenReturn("Awesome Product");
        when(product.getId()).thenReturn("prodidnew");
        when(product.getName()).thenReturn("Awesome Product");
        when(sub.getProduct()).thenReturn(product);

        PoolHelper ph = new PoolHelper(pm, psa, null);
        assertTrue(ph.checkForChangedProducts(pool, sub));
    }

    @Ignore
    @Test
    public void attributeChanged() {
        when(pool.getProductId()).thenReturn("prodid123");
        when(pool.getProductName()).thenReturn("Awesome Product");
        when(product.getId()).thenReturn("prodidnew");
        when(product.getName()).thenReturn("Awesome Product");
        when(sub.getProduct()).thenReturn(product);

        Set<ProvidedProduct> poolprods = new HashSet<ProvidedProduct>();
       // ProvidedProduct pp = mock(ProvidedProduct.class);
        ProvidedProduct pp = new ProvidedProduct("productid", "Awesome Product");
        pp.setId(pp.getProductId());
        //when(pp.getId()).thenReturn("productid");
        System.out.println(pp.hashCode());
        System.out.println(pp.getId());
        poolprods.add(pp);
        Set<Product> subprods = new HashSet<Product>();
       // Product p = mock(Product.class);
        Product p = new Product("productid", "Awesome Product");
        //when(p.getId()).thenReturn("productid");
        System.out.println(p.hashCode());
        System.out.println(p.getId());
        subprods.add(p);
        when(pool.getProvidedProducts()).thenReturn(poolprods);
        when(sub.getProvidedProducts()).thenReturn(subprods);

        SetView view = Sets.difference(poolprods, subprods);
        System.out.println(view.toString());
        PoolHelper ph = new PoolHelper(pm, psa, null);
        assertTrue(ph.checkForChangedProducts(pool, sub));
    }
    
    @Test
    public void collapseAttributesOntoPoolAddsNewAttribute() {
        Product targetProduct = TestUtil.createProduct();
        targetProduct.getAttributes().clear();
        targetProduct.setAttribute("A1", "V1");
        targetProduct.setAttribute("A2", "V2");
        Subscription sourceSub = TestUtil.createSubscription(targetProduct);
        Pool targetPool = TestUtil.createPool(targetProduct);
        
        PoolHelper ph = new PoolHelper(pm, psa, null);
        assertTrue("Update expected.", ph.collapseAttributesOntoPool(sourceSub,
            targetPool));
        assertEquals(2, targetPool.getProductProvidedAttributes().size());
        assertTrue(targetPool.hasProductProvidedAttribute("A1"));
        assertTrue(targetPool.hasProductProvidedAttribute("A2"));
    }
    
    @Test
    public void collapseAttributesOntoPoolUpdatesExistingAttribute() {
        Product targetProduct = TestUtil.createProduct();
        targetProduct.getAttributes().clear();
        targetProduct.setAttribute("A1", "V-updated");
        Subscription sourceSub = TestUtil.createSubscription(targetProduct);
        
        Pool targetPool = TestUtil.createPool(targetProduct);
        targetPool.setProductProvidedAttribute("A1", "V1", targetProduct.getId());
        
        PoolHelper ph = new PoolHelper(pm, psa, null);
        assertTrue("Update expected.", ph.collapseAttributesOntoPool(sourceSub,
            targetPool));
        assertEquals(1, targetPool.getProductProvidedAttributes().size());
        assertTrue(targetPool.hasProductProvidedAttribute("A1"));
        assertEquals("V-updated", targetPool.getProductProvidedAttribute("A1").getValue());
    }
    
    @Test
    public void collapseAttributesOntoPoolRemovesNonExistingAttribute() {
        Product targetProduct = TestUtil.createProduct();
        targetProduct.getAttributes().clear();
        Subscription sourceSub = TestUtil.createSubscription(targetProduct);
        Pool targetPool = TestUtil.createPool(targetProduct);
        
        targetPool.setProductProvidedAttribute("A1", "V1", targetProduct.getId());
        
        PoolHelper ph = new PoolHelper(pm, psa, null);
        assertTrue("Update expected.", ph.collapseAttributesOntoPool(sourceSub,
            targetPool));
        assertTrue(targetPool.getProductProvidedAttributes().isEmpty());
    }
    
}