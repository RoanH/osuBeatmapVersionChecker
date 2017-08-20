package me.roan.versionchecker;

public class BeatmapData{
	//shared
	public String artist;
	public String title;
	public String creator;
	public int total_length;
	public double difficultyrating;
	public int mode;
	
	public static final class LocalBeatmapData extends BeatmapData{
		//local only
		public String songfolder;
		public String audiofile;
		public int previeuw_time;
		public int status;
		public int thread;
		public String osufilename;
		public String diff;
		public int mapid;
		public int setid;
		public String hash;
	}
	
	public static final class OnlineBeatmapData extends BeatmapData{
		//online only
		/**
		 * Ranked status of the map
		 */
		public int approved;
		
		//special
		/**
		 * Whether or not this is a place holder
		 * data object. This indicates that the
		 * online map data could not be fetched
		 * because the result was empty meaning
		 * that an update is available.
		 */
		boolean generated = false;
	}
}