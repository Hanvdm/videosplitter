/**
 * 
 */
package com.googlecode.videosplitter.mpeg.atom;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * The movie atom is a top-level atom.  It contains the metadata for a presentation.
 */
public class MoovAtom extends ContainerAtom {
	
  // the movie header atom
  private MvhdAtom mvhd;
  // initial object descriptor 
  private IodsAtom iods;
  // the user data atom
  private UdtaAtom udta;
  // the list of tracks
  private List<TrakAtom> traks;
  
  /**
   * Constructor for movie atom
   */
  public MoovAtom() {
    super(new byte[]{'m','o','o','v'});
  }
  
  /**
   * Copy constructor for movie atom.  Performs a deep copy.
   * @param old the movie atom to copy
   */
  public MoovAtom(MoovAtom old) {
    super(old);
    mvhd = new MvhdAtom(old.mvhd);
    if (iods != null) {
      iods = new IodsAtom(old.iods);
    }
    if (udta != null) {
      udta = new UdtaAtom(old.udta);
    }
    traks = new LinkedList<TrakAtom>();
    for (Iterator<TrakAtom> i = old.getTracks(); i.hasNext(); ) {
      traks.add(new TrakAtom(i.next()));
    }
  }
  
  /**
   * Return the movie header atom
   * @return the movie header atom
   */
  public MvhdAtom getMvhd() {
    return mvhd;
  }
  
  /**
   * Set the movie header atom
   * @param mvhd the new movie header atom
   */
  public void setMvhd(MvhdAtom mvhd) {
    this.mvhd = mvhd;
  }
  
  /**
   * Return the initial object descriptor atom
   * @return the initial object descriptor atom
   */
  public IodsAtom getIods() {
    return iods;
  }
  
  /**
   * Set the initial object descriptor atom
   * @param iods the new initial object descriptor
   */
  public void setIods(IodsAtom iods) {
    this.iods = iods;
  }
  
  /**
   * Return the user data atom
   * @return the user data atom
   */
  public UdtaAtom getUdta() {
    return udta;
  }
  
  /**
   * Set the user-data atom
   * @param udta the new user-data atom.
   */
  public void setUdta(UdtaAtom udta) {
    this.udta = udta;
  }
  
  /**
   * Return an iterator with the media's tracks.  For most movies, there are two tracks, the sound 
   * track and the video track.
   * @return an iterator with the movie traks.
   */
  public Iterator<TrakAtom> getTracks() {
    return traks.iterator();
  }
  
  /**
   * Add a track to the movie.  If no tracks have been added, then allocate
   * space for the tracks.
   * @param trak a new track.
   */
  public void addTrack(TrakAtom trak) {
    if (this.traks == null) {
      this.traks = new LinkedList<TrakAtom>();
    }
    this.traks.add(trak);
  }
  
  /**
   * Add a child atom to the moov atom.  If the atom is not recognized as a child of moov
   * then a run-time exception is thrown.
   * @param atom the atom to add
   */
  @Override
  public void addChild(Atom atom) {
    if (atom instanceof MvhdAtom) {
      mvhd = (MvhdAtom) atom;
    }
    else if (atom instanceof IodsAtom) {
      iods = (IodsAtom) atom;
    }
    else if (atom instanceof UdtaAtom) {
      udta = (UdtaAtom) atom;
    }
    else if (atom instanceof TrakAtom) {
      if (traks == null) {
        traks = new LinkedList<TrakAtom>();
      }
      traks.add((TrakAtom) atom);
    }
    else {
      throw new AtomError("Can't add " + atom + " to moov");
    }
  }
  
  /**
   * Recompute the size of the moov atom, which needs to be done if
   * any of the child atom sizes have changed.
   */
  @Override
  protected void recomputeSize() {
    long newSize = mvhd.size();
    for (Iterator<TrakAtom> i = getTracks(); i.hasNext(); ) {
      newSize += i.next().size();
    }
    if (iods != null) {
      newSize += iods.size();
    }
    if (udta != null) {
      newSize += udta.size();
    }
    setSize(ATOM_HEADER_SIZE + newSize);
  }
  
  /**
   * Cut the movie atom at the specified time
   * @param time the time at which the cut is performed.  Must be converted to movie time.
   * @return the new movie atom
   */
  public MoovAtom cut(float time) {
    long movieTimeScale = mvhd.getTimeScale();
    long duration = mvhd.getDuration();
    
    System.out.println("DBG: Movie time " + (duration/movieTimeScale) + " sec, cut at " + time + "sec");
    System.out.println("\tDBG: ts " + movieTimeScale + " cut at " + (time * movieTimeScale));
    
    MoovAtom cutMoov = new MoovAtom();
    cutMoov.setMvhd(mvhd.cut());
    if (iods != null) {
      cutMoov.setIods(iods.cut());
    }
    if (udta != null) {
      cutMoov.setUdta(udta.cut());
    }
    long minDuration = Long.MAX_VALUE;
    // iterate over each track and cut the track
    for (Iterator<TrakAtom> i = getTracks(); i.hasNext(); ) {
      TrakAtom cutTrak = i.next().cut(time, movieTimeScale);
      cutMoov.addTrack(cutTrak);
      // need to convert the media time-scale to the movie time-scale
      long cutDuration = cutTrak.convertDuration(movieTimeScale);
      System.out.println("DBG: cutDuration " + cutDuration);
      cutTrak.fixupDuration(cutDuration);
      if (cutDuration > cutMoov.getMvhd().getDuration()) {
        cutMoov.getMvhd().setDuration(cutDuration);
      }
      if (cutDuration < minDuration) {
        minDuration = cutDuration;
      }
      time = (duration - cutDuration) / (float) movieTimeScale;
      System.out.println("DBG: new time " + time);
    }
    // check if any edits need to be added 
/*    for (Iterator<TrakAtom> i = cutMoov.getTracks(); i.hasNext(); ) { 
      TrakAtom trak = i.next();
      long trakDuration = trak.convertDuration(movieTimeScale);
      System.out.println("DBG: trak duration " + trakDuration);
      if (trakDuration > minDuration) {
        long editDuration = trak.convertToMediaScale(trakDuration - minDuration, movieTimeScale);
        System.out.println("\tDBG: edit duration " + editDuration);
        // add an edit to the media
        trak.addEdit(editDuration);
        trak.recomputeSize();
      }
    }
*/    cutMoov.recomputeSize();
    return cutMoov;
  }

  public MoovAtom cut(float time, long curtime) {
	    long movieTimeScale = mvhd.getTimeScale();
	    long duration = mvhd.getDuration();
	    
	    MoovAtom cutMoov = new MoovAtom();
	    cutMoov.setMvhd(mvhd.cut());
	    if (iods != null) {
	    	cutMoov.setIods(iods.cut());
	    }
	    if (udta != null) {
	    	cutMoov.setUdta(udta.cut());
	    }
	    long minDuration = Long.MAX_VALUE;
	    // iterate over each track and cut the track
	    for (Iterator<TrakAtom> i = getTracks(); i.hasNext(); ) {
	    	TrakAtom cutTrak = i.next().cut(time, movieTimeScale, curtime);
	    	cutMoov.addTrack(cutTrak);
	    	// need to convert the media time-scale to the movie time-scale
	    	long cutDuration = cutTrak.convertDuration(movieTimeScale);
	    	cutTrak.fixupDuration(cutDuration);
	    	if (cutDuration > cutMoov.getMvhd().getDuration()) {
	    		cutMoov.getMvhd().setDuration(cutDuration);
	    	}
	    	if (cutDuration < minDuration) {
	    		minDuration = cutDuration;
	    	}
	    }
	    cutMoov.recomputeSize();
	    return cutMoov;	  
  }
  
  /**
   */
  public long findCommonTime(float time) {
    long adjustedTime = 0;
    for (Iterator<TrakAtom> i = getTracks(); i.hasNext(); ) {
      TrakAtom trak = i.next();
      StblAtom stbl = trak.getMdia().getMinf().getStbl();
      long mediaTimeScale = trak.getMdia().getMdhd().getTimeScale();
      long mediaTime = (long)(time * mediaTimeScale);
      if (stbl.getStss() != null) {
        long sampleNum = stbl.getStts().timeToSample(mediaTime);
        System.out.println("DBG: sampleNum " + sampleNum);
        sampleNum = stbl.getStss().getKeyFrame(sampleNum);
        System.out.println("DBG: new key frame " + sampleNum);
        mediaTime = stbl.getStts().sampleToTime(sampleNum);
      }
      System.out.println("DBG: track " + trak.getTkhd().getTrackId() +
          " spec time " + (long)(time * mediaTimeScale) + " adj time " + mediaTime +
          " spec time sec " + (long)(time) + " adj time sec " + (mediaTime/mediaTimeScale));
    }
    return adjustedTime;
  }
  
  /**
   * Return the byte offset of the first data in the mdat atom.  
   * This is computed by looking at the first entry in the stco atom,
   * which contains mdat offset values.  This method returns the smallest
   * value of any of the tracks.
   * @return the byte offset of the first data.
   */
  public long firstDataByteOffset() {
    long offset = Long.MAX_VALUE;
    for (Iterator<TrakAtom> i = getTracks(); i.hasNext(); ) {
      StcoAtom stco = i.next().getMdia().getMinf().getStbl().getStco();
      if (stco.getChunkOffset(1) < offset) {
        offset = stco.getChunkOffset(1);
      }
    }
    return offset;
  }
  
  @Override
  public void accept(AtomVisitor v) throws AtomException {
    v.visit(this);
  }

  /**
   * Write the moov atom data to the specified output
   * @param out where the data goes
   * @throws IOException if there is an error writing the data
   */
  @Override
  public void writeData(DataOutput out) throws IOException {
    writeHeader(out);
    mvhd.writeData(out);
    if (iods != null) {
      iods.writeData(out);
    }
    for (Iterator<TrakAtom> i = getTracks(); i.hasNext(); ) {
      i.next().writeData(out);
    }
    if (udta != null) {
      udta.writeData(out);
    }
  }
  
  /**
   * Update the fixed offset values in the atom.  This needs to be done if
   * the file contents change.
   * @param delta the change in file size
   */
  public void fixupOffsets(long delta) {
    for (Iterator<TrakAtom> i = getTracks(); i.hasNext(); ) {
      i.next().fixupOffsets(delta);
    }
  }
    
}