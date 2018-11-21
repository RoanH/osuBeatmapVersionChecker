package me.roan.versionchecker;

/**
 * Object that hold data about a
 * beatmap. This object only holds
 * shared properties local and online
 * specific values are found in the
 * {@linkplain OnlineBeatmapData} and
 * {@linkplain LocalBeatmapData} subclasses
 * @author Roan
 */
public class BeatmapData{
	/**
	 * Beatmap song artist
	 */
	public String artist;
	/**
	 * Beatmap song title
	 */
	public String title;
	/**
	 * Beatmap creator
	 */
	public String creator;
	/**
	 * Total lenght of the beatmap
	 */
	public int total_length;
	/**
	 * Star rating of the beatmap
	 */
	public double difficultyrating;
	/**
	 * Game mode
	 */
	public int mode;

	/**
	 * Object to hold the local data
	 * about a beatmap found in osu!.db
	 * @author Roan
	 */
	public static final class LocalBeatmapData extends BeatmapData{
		/**
		 * Beatmap folder name
		 */
		public String songfolder;
		/**
		 * Beatmap audio file name
		 */
		public String audiofile;
		/**
		 * Beatmap song preview time
		 */
		public int previeuw_time;
		/**
		 * Beatmap ranked status
		 */
		public int status;
		/**
		 * Beatmap thread ID
		 */
		public int thread;
		/**
		 * Beatmap .osu file name
		 */
		public String osufilename;
		/**
		 * Beatmap difficulty name
		 */
		public String diff;
		/**
		 * Beatmap id
		 */
		public int mapid;
		/**
		 * Beatmap setid
		 */
		public int setid;
		/**
		 * Beatmap hash
		 */
		public String hash;
	}

	/**
	 * Object to mirror the online
	 * state of a beatmap
	 * @author Roan
	 */
	public static final class OnlineBeatmapData extends BeatmapData{
		/**
		 * Ranked status of the map
		 */
		public int approved;

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