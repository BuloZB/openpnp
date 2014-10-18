/*
 	Copyright (C) 2013 Richard Spelling <openpnp@chebacco.com>
 	
 	This file is part of OpenPnP.
 	
	OpenPnP is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OpenPnP is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OpenPnP.  If not, see <http://www.gnu.org/licenses/>.
 	
 	For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.machine.zippy;

import org.openpnp.machine.reference.ReferenceActuator;
import org.openpnp.model.Location;

public class ZippyActuator extends ReferenceActuator {
    @Override
    public void moveToSafeZ(double speed) throws Exception {
        logger.debug("{}.moveToSafeZ({})", new Object[] { getId(), speed } );
        Location l = new Location(getLocation().getUnits(), Double.NaN, Double.NaN, 10, Double.NaN);
        driver.moveTo(this, l, speed);
        machine.fireMachineHeadActivity(head);
    }
}
