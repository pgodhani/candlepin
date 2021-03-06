/**
 * Copyright (c) 2009 - 2012 Red Hat, Inc.
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
package org.candlepin.controller;

import org.candlepin.model.Consumer;
import org.candlepin.model.Entitlement;
import org.candlepin.model.Environment;
import org.candlepin.model.Owner;
import org.candlepin.model.Pool;
import org.candlepin.model.PoolQuantity;
import org.candlepin.model.Subscription;
import org.candlepin.policy.EntitlementRefusedException;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *
 */
public interface PoolManager {

    // WARNING: Changing these will impact rules files in the wild.
    String DELETE_FLAG = "candlepin.delete_pool";

    Pool createPool(Pool p);

    /**
     * @param sub
     * @return the newly created Pool
     */
    List<Pool> createPoolsForSubscription(Subscription sub);

    /**
     * Cleanup entitlements and safely delete the given pool.
     *
     * @param pool
     */
    void deletePool(Pool pool);

    /**
     * Request an entitlement by pool..
     *
     * If the entitlement cannot be granted, null will be returned.
     *
     * TODO: Throw exception if entitlement not granted. Report why.
     *
     * @param consumer
     * consumer requesting to be entitled
     * @param pool
     * entitlement pool to consume from
     * @return Entitlement
     *
     * @throws EntitlementRefusedException if entitlement is refused
     */
    Entitlement entitleByPool(Consumer consumer, Pool pool, Integer quantity)
        throws EntitlementRefusedException;

    Entitlement ueberCertEntitlement(Consumer consumer, Pool pool,
        Integer quantity) throws EntitlementRefusedException;

    /**
     * Request an entitlement by product.
     *
     * If the entitlement cannot be granted, null will be returned.
     *
     * TODO: Throw exception if entitlement not granted. Report why.
     *
     * @param consumer
     * consumer requesting to be entitled
     * @param productIds
     * products to be entitled.
     * @param entitleDate specific date to entitle by.
     * @return Entitlement
     * @throws EntitlementRefusedException if entitlement is refused
     */
    List<Entitlement> entitleByProducts(Consumer consumer, String[] productIds,
        Date entitleDate)
        throws EntitlementRefusedException;

    List<PoolQuantity> getBestPools(Consumer consumer, String[] productIds,
        Date entitleDate, Owner owner, String serviceLevelOverride)
        throws EntitlementRefusedException;

    Pool find(String poolId);

    List<Pool> lookupBySubscriptionId(String id);

    Refresher getRefresher();
    Refresher getRefresher(boolean lazy);

    /**
     * @param e
     * @param ueberCertificate TODO
     */
    void regenerateCertificatesOf(Entitlement e, boolean ueberCertificate, boolean lazy);

    void regenerateCertificatesOf(Environment env, Set<String> contentIds, boolean lazy);

    void regenerateCertificatesOf(Set<String> affectedContent, boolean lazy);

    void regenerateCertificatesOf(String productId, boolean lazy);

    void regenerateEntitlementCertificates(Consumer consumer, boolean lazy);

    int revokeAllEntitlements(Consumer consumer);

    int removeAllEntitlements(Consumer consumer);

    void revokeEntitlement(Entitlement entitlement);

    Pool updatePoolQuantity(Pool pool, long adjust);

    Pool setPoolQuantity(Pool pool, long set);

    void regenerateDirtyEntitlements(List<Entitlement> entitlements);

    Entitlement adjustEntitlementQuantity(Consumer consumer, Entitlement entitlement,
        Integer quantity) throws EntitlementRefusedException;
}
