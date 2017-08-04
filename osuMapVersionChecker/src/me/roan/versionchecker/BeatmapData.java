package me.roan.versionchecker;

public final class BeatmapData{
	//shared
	public String artist;
	public String title;
	public String creator;
	public int total_length;
	
	//local only
	public String songfolder;
	public String audiofile;
	public int preview_time;
	
	
	
	
	public int status;
	public String diff;
	public String hash;
	public String osufilename;
	public int mapid;
	public int setid;
	int approved;
	
	String version;
	float diff_size;
	float diff_overal;
	float diff_approach;
	float diff_drain;
	int mode;
	String approved_date;
	String last_update;
	int bpm;
	String source;
	String tags;
	double difficultyrating;
}