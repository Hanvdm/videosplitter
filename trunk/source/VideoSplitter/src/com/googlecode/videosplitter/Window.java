/*
 *  Video Splitter
 * 
 *  Copyright (c) 2014 Ted Hive <tedhive@gmail.com>
 *  
 *  Developed from mp4splitter [ https://code.google.com/p/mp4splitter/ ]
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.googlecode.videosplitter;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Window {

    private static Logger logger = Logger.getLogger(Window.class);

	private Shell shell;
	private Text textFolder;
	private Text textVideo;
	private Text textTime;
	
	public Window() {
		shell = new Shell(Display.getDefault(), SWT.CLOSE | SWT.MIN);
		decorate();		
	}

	protected void decorate() {
		FormData fd;
		Button button;
		Label label;
		Text text;
		
		shell.setSize(400, 250);
		Tool.placeCentered(shell, 400, 250);
		shell.setText(" Video Splitter ");
		shell.setImage(Images.get("Icon.16x16"));
		shell.setLayout(new FormLayout());

		label = new Label(shell, SWT.NONE);
		label.setText("Folder to save");
		fd = new FormData();
		fd.left = new FormAttachment(0, 10);
		fd.top = new FormAttachment(0, 10);
		label.setLayoutData(fd);
		
		text = new Text(shell, SWT.BORDER);
		textFolder = text;
		fd = new FormData();
		fd.left = new FormAttachment(0, 10);
		fd.top = new FormAttachment(0, 30);
		fd.right = new FormAttachment(100, - 10 - 50 - 10);
		text.setLayoutData(fd);

        button = new Button(shell, SWT.PUSH);
        button.setText("Browse");
		fd = new FormData();
		fd.top = new FormAttachment(0, 28);
		fd.right = new FormAttachment(100, - 10);
		fd.width = 50;
		button.setLayoutData(fd);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	try {
            		DirectoryDialog dlg = new DirectoryDialog(shell, SWT.SAVE);
            		dlg.setMessage("Select folder to save video file in");
            		String folder = dlg.open();
            		if (folder != null) {
            			if (new java.io.File(folder).exists()) {
            				textFolder.setText(folder);
            			}
            		}
            	} catch (Exception ex) {
            		logger.error("", ex);
            		Tool.errorBox(shell, ex);
            	}
            }
        });

		label = new Label(shell, SWT.NONE);
		label.setText("Video file to split");
		fd = new FormData();
		fd.left = new FormAttachment(0, 10);
		fd.top = new FormAttachment(0, 60);
		label.setLayoutData(fd);
		
		text = new Text(shell, SWT.BORDER);
		textVideo = text;
		fd = new FormData();
		fd.left = new FormAttachment(0, 10);
		fd.top = new FormAttachment(0, 80);
		fd.right = new FormAttachment(100, - 10 - 50 - 10);
		text.setLayoutData(fd);
        
        button = new Button(shell, SWT.PUSH);
        button.setText("Browse");
		fd = new FormData();
		fd.top = new FormAttachment(0, 78);
		fd.right = new FormAttachment(100, - 10);
		fd.width = 50;
		button.setLayoutData(fd);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	try {
            		FileDialog dlg = new FileDialog(shell, SWT.OPEN);
            		dlg.setFilterNames(new String[] { "MPEG Files (*.mp4)" });
            		dlg.setFilterExtensions(new String[] { "*.mp4" });
            		String filename = dlg.open();
            		if (filename != null) {
            			if (new java.io.File(filename).exists()) {
            				textVideo.setText(filename);
            			}
            		}
            	} catch (Exception ex) {
            		logger.error("", ex);
            		Tool.errorBox(shell, ex);
            	}
            }
        });

		label = new Label(shell, SWT.NONE);
		label.setText("Splitted video time in second");
		fd = new FormData();
		fd.left = new FormAttachment(0, 10);
		fd.top = new FormAttachment(0, 110);
		label.setLayoutData(fd);
		
		text = new Text(shell, SWT.BORDER);
		textTime = text;
		fd = new FormData();
		fd.left = new FormAttachment(0, 10);
		fd.top = new FormAttachment(0, 130);
		fd.right = new FormAttachment(100, - 10 - 50 - 10);
		text.setLayoutData(fd);
		text.setText("600");
        
		label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
		fd = new FormData();
		fd.bottom = new FormAttachment(100, -35);
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		label.setLayoutData(fd);

        button = new Button(shell, SWT.PUSH);
        button.setText("Close");
		fd = new FormData();
		fd.bottom = new FormAttachment(100, -5);
		fd.right = new FormAttachment(100, -5);
		fd.width = 75;
		button.setLayoutData(fd);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	try {
            		shell.close();
            	} catch (Exception ex) {
            		logger.error("", ex);
            		Tool.errorBox(shell, ex);
            	}
            }
        });

        button = new Button(shell, SWT.PUSH);
        button.setText("Split");
		fd = new FormData();
		fd.bottom = new FormAttachment(100, -5);
		fd.right = new FormAttachment(100, -5 - 85);
		fd.width = 75;
		button.setLayoutData(fd);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	try {
            		String folder = textFolder.getText();
            		if (!(new java.io.File(folder).exists())) {
            			Tool.errorBox(shell, "Folder to save videos does not exist");
            			return;
            		}
            		String filename = textVideo.getText();
            		if (!(new java.io.File(filename).exists())) {
            			Tool.errorBox(shell, "Video file does not exist");
            			return;
            		}
            		String name = new File(filename).getName();
            		String ext = "";
            		int pos = name.lastIndexOf(".");
            		if (pos >= 0) {
            			ext = name.substring(pos + 1);
            		}
            		if (".mp4.".indexOf(ext) < 0) {
            			Tool.errorBox(shell, "Unsupported video file format");
            			return;
            		}
            		long time = 0;
            		try {
            			time = Long.parseLong(textTime.getText());
            			if (time <= 0) {
                			Tool.errorBox(shell, "Invalid video time");
                			return;
            			}
            		} catch (Exception ex) {
            			Tool.errorBox(shell, "Invalid video time");
            			return;
            		}

					MonitorDialog dlg = new MonitorDialog(shell);
		    		dlg.run(true, true, new SplitRunable(filename, folder, time));
		    		
		    		Tool.infoBox(shell, "Splitting completed");
            	} catch (Exception ex) {
            		logger.error("", ex);
            		Tool.errorBox(shell, ex);
            	}
            }
        });
        
	}
    
	public void open() {
		shell.open();
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) shell.getDisplay().sleep();
		}
	}
	
    public class SplitRunable implements IRunnableWithProgress {
    	
    	private String videoFile;
    	private String tagDir;
    	private long time;
    	
    	public SplitRunable(String videoFile, String tagDir, long time) {
    		super();
    		this.videoFile = videoFile;
    		this.tagDir = tagDir;
    		this.time = time;
    	}

    	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    		try {
    			new Splitter(monitor, videoFile, tagDir, time).run();
    		} catch (Exception e) {
    			throw new InvocationTargetException(e);
    		}
    	}
    }
	
    public class MonitorDialog extends ProgressMonitorDialog {
    	public MonitorDialog(Shell parent) {
    		super(parent);
    	}
    	
    	protected void createButtonsForButtonBar(Composite parent) {
    		Button button;
    		FormData fd;
    		
    		parent.setLayout(new FormLayout());

    		button = new Button(parent, SWT.PUSH);
    		cancel = button;
    		button.setText(IDialogConstants.CANCEL_LABEL);
            button.setVisible(true);
            fd = new FormData();
            fd.top = new FormAttachment(0, 0);
            fd.right = new FormAttachment(100, 0);
            fd.width = 75;
            button.setLayoutData(fd);

            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                	cancelPressed();
                }
            });
    	}
    }
	
}
