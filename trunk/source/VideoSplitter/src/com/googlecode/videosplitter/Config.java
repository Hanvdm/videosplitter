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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Config {

    private static Logger logger = Logger.getLogger(Config.class);
	
    public static final boolean BOOLEAN_DEFAULT = false;
    public static final double DOUBLE_DEFAULT = 0.0;
    public static final float FLOAT_DEFAULT = 0.0f;
    public static final int INT_DEFAULT = 0;
    public static final long LONG_DEFAULT = 0L;
    public static final byte BYTE_DEFAULT = 0;
    public static final String STRING_DEFAULT = "";
    public static final String PASSWORD_DEFAULT = "";
	
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
	private final static String KEY = "968AD25B96915DD9267110A56E37D838";
	
    public static final String PORT = "port";
    
    private Properties properties;
    private String filename;

    private static Config instance;
    private static Config buffer;
    
    public static Config instance() {
    	if (instance == null) {
    		instance = new Config(new File(System.getProperty("user.dir"), "cfg/config.properties").getAbsolutePath());
    		instance.load();
    	}
    	return instance;
    }
    
    public static Config buffer() {
    	if (buffer == null) {
    		buffer = new Config();
    	}
    	return buffer;
    }
    
    public Config(String filename) {
    	this.filename = filename;
    	properties = new Properties();
    }
    
    public Config() {
    	properties = new Properties();
    }

    public void setFilename(String filename) {
    	this.filename = filename;
    }
    
    public String getFilename() {
    	return filename;
    }
    
    public boolean getBoolean(String name) {
        String value = properties.getProperty(name);
        if (value == null || value.length() == 0) return BOOLEAN_DEFAULT;
        if (value.equals(Config.TRUE)) return true;
        return false;
    }

    public boolean getBoolean(String name, boolean defVal) {
        String value = properties.getProperty(name);
        if (value == null || value.length() == 0) return defVal;
        if (value.equals(Config.TRUE)) return true;
        return false;
    }
    
    public double getDouble(String name) {
        String value = properties.getProperty(name);
        if (value == null || value.length() == 0) return DOUBLE_DEFAULT;
        double ival = DOUBLE_DEFAULT;
        try {
            ival = new Double(value).doubleValue();
        } catch (Exception e) {
            ival = DOUBLE_DEFAULT;
        }
        return ival;
    }

    public float getFloat(String name) {
        String value = properties.getProperty(name);
        if (value == null || value.length() == 0) return FLOAT_DEFAULT;
        float ival = FLOAT_DEFAULT;
        try {
            ival = new Float(value).floatValue();
        } catch (Exception e) {
            ival = FLOAT_DEFAULT;
        }
        return ival;
    }

    public int getInt(String name) {
        String value = properties.getProperty(name);
        if (value == null || value.length() == 0) return INT_DEFAULT;
        int ival = INT_DEFAULT;
        try {
            ival = Integer.parseInt(value);
        } catch (Exception e) {
            ival = INT_DEFAULT;
        }
        return ival;
    }

    public byte getByte(String name) {
        String value = properties.getProperty(name);
        if (value == null || value.length() == 0) return BYTE_DEFAULT;
        byte ival = BYTE_DEFAULT;
        try {
        	ival = Byte.parseByte(value);
        } catch (Exception e) {
            ival = BYTE_DEFAULT;
        }
        return ival;
    }
    
    public long getLong(String name) {
        String value = properties.getProperty(name);
        if (value == null || value.length() == 0) return LONG_DEFAULT;
        long ival = LONG_DEFAULT;
        try {
            ival = Long.parseLong(value);
        } catch (Exception e) {
            ival = LONG_DEFAULT;
        }
        return ival;
    }
    
    public String getString(String name) {
        String value = properties.getProperty(name);
        if (value == null) return STRING_DEFAULT;
        return value;
    }
    
    public String getPassword(String name) {
    	String src = getString(name);
    	String tag = PASSWORD_DEFAULT;
    	try {
    		tag = Crypto.decrypt(src, KEY);
    	} catch (Exception e) {
    		logger.error("", e);
    		tag = PASSWORD_DEFAULT;
    	}
    	return tag;
    }
    
    public Date getDate(String name) {
        String value = properties.getProperty(name);
        if (value == null || value.length() == 0) return Calendar.getInstance().getTime();
        Date dval = Calendar.getInstance().getTime();
        try {
            dval = DATE_FORMATTER.parse(value);
        } catch (Exception e) {
            dval = Calendar.getInstance().getTime();
        }
        return dval;
    }
    
    public void setValue(String name, double value) {
        double oldValue = getDouble(name);
        if (oldValue != value) {
        	properties.put(name, Double.toString(value));
        }
    }

    public void setValue(String name, float value) {
        float oldValue = getFloat(name);
        if (oldValue != value) {
        	properties.put(name, Float.toString(value));
        }
    }

    public void setValue(String name, int value) {
        int oldValue = getInt(name);
        if (oldValue != value) {
        	properties.put(name, Integer.toString(value));
        }
    }

    public void setValue(String name, byte value) {
        int oldValue = getByte(name);
        if (oldValue != value) {
        	properties.put(name, Byte.toString(value));
        }
    }
    
    public void setValue(String name, long value) {
        long oldValue = getLong(name);
        if (oldValue != value) {
        	properties.put(name, Long.toString(value));
        }
    }

    public void setValue(String name, String value) {
        String oldValue = getString(name);
        if (oldValue == null || !oldValue.equals(value)) {
            if (value != null) {
            	properties.put(name, value);
            }
        }
    }

    public void setPassword(String name, String value) {
    	String oldValue = getPassword(name);
    	if (!oldValue.equals(value)) {
    		if (value != null) {
    			try {
                	properties.put(name, Crypto.encrypt(value, KEY));
    			} catch (Exception e) {
    	    		logger.error("", e);
    			}
    		}
    	}
    }
    
    public void setValue(String name, boolean value) {
        boolean oldValue = getBoolean(name);
        if (oldValue != value) {
        	properties.put(name, value == true ? Config.TRUE : Config.FALSE);
        }
    }

    public void setValue(String name, Date value) {
        Date oldValue = getDate(name);
        if (oldValue != value) {
        	properties.put(name, DATE_FORMATTER.format(value));
        }
    }

    public void save() {
    	save(filename);
    }
    
    public void save(String filename) {
        try {
        	FileOutputStream fos = new FileOutputStream(filename);
            properties.store(fos, "");
            fos.close();
        } catch (Exception e) {
        	logger.error("", e);
        }
    }
    
    public void load() {
    	load(filename);
    }
    
    public void load(String filename) {
    	try {
    		FileInputStream fis = new FileInputStream(filename);
    		properties.load(fis);
    		fis.close();
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    }
	
    public void read(String src) {
    	try {
    		ByteArrayInputStream bais = new ByteArrayInputStream(src.getBytes("UTF-8"));
    		properties.load(bais);
    		bais.close();
    	} catch (Exception e) {
        	logger.error("", e);
    	}
    }

    public String write() {
    	String tag = "";
        try {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
            properties.store(baos, "");
            tag = baos.toString();
            baos.close();
        } catch (Exception e) {
        	logger.error("", e);
        }
        return tag;
    }
    
    public void clear() {
    	properties = new Properties();
    }
    
    public List<String> getKeys() {
    	List<String> tag = new ArrayList<String>();
    	for (Object key : properties.keySet()) {
    		tag.add((String)key);
    	}
    	return tag;
    }
    
    public Config clone() {
    	Config tag = new Config();
    	for (Object key : properties.keySet()) {
    		tag.setValue((String)key, getString((String)key));
    	}
    	return tag;
    }
    
    public void clone(Config src) {
    	clear();
    	List<String> keys = src.getKeys();
    	for (int i = 0; i < keys.size(); i++) {
    		setValue(keys.get(i), src.getString(keys.get(i)));
    	}
    }
    
    public List<String> getStringList(String key) {
    	List<String> tag = new ArrayList<String>();
    	int size = getInt(key + ".size");
    	for (int i = 0; i < size; i++) {
    		tag.add(getString(key + "." + i));
    	}
    	return tag;
    }
    
    public void remove(String prefix) {
    	List<String> keys = getKeys();
    	for (int i = 0; i < keys.size(); i++) {
    		String key = keys.get(i);
    		if (key.startsWith(prefix)) {
    			properties.remove(key);
    		}
    	}
    }
    
    public void setValue(String key, List<String> val) {
    	remove(key + ".");
    	setValue(key + ".size", val.size());
    	for (int i = 0; i < val.size(); i++) {
    		setValue(key + "." + i, val.get(i));
    	}
    }
    
}
