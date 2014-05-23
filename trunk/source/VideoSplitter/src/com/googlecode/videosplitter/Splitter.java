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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import com.googlecode.videosplitter.mpeg.Mp4Split;
import com.googlecode.videosplitter.mpeg.atom.Atom;
import com.googlecode.videosplitter.mpeg.atom.AtomException;
import com.googlecode.videosplitter.mpeg.atom.ContainerAtom;
import com.googlecode.videosplitter.mpeg.atom.DefaultAtomVisitor;
import com.googlecode.videosplitter.mpeg.atom.FtypAtom;
import com.googlecode.videosplitter.mpeg.atom.LeafAtom;
import com.googlecode.videosplitter.mpeg.atom.MdatAtom;
import com.googlecode.videosplitter.mpeg.atom.MoovAtom;
import com.googlecode.videosplitter.mpeg.atom.TrakAtom;

public class Splitter {

    private static Logger logger = Logger.getLogger(Splitter.class);
	
    private IProgressMonitor monitor;
	private String videoFile;
	private String tagDir;
	private long time;
	private String title;
	private String ext;

	public Splitter(IProgressMonitor monitor, String videoFile, String tagDir, long time) {
    	this.monitor = monitor;
		this.videoFile = videoFile;
		this.tagDir = tagDir;
		this.time = time;
		title = new File(videoFile).getName();
		ext = "";
		int pos = title.lastIndexOf(".");
		if (pos >= 0) {
			ext = title.substring(pos + 1);
			title = title.substring(0, pos);
		}
	}
	
    public void run() throws Exception {
    	monitor.beginTask("Start splitting video [ " + title + " ]", 1);

    	if ("mp4".equalsIgnoreCase(ext)) {
    		splitMPEG();
    	}
    	
    	monitor.worked(1);
    }

    private String no2Str(long no) {
    	String tag = "";
    	if (no <= 9) {
    		tag = "00" + no;
    	} else if (no <= 99) {
    		tag = "0" + no;
    	} else {
    		tag = "" + no;
    	}
    	return tag;
    }
    
    private void splitMPEG() throws Exception {
        long fileNo = 1;

        while (true) {
        	if (monitor.isCanceled()) {
        		throw new Exception("Splitting is cancelled");
        	}

        	DataInputStream mp4file = new DataInputStream(new FileInputStream(videoFile));
        	MPEGVisitor visitor = new MPEGVisitor(mp4file);
            FtypAtom ftyp = (FtypAtom) visitor.parseAtom();
            MoovAtom moov = (MoovAtom) visitor.parseAtom();
            MdatAtom mdat = (MdatAtom) visitor.parseAtom();
            long duration = moov.getMvhd().getDurationNormalized();
            long curtime = time;
            
            if (fileNo == 1) {
            	long count = ((duration - (duration % time)) / time) + 1;
            	monitor.beginTask("Splitting MPEG video [ " + title + " ]", (int)count);
            }
        	
        	File outputFile = new File(tagDir, title + "." + no2Str(fileNo) + "." + ext);

            MoovAtom cutMoov = moov.cut((fileNo - 1) * time, time);
            MoovAtom cutMoov2 = moov.cut(fileNo * time);
            MoovAtom cutMoov3 = moov.cut(duration);
            
            long datSize = 0;
            if (cutMoov2.firstDataByteOffset() == moov.firstDataByteOffset()) {
            	datSize = cutMoov3.firstDataByteOffset() - cutMoov.firstDataByteOffset();
            	curtime = duration - (fileNo - 1) * time;
            } else {
            	datSize = cutMoov2.firstDataByteOffset() - cutMoov.firstDataByteOffset();
            }
            mp4file.close();

        	mp4file = new DataInputStream(new FileInputStream(videoFile));
        	visitor = new MPEGVisitor(mp4file);
            ftyp = (FtypAtom) visitor.parseAtom();
            moov = (MoovAtom) visitor.parseAtom();
            mdat = (MdatAtom) visitor.parseAtom();
            cutMoov = moov.cut((fileNo - 1) * time, curtime);
            
            long mdatSkip = cutMoov.firstDataByteOffset() - moov.firstDataByteOffset();
            MdatAtom cutMdat = mdat.cut(mdatSkip);
            long updateAmount = mdatSkip + (moov.size() - cutMoov.size());
            cutMoov.fixupOffsets(-updateAmount);
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile));
            ftyp.writeData(dos);
            cutMoov.writeData(dos);
            cutMdat.setSize(datSize + cutMdat.ATOM_HEADER_SIZE);
            cutMdat.writeData(dos);
            dos.close();
            mp4file.close();

            long percentage = (fileNo * time * 100) / duration;
			monitor.subTask("".concat(Long.toString(percentage).concat(" % splitted")));
			monitor.worked(1);
            
            if (fileNo * time > duration) {
            	break;
            }
            fileNo++;
        }
    }
        
    private class MPEGVisitor extends DefaultAtomVisitor {
    	
    	private DataInputStream mp4file;
    	
    	public MPEGVisitor(DataInputStream mp4file) {
    		this.mp4file = mp4file;
    	}
    	
    	@Override
    	protected void defaultAction(Atom atom) throws AtomException {
    		if (atom.isContainer()) {
    			long bytesRead = 0;
    			long bytesToRead = atom.dataSize();
    			while (bytesRead < bytesToRead) {
    				Atom child = parseAtom();
    				((ContainerAtom)atom).addChild(child);
    				bytesRead += child.size();
    			}
    		} else {
    			// the default action for a leaf is to read the data in to a buffer
    			((LeafAtom)atom).readData(mp4file);
    		}
    	}

    	/**
    	  * Don't the the mdat atom since that's the biggest segment of the 
    	  * file.  It contains the video and sound data.  Plus, we'll just
    	  * skip over the beginning when we cut the movie.
    	  */
    	@Override
    	public void visit(MdatAtom atom) throws AtomException {
    		atom.setInputStream(mp4file);
    	}
    	
        public Atom parseAtom() throws AtomException {
        	// get the atom size
            byte[] word = new byte[Atom.ATOM_WORD];
            int num;
            try {
            	num = mp4file.read(word);
            } catch (IOException e1) {
            	throw new AtomException("IOException while reading file");
            }
            // check for end of file
            if (num == -1) {
            	return null;
            }
            if (num != Atom.ATOM_WORD) {
            	throw new AtomException("Unable to read enough bytes for atom");
            }
            long size = Atom.byteArrayToUnsignedInt(word, 0);
            // get the atom type
            try {
            	num = mp4file.read(word);
            } catch (IOException e1) {
            	throw new AtomException("IOException while reading file");
            }
            if (num != Atom.ATOM_WORD) {
            	throw new AtomException("Unable to read enough bytes for atom");  
            }
            try {
            	Class<?> cls = Class.forName(Atom.typeToClassName(word));
            	Atom atom = (Atom) cls.newInstance();
            	atom.setSize(size);
            	atom.accept(this);
            	return atom;
            } catch (ClassNotFoundException e) {
            	throw new AtomException("Class not found");
            } catch (InstantiationException e) {
            	throw new AtomException("Unable to instantiate atom");
            } catch (IllegalAccessException e) {
            	throw new AtomException("Unabel to access atom object");
            }
        }
    	
    }
}
