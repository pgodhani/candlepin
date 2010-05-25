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
package org.fedoraproject.candlepin.model;


import java.util.List;

import org.fedoraproject.candlepin.audit.Event;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;

/**
 * AttributeCurator
 */
public class EventCurator extends AbstractHibernateCurator<Event> {

    protected EventCurator() {
        super(Event.class);
    }

    /**
     * Query events, most recent first.
     * @return List of events.
     */
    public List<Event> listMostRecent(int limit) {
        Criteria crit = currentSession().createCriteria(Event.class);
        crit.setMaxResults(limit);
        crit.addOrder(Order.desc("timestamp"));
        return crit.list();
    }

}
