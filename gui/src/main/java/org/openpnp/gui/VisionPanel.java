package org.openpnp.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import java.awt.FlowLayout;

import javax.swing.JButton;

import java.awt.GridBagLayout;

import javax.swing.JTextField;

import java.awt.Insets;

import javax.swing.JTextArea;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.codec.digest.DigestUtils;
import org.firepick.FireSightVisionProvider;
import org.openpnp.model.Configuration;
import org.openpnp.spi.Camera;


//TODO: Investigate syntax highlighting using https://github.com/bobbylight/RSyntaxTextArea

public class VisionPanel extends JPanel {
	private JTextField txtCameraImageName;
	private JTextField txtOutputImageName;
	private JTextField txtJsonPipelineName;
	private JTextField txtJsonOutputName;
	private JTextField txtWorkingDirectory;
	private JTextArea  txtJsonPipeline;
	private JTextArea  txtJsonOutput;

	private File workingDir   = null;

	public VisionPanel(){
		//TODO: Use file.createTempFile();
		try {
			workingDir = Configuration.get().getResourceDirectory(getClass());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 323, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblWorkingDirectory = new JLabel("Working Directory");
		GridBagConstraints gbc_lblWorkingDirectory = new GridBagConstraints();
		gbc_lblWorkingDirectory.anchor = GridBagConstraints.EAST;
		gbc_lblWorkingDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_lblWorkingDirectory.gridx = 0;
		gbc_lblWorkingDirectory.gridy = 0;
		add(lblWorkingDirectory, gbc_lblWorkingDirectory);
		
		txtWorkingDirectory = new JTextField();
		txtWorkingDirectory.setText(workingDir.toString());
		GridBagConstraints gbc_txtWorkingDirectory = new GridBagConstraints();
		gbc_txtWorkingDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_txtWorkingDirectory.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtWorkingDirectory.gridx = 1;
		gbc_txtWorkingDirectory.gridy = 0;
		add(txtWorkingDirectory, gbc_txtWorkingDirectory);
		txtWorkingDirectory.setColumns(10);
		
		JButton btnBrowse = new JButton("Browse...");
		GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
		gbc_btnBrowse.fill = GridBagConstraints.BOTH;
		gbc_btnBrowse.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowse.gridx = 2;
		gbc_btnBrowse.gridy = 0;
		add(btnBrowse, gbc_btnBrowse);
		
		JLabel lblCameraImageName = new JLabel("Camera Image Name");
		GridBagConstraints gbc_lblCameraImageName = new GridBagConstraints();
		gbc_lblCameraImageName.insets = new Insets(0, 0, 5, 5);
		gbc_lblCameraImageName.anchor = GridBagConstraints.EAST;
		gbc_lblCameraImageName.gridx = 0;
		gbc_lblCameraImageName.gridy = 1;
		add(lblCameraImageName, gbc_lblCameraImageName);
		
		txtCameraImageName = new JTextField();
		txtCameraImageName.setText("camera.png");
		GridBagConstraints gbc_txtCameraImageName = new GridBagConstraints();
		gbc_txtCameraImageName.insets = new Insets(0, 0, 5, 5);
		gbc_txtCameraImageName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtCameraImageName.gridx = 1;
		gbc_txtCameraImageName.gridy = 1;
		add(txtCameraImageName, gbc_txtCameraImageName);
		txtCameraImageName.setColumns(10);
		
		JLabel lblOutputImagePath = new JLabel("Output Image Name");
		GridBagConstraints gbc_lblOutputImagePath = new GridBagConstraints();
		gbc_lblOutputImagePath.anchor = GridBagConstraints.EAST;
		gbc_lblOutputImagePath.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutputImagePath.gridx = 0;
		gbc_lblOutputImagePath.gridy = 2;
		add(lblOutputImagePath, gbc_lblOutputImagePath);
		
		txtOutputImageName = new JTextField();
		txtOutputImageName.setText("output.jpg");
		GridBagConstraints gbc_txtOutputImageName = new GridBagConstraints();
		gbc_txtOutputImageName.insets = new Insets(0, 0, 5, 5);
		gbc_txtOutputImageName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtOutputImageName.gridx = 1;
		gbc_txtOutputImageName.gridy = 2;
		add(txtOutputImageName, gbc_txtOutputImageName);
		txtOutputImageName.setColumns(10);
		
		JLabel lblJsonPipelinePath = new JLabel("JSON Pipeline Name");
		GridBagConstraints gbc_lblJsonPipelinePath = new GridBagConstraints();
		gbc_lblJsonPipelinePath.anchor = GridBagConstraints.EAST;
		gbc_lblJsonPipelinePath.insets = new Insets(0, 0, 5, 5);
		gbc_lblJsonPipelinePath.gridx = 0;
		gbc_lblJsonPipelinePath.gridy = 3;
		add(lblJsonPipelinePath, gbc_lblJsonPipelinePath);
		
		txtJsonPipelineName = new JTextField();
		txtJsonPipelineName.setText("pipeline.json");
		GridBagConstraints gbc_txtJsonPipelineName = new GridBagConstraints();
		gbc_txtJsonPipelineName.insets = new Insets(0, 0, 5, 5);
		gbc_txtJsonPipelineName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtJsonPipelineName.gridx = 1;
		gbc_txtJsonPipelineName.gridy = 3;
		add(txtJsonPipelineName, gbc_txtJsonPipelineName);
		txtJsonPipelineName.setColumns(10);
		
		JLabel lblJsonOutputhPath = new JLabel("JSON Output Name");
		GridBagConstraints gbc_lblJsonOutputhPath = new GridBagConstraints();
		gbc_lblJsonOutputhPath.anchor = GridBagConstraints.EAST;
		gbc_lblJsonOutputhPath.insets = new Insets(0, 0, 5, 5);
		gbc_lblJsonOutputhPath.gridx = 0;
		gbc_lblJsonOutputhPath.gridy = 4;
		add(lblJsonOutputhPath, gbc_lblJsonOutputhPath);
		
		txtJsonOutputName = new JTextField();
		txtJsonOutputName.setText("output.json");
		GridBagConstraints gbc_txtJsonOutputName = new GridBagConstraints();
		gbc_txtJsonOutputName.insets = new Insets(0, 0, 5, 5);
		gbc_txtJsonOutputName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtJsonOutputName.gridx = 1;
		gbc_txtJsonOutputName.gridy = 4;
		add(txtJsonOutputName, gbc_txtJsonOutputName);
		txtJsonOutputName.setColumns(10);
		
		JLabel lblJsonPipeline = new JLabel("JSON Pipeline");
		GridBagConstraints gbc_lblJsonPipeline = new GridBagConstraints();
		gbc_lblJsonPipeline.insets = new Insets(0, 0, 5, 5);
		gbc_lblJsonPipeline.gridx = 0;
		gbc_lblJsonPipeline.gridy = 5;
		add(lblJsonPipeline, gbc_lblJsonPipeline);
		
		txtJsonPipeline = new JTextArea();
		txtJsonPipeline.setText("[{\"op\":\"resize\", \"fx\":0.5, \"fy\":0.5}]");
		GridBagConstraints gbc_txtJsonPipeline = new GridBagConstraints();
		gbc_txtJsonPipeline.insets = new Insets(0, 0, 5, 5);
		gbc_txtJsonPipeline.fill = GridBagConstraints.BOTH;
		gbc_txtJsonPipeline.gridx = 1;
		gbc_txtJsonPipeline.gridy = 5;
		add(txtJsonPipeline, gbc_txtJsonPipeline);
		
		JButton btnRunCv = new JButton("Execute FireSight Pipeline");
		btnRunCv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchFireSight();
			}
		});
		GridBagConstraints gbc_btnRunCv = new GridBagConstraints();
		gbc_btnRunCv.fill = GridBagConstraints.VERTICAL;
		gbc_btnRunCv.insets = new Insets(0, 0, 5, 0);
		gbc_btnRunCv.gridx = 2;
		gbc_btnRunCv.gridy = 5;
		add(btnRunCv, gbc_btnRunCv);
		
		JLabel lblJsonOutput = new JLabel("JSON Output");
		GridBagConstraints gbc_lblJsonOutput = new GridBagConstraints();
		gbc_lblJsonOutput.insets = new Insets(0, 0, 5, 5);
		gbc_lblJsonOutput.gridx = 0;
		gbc_lblJsonOutput.gridy = 6;
		add(lblJsonOutput, gbc_lblJsonOutput);
		
		txtJsonOutput = new JTextArea();
		GridBagConstraints gbc_txtJsonOutput = new GridBagConstraints();
		gbc_txtJsonOutput.insets = new Insets(0, 0, 5, 5);
		gbc_txtJsonOutput.fill = GridBagConstraints.BOTH;
		gbc_txtJsonOutput.gridx = 1;
		gbc_txtJsonOutput.gridy = 6;
		add(txtJsonOutput, gbc_txtJsonOutput);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = gbc.weighty = 1.0;

	}
	
	private void launchFireSight(){
		Camera camera = MainFrame.cameraPanel.getSelectedCamera();
		FireSightVisionProvider visionProvider = (FireSightVisionProvider)camera.getVisionProvider();

		File imageInput   = new File(workingDir,txtCameraImageName.getText());
		File imageOutput  = new File(workingDir,txtOutputImageName.getText());
		File jsonPipeline = new File(workingDir,txtJsonPipelineName.getText());
		File jsonOutput   = new File(workingDir,txtJsonOutputName.getText());

		//Write the image to file
        try {
            BufferedImage image_ = camera.capture();
			ImageIO.write(image_, "png", imageInput);
		} catch (IOException e) {
			e.printStackTrace();
		}

        //Write JSON pipeline to file
        FileWriter fWriter;
		try {
			String sanitized = txtJsonPipeline.getText().replaceAll("\\r\\n|\\n|\\r", "");
			fWriter = new FileWriter(jsonPipeline);
			fWriter.write(sanitized);
	        fWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<String> params = new ArrayList<String>(); //empty

		//Execute the pipeline!
		visionProvider.callFireSight(imageInput,imageOutput,jsonPipeline,jsonOutput,params,workingDir);
		
		//Now write the JSON output data back into the text area 
		try {
			String content = new Scanner(jsonOutput).useDelimiter("\\Z").next();
			txtJsonOutput.setText(content);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
