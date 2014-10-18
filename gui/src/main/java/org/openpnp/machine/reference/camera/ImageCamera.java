/*
 	Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
 	
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

package org.openpnp.machine.reference.camera;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeSupport;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Action;

import org.openpnp.CameraListener;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.gui.wizards.CameraConfigurationWizard;
import org.openpnp.machine.reference.ReferenceCamera;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.PropertySheetHolder;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.core.Commit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageCamera extends ReferenceCamera implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(ImageCamera.class);
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	@Attribute(required=false)
	private int fps = 24;
	
	@Element
	private String sourceUri;
	
	@Attribute
	private int width = 640;
	
	@Attribute
	private int height = 480;
	    
	private BufferedImage source;
	
	private Thread thread;
	
	public ImageCamera() {
	    unitsPerPixel = new Location(LengthUnit.Inches, 0.031, 0.031, 0, 0);
	}
	
	@SuppressWarnings("unused")
	@Commit
	private void commit() throws Exception {
	    setSourceUri(sourceUri);
	}
	
	@Override
	public synchronized void startContinuousCapture(CameraListener listener, int maximumFps) {
		start();
		super.startContinuousCapture(listener, maximumFps);
	}
	
	@Override
	public synchronized void stopContinuousCapture(CameraListener listener) {
		super.stopContinuousCapture(listener);
		if (listeners.size() == 0) {
			stop();
		}
	}
	
	private synchronized void stop() {
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
			try {
				thread.join();
			}
			catch (Exception e) {
				
			}
			thread = null;
		}
	}
	
	private synchronized void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	public String getSourceUri() {
		return sourceUri;
	}

	public void setSourceUri(String sourceUri) throws Exception {
		String oldValue = this.sourceUri;
		this.sourceUri = sourceUri;
		pcs.firePropertyChange("sourceUri", oldValue, sourceUri);
		initialize();
	}
	
	@Override
	public synchronized BufferedImage capture() {
        /*
         * Create a buffer that we will render the center tile and it's
         * surrounding tiles to. 
         */
        BufferedImage frame = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB);

        Graphics gFrame = frame.getGraphics();
        
        Location location = getLocation();
        double x = -location.getX();
        double y = location.getY();

        x /= getUnitsPerPixel().getX();
        y /= getUnitsPerPixel().getY();
        
        gFrame.drawImage(source, (int) (x + (width / 2)), (int) (y + (height / 2) - source.getHeight()), null);
        
        gFrame.dispose();
        
	    return frame;
	}
	
    private synchronized void initialize() throws Exception {
        stop();
        
        source = ImageIO.read(new URL(sourceUri));

        if (listeners.size() > 0) {
            start();
        }
    }
	
	
	public void run() {
		while (!Thread.interrupted()) {
			BufferedImage frame = capture();
			broadcastCapture(frame);
			try {
				Thread.sleep(1000 / fps);
			}
			catch (InterruptedException e) {
				return;
			}
		}
	}
	
	@Override
	public Wizard getConfigurationWizard() {
		return null;
	}
	
    @Override
    public String getPropertySheetHolderTitle() {
        return getClass().getSimpleName() + " " + getId();
    }

    @Override
    public PropertySheetHolder[] getChildPropertySheetHolders() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PropertySheet[] getPropertySheets() {
        return new PropertySheet[] {
                new PropertySheetWizardAdapter(new CameraConfigurationWizard(this)),
                new PropertySheetWizardAdapter(getConfigurationWizard())
        };
    }
	
    @Override
    public Action[] getPropertySheetHolderActions() {
        // TODO Auto-generated method stub
        return null;
    }
}
