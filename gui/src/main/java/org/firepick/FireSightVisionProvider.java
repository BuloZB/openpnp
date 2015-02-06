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

//import java.awt.Color;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.List;
import java.net.URISyntaxException;
import java.net.URL;

//import javax.imageio.ImageIO;

import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.openpnp.gui.support.Wizard;
import org.openpnp.model.Rectangle;
//import org.openpnp.model.Configuration;
//import org.openpnp.model.Rectangle;
import org.openpnp.spi.Camera;
import org.openpnp.spi.VisionProvider;
import org.simpleframework.xml.Attribute;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;



public class FireSightVisionProvider implements VisionProvider {
    //private final static Logger logger = LoggerFactory.getLogger(OpenCvVisionProvider.class);

    // SimpleXML requires at least one attribute or element on a class before
    // it will recognize it.
    @Attribute(required = false)
    private String dummy;

    private Camera camera;
    
    String FireSightCmd = null;
    File FireSightBinDir = null;
    File FireSightDir = null;
    File FireSightExampleDir = null;
    
    public class matchTemplateResult {
    	public Rectangle coords;
    	public double angle;
    	public double corr;
    }
    
    public FireSightVisionProvider() {
    	
        //FireSight Executable command -> String
        URL url = FireSightVisionProvider.class.getResource("/bin/firesight.exe");
        try {
			File file = new File(url.toURI());
			FireSightCmd = file.toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        
        //FireSight Binary file directory -> File (dir path)
        url = FireSightVisionProvider.class.getResource("/bin");
        try {
			FireSightBinDir = new File(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        
        //FireSight Directory for CVEs -> File (dir path)
        url = FireSightVisionProvider.class.getResource("/firesight");
        try {
			FireSightDir = new File(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        
    }

    //https://github.com/firepick1/FireSight/wiki/firesight
    public JSONResult callFireSight(File imageInput, 
    		                          File imageOutput, 
    		                          File jsonInput, 
    		                          File jsonOutput,
    		                          List<String> params, File WorkingDir) {
    	//Example FireSight commandline:
    	//firesight -i img/duck.jpg -p json/resize.json -o target/resize.jpg -Dfx=0.25 -Dfy=0.5
    	
    	//Build the command --------------------------------
    	List<String> cmdLine = new ArrayList<String>();
    	cmdLine.add(FireSightCmd);
    	cmdLine.add("-i"); // image-file-path Optional input image.
    	cmdLine.add(imageInput.toString());
    	cmdLine.add("-o"); // output-image-path Optional output image path.
    	cmdLine.add(imageOutput.toString());
    	cmdLine.add("-p"); // JSON-pipeline-file-path  
    	cmdLine.add(jsonInput.toString());
    	for (int i=0;i<params.size();i++)
    	{
    		cmdLine.add("-D" + params.get(i)); // Define value for a pipeline parameter 
    	}
    	
    	//Build process and run --------------------------------
    	ProcessBuilder pb = new ProcessBuilder(cmdLine);
    	//pb.redirectErrorStream(true);
    	pb.directory(FireSightDir);
    	Process process = null;
		try {
			process = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Handle std output --------------------------------
		String jsonString = new String("");
		BufferedReader inStreamReader = new BufferedReader(new InputStreamReader(process.getInputStream())); 
		try {
			String line;
			while((line = inStreamReader.readLine()) != null){
				jsonString += line;
			}
            FileWriter fWriter = new FileWriter(jsonOutput);
			fWriter.write(jsonString);
	        fWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		JSONResult result = new JSONResult(jsonString);
		return result;
    }
    
    /**  Runs a FireSight MatchTemplate operation: https://github.com/firepick1/FireSight/wiki/op-matchTemplate
     *  OpenCV: http://docs.opencv.org/modules/imgproc/doc/object_detection.html#matchtemplate
     *  @param threshold:  If maxVal is below this value, then no matches will be reported. Default is 0.7.
     *  @param method: Default is 3 (CV_TM_CCOEFF_NORMED). Using an int instead of an enum because I'm lazy.
     *  - 0 CV_TM_SQDIFF
     *  - 1 CV_TM_SQDIFF_NORMED
     *  - 2 CV_TM_CCORR
     *  - 3 CV_TM_CCORR_NORMED (Default)
     *  - 4 CV_TM_CCOEFF
     *  - 5 CV_TM_CCOEFF_NORMED
     *  @param corr: Normalized recognition threshold in the interval [0,1]. Used to determine best match of 
     *    candidates. For CV_TM_CCOEFF, CV_TM_CCOEFF_NORMED, CV_TM_CCORR, and CV_TM_CCORR_NORMED methods, this 
     *    is a minimum threshold for positive recognition; for all other methods, it is a maximum threshold. 
     *    Default is 0.85.
     *  @param angle: Match template at specified angle in degrees */
    private List<matchTemplateResult> fireSightMatchTemplate(float threshold, 
    		                                                  File template, 
    		                                                  int method, 
    		                                                  float corr, 
    		                                                  float angle) {
        File matchTemplateImgInput   = new File(FireSightDir, "matchTemplate-input.png");
        File matchTemplateImgOutput  = new File(FireSightDir, "matchTemplate-output.png");
        File matchTemplateJsonInput  = new File(FireSightDir, "matchTemplate.json");
        File matchTemplateJsonOutput = new File(FireSightDir, "matchTemplate-output.json");
        List<String> params = new ArrayList<String>();
        String methodStr = new String();
        switch (method) {
        	case 0: methodStr = "CV_TM_SQDIFF";        break;
        	case 1: methodStr = "CV_TM_SQDIFF_NORMED"; break;
        	case 2: methodStr = "CV_TM_CCORR";         break;
        	default:
        	case 3: methodStr = "CV_TM_CCORR_NORMED";  break;
        	case 4: methodStr = "CV_TM_CCOEFF";        break;
        	case 5: methodStr = "CV_TM_CCOEFF_NORMED"; break;
        }
        params.add("template="  + template.toString() );
        params.add("corr="      + String.format("%.2f",corr) );
        params.add("threshold=" + String.format("%.2f",threshold) );
        params.add("method="    + methodStr);
        params.add("angle="     + String.format("%.2f",angle));
        params.add("output=current");

        //Write the image to file
        try {
            BufferedImage image_ = camera.capture();
			ImageIO.write(image_, "png", matchTemplateImgInput);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        JSONResult jsResult = callFireSight(matchTemplateImgInput, 
        								    matchTemplateImgOutput, 
        								    matchTemplateJsonInput, 
        								    matchTemplateJsonOutput,
        								    params, 
        								    FireSightDir);
    	//Now parse expected JSON result:
    	/*	{"match":{"rects":[
    		  {"x":211.0,"y":74.0,"width":71.0,"height":68.0,"angle":-0.0,"corr":"1"},
    		  {"x":123.0,"y":75.0,"width":71.0,"height":68.0,"angle":-0.0,"corr":"0.950502"}],
    		  "maxVal":"1","matches":2 },"s2":{} }
    	*/
    	int jsMatches = jsResult.get("match").get("matches").getInt(); 
    	List<matchTemplateResult> mtResult = new ArrayList<matchTemplateResult>();
    	for (int i=0;i<jsMatches;i++){
        	matchTemplateResult mtResulti = new matchTemplateResult();
        	mtResulti.coords.setX     ( jsResult.get("match").get("rects").get(i).get("x").getInt()     );
        	mtResulti.coords.setY     ( jsResult.get("match").get("rects").get(i).get("y").getInt()     );
        	mtResulti.coords.setWidth ( jsResult.get("match").get("rects").get(i).get("width").getInt() );
        	mtResulti.coords.setHeight( jsResult.get("match").get("rects").get(i).get("height").getInt());
        	mtResulti.angle  = jsResult.get("match").get("rects").get(i).get("angle").getDouble();
        	mtResulti.corr   = jsResult.get("match").get("rects").get(i).get("corr").getDouble();
    		mtResult.add(mtResulti);
    	}
    	return mtResult;
    }
    
    @Override
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    @Override
    public Wizard getConfigurationWizard() {
        return null;
    }

    @Override
    public Circle[] locateCircles(int roiX, int roiY, int roiWidth,
            int roiHeight, int coiX, int coiY, int minimumDiameter,
            int diameter, int maximumDiameter) throws Exception {

        return new Circle[] {new Circle(1, 1, 1) };
    }

    @Override
    public Point[] locateTemplateMatches(int roiX, int roiY, int roiWidth,
            int roiHeight, int coiX, int coiY, BufferedImage templateImage_)
            throws Exception 
    {
    	float threshold = (float) 0.7;
    	float angle     = 0;
    	float corr      = (float) 0.85;
    	int   method    = 3;
    	
        //Write the template image to file
    	File template  = new File(FireSightDir, "matchTemplate-template.png");
    	try {
			ImageIO.write(templateImage_, "png", template);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	//, Rectangle roi, File template, int xtol, int ytol
    	List<matchTemplateResult> mtResult = fireSightMatchTemplate(threshold, template, method, corr, angle);
    	Point[] points = new Point[mtResult.size()];
    	for (int i=0;i<mtResult.size();i++) {
        	points[i]=mtResult.get(i).coords.getCenter();;
        }
    	return points;
    }


}
