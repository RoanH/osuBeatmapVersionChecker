package me.roan.versionchecker;

import java.util.Date;

public final class BeatmapData{
	//shared
	public String artist;
	public String title;
	public String creator;
	public int total_length;
	public double difficultyrating;
	public float diff_size;
	public float diff_overall;
	public float diff_approach;
	public float diff_drain;
	
	//local only
	public String songfolder;
	public String audiofile;
	public int previeuw_time;
	public int status;
	public int thread;
	
	//online only
	public int approved;
	
	
	public String diff;
	public String hash;
	public String osufilename;
	public int mapid;
	public int setid;
	
	Date last_modification_time;
	String version;
	
	int mode;
	String approved_date;
	String last_update;
	
	boolean generated = false;
}