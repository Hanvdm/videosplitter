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

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

public class Tool {
	
    private static Logger logger = Logger.getLogger(Tool.class);
	
    public static void placeCentered(Shell shell) {
        Point size = shell.computeSize(-1, -1);
        placeCentered(shell, size.x, size.y);
    }

    public static void placeCentered(Shell shell, int width, int height) {
        Rectangle screen = Display.getCurrent().getBounds();
        shell.setLocation((screen.width - width) / 2, (screen.height - height) / 2);
    }
    
    public static int warningBox(Shell shell, String message) {
        return messageBox(shell, "Warning", SWT.ICON_WARNING | SWT.OK, message);
    }

    public static int confirmBox(Shell shell, String message) {
        return messageBox(shell, "Confirmation", SWT.ICON_QUESTION | SWT.YES | SWT.NO, message);
    }
    
    public static int errorBox(Shell shell, Exception e) {
        return errorBox(shell, getMessage(e)); 
    }
    
    public static String getMessage(Exception e) {
        String msg = "";
        if (e.getCause() != null) {
                if (e.getCause().getMessage() != null) {
                        msg = e.getCause().getMessage();
                } else {
                        msg = e.getCause().toString();
                }
        } else {
                if (e.getMessage() != null) {
                        msg = e.getMessage();
                } else {
                        msg = e.toString();
                }
        }
        return msg;
    }
    
    public static int errorBox(Shell shell, String message) {
        return messageBox(shell, "Error", SWT.OK | SWT.ICON_ERROR, message);
    }
    
    public static int messageBox(Shell shell, String title, int uiProps, String message) {
        MessageBox mb = new MessageBox(shell, uiProps);
        if(title != null && title.length() > 0) {
            mb.setText(title);
        }
        mb.setMessage(message);
        return mb.open();
    }

    public static int infoBox(Shell shell, String message) {
        return messageBox(shell, "Information", SWT.OK | SWT.ICON_INFORMATION, message);
    }
	
    public static void sleep(long src) {
    	try {
    		Thread.sleep(src);
    	} catch (Exception e) {
    	}
    }
    
	public static String xmlToString(Node node) {
	    String returnString = "";
	    try {
	        //create string from xml tree
	        //Output the XML
	        //set up a transformer
	        TransformerFactory transfac = TransformerFactory.newInstance();
	        Transformer trans;
	        trans = transfac.newTransformer();
	        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        trans.setOutputProperty(OutputKeys.INDENT, "yes");
	        StringWriter sw = new StringWriter();
	        StreamResult streamResult = new StreamResult(sw);
	        DOMSource source = new DOMSource(node);
	        trans.transform(source, streamResult);
	        String xmlString = sw.toString();
	        //print the XML
	        returnString = returnString + xmlString;
	    } catch (TransformerException ex) {
	    }
	    return returnString;
	}
    
	public static String replace(String src, String pattern, String replacement) {
		String tag = "";
		int begin = 0;
		int pos = 0;
		pos = src.indexOf(pattern, begin);
		while (pos >= 0) {
			tag += src.substring(begin, pos) + replacement;
			begin = pos + pattern.length();
			pos = src.indexOf(pattern, begin);
		}
		tag += src.substring(begin);
		return tag;
	}
		
}
