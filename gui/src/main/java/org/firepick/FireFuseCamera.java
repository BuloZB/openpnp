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

package org.firepick;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.gui.wizards.CameraConfigurationWizard;
import org.openpnp.machine.reference.ReferenceCamera;
import org.openpnp.model.Configuration;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.spi.PropertySheetHolder.PropertySheet;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.core.Commit;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeSupport;
import java.lang.ref.SoftReference;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Action;

import java.io.File;


/**
 * A Camera implementation based on the OpenCV FrameGrabbers.
 */
public class FireFuseCamera extends ReferenceCamera implements Runnable {
	//private final static Logger logger = LoggerFactory.getLogger(TableScannerCamera.class);
	
	//ATTRIBUTES AND ELEMENTS ---------------------------------------------------
	//@Attribute(required=true)
	//private int deviceIndex = 0;

	@Attribute(required=false)
	private int fps = 1;

	@Element(required=true)
	private String sourceUri;

	private Thread thread;
	private File cacheDirectory;
	private File file;
	private URL sourceUrl;
	private SoftReference<BufferedImage> image;

	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);


	//constructor
	public FireFuseCamera() {
	}
	
	@Commit //XML serializer callback for configuration settings.  http://simple.sourceforge.net/download/stream/doc/javadoc/org/simpleframework/xml/core/Commit.html
	private void commit() {
		try {
			setSourceUri(sourceUri);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setSourceUri(String sourceUri) throws Exception {
		String oldValue = this.sourceUri;
		this.sourceUri = sourceUri;
		pcs.firePropertyChange("sourceUri", oldValue, sourceUri);
		// TODO: Move to start() so simply setting a property doesn't sometimes
		// blow up.
		initialize();
	}

	private synchronized void initialize() throws Exception {
		stop();
		sourceUrl = new URL(sourceUri);
		cacheDirectory = new File(Configuration.get().getResourceDirectory(getClass()), DigestUtils.shaHex(sourceUri));
		if (!cacheDirectory.exists()) {
			cacheDirectory.mkdirs();
		}
		file = new File(cacheDirectory, "camera.jpg");
		start();
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

	//This kicks off the thread
	public void run() {
		while (!Thread.interrupted()) {
			try {
				BufferedImage image = capture();
				if (image != null) {
					broadcastCapture(image); // I think this is in abstractCamera.java, which probably sends it out to listeners? -NJ
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000 / fps);
			}
			catch (InterruptedException e) {
				break;
			}
		}
	}

	@Override
	public synchronized BufferedImage capture() {
		try {
			return getImage();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public synchronized BufferedImage getImage() {
		try {
			URL imageUrl = new URL(sourceUrl, file.getName());
			//logger.debug("Attempting to download {}", imageUrl.toString());
			FileUtils.copyURLToFile(imageUrl, file);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		try {
			image = new SoftReference<BufferedImage>(ImageIO.read(file));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return image.get();
	}

	public String getSourceUri() {
		return sourceUri;
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
