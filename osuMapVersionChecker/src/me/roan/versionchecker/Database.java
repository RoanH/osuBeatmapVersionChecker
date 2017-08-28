package me.roan.versionchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import me.roan.infinity.util.ByteUtils;
import me.roan.infinity.util.encryption.Encryption;
import me.roan.versionchecker.BeatmapData.LocalBeatmapData;

/**
 * This class is used to read beatmap
 * data from osu!.db
 * @author RoanH
 */
public class Database {
	/**
	 * osu! version used for proper parsing of the database
	 */
	private static int version;
	/**
	 * List of unranked beatmaps
	 */
	public static final List<LocalBeatmapData> maps = new ArrayList<LocalBeatmapData>();
	/**
	 * Gamemode standard constant
	 */
	public static final int GM_STANDARD = 0;
	/**
	 * Gamemode catch constant
	 */
	public static final int GM_CTB = 2;
	/**
	 * Gamemode mania constant
	 */
	public static final int GM_MANIA = 3;
	/**
	 * Gamemode taiko constant
	 */
	public static final int GM_TAIKO = 1;
	
	/**
	 * Reads the local beatmap database and
	 * filters out and compiles information
	 * about present unranked maps 
	 * @throws IOException When an IOException occurs
	 */
	public static final void readDatabase() throws IOException{
		FileInputStream in = new FileInputStream(new File(VersionChecker.OSUDIR, "osu!.db"));
		version = readInt(in);
		in.skip(13);
		readString(in);
		int numberOfBeatmaps = readInt(in);
		VersionChecker.time.setText("Parsing beatmaps: 0/" + numberOfBeatmaps);
		VersionChecker.progress.setMinimum(0);
		VersionChecker.progress.setValue(0);
		VersionChecker.progress.setMaximum(Database.maps.size());
		for(int i = 0 ; i < numberOfBeatmaps; i++){
			LocalBeatmapData data = readBeatmapEntry(in);
			if(data.status != 4 && data.status != 5 && data.status != 1 && data.status != 7){//ignore: ranked, approved, unsubmitted and loved
				BeatmapItem local = new BeatmapItem(new File(VersionChecker.OSUDIR, "Songs" + File.separator + data.songfolder), data);
				FileManager.beatmapsModel.addElement(local);
				VersionChecker.updateQueue.add(()->{
					System.out.println("Execute state check");
					local.setOnlineData(VersionChecker.checkState(data));
					System.out.println("Online data fetched");
					if(local.online == null){
						return false;
					}
					if(local.mapChanged()){
						FileManager.beatmapsUpdateModel.addElement(local);
						VersionChecker.categories.setTitleAt(2, "Update available (" + FileManager.beatmapsUpdateModel.size() + ")");
					}
					if(local.stateChanged()){
						FileManager.beatmapsStateModel.addElement(local);
						VersionChecker.categories.setTitleAt(1, "State changed (" + FileManager.beatmapsStateModel.size() + ")");
					}
					System.out.println("State check done");
					return true;
				});
			}
			System.out.println("Parsing beatmaps: " + FileManager.beatmapsModel.size() + "/" + Database.maps.size());
			VersionChecker.time.setText("Parsing beatmaps: " + FileManager.beatmapsModel.size() + "/" + Database.maps.size());
			VersionChecker.progress.setValue(FileManager.beatmapsModel.size());
		}
		in.skip(4);
		in.close();
	}
	
	/**
	 * Reads a single beatmap entry
	 * from the database
	 * @param in The open input stream to the database
	 * @return The parsed beatmap entry
	 * @throws IOException When an IOException occurs
	 */
	private static final LocalBeatmapData readBeatmapEntry(InputStream in) throws IOException{
		LocalBeatmapData data = new LocalBeatmapData();
		in.skip(4);
		data.artist = readString(in);
		readString(in);
		data.title = readString(in);
		readString(in);
		data.creator = readString(in);
		data.diff = readString(in);
		data.audiofile = readString(in);
		data.hash = readString(in);
		data.osufilename = readString(in);
		data.status = in.read();
		in.skip(14);
		if(version < 20140609){
			in.skip(4);
		}else{
			in.skip(16);
		}
		in.skip(8);
		double std_rating = 0.0D;
		double taiko_rating = 0.0D;
		double ctb_rating = 0.0D;
		double mania_rating = 0.0D;
		if(version >= 20140609){
			for(int i = readInt(in); i > 0; i--){
				double rating = readIntDoublePairNoModRating(in);
				if(rating != -1){
					std_rating = rating;
				}
			}
			for(int i = readInt(in); i > 0; i--){
				double rating = readIntDoublePairNoModRating(in);
				if(rating != -1){
					taiko_rating = rating;
				}
			}
			for(int i = readInt(in); i > 0; i--){
				double rating = readIntDoublePairNoModRating(in);
				if(rating != -1){
					ctb_rating = rating;
				}
			}
			for(int i = readInt(in); i > 0; i--){
				double rating = readIntDoublePairNoModRating(in);
				if(rating != -1){
					mania_rating = rating;
				}
			}
		}
		in.skip(4);
		data.total_length = readInt(in);
		data.previeuw_time = readInt(in);
		in.skip(readInt(in) * 17);
		data.mapid = readInt(in);
		data.setid = readInt(in);
		data.thread = readInt(in);
		in.skip(10);
		data.mode = in.read();
		switch(data.mode){
		case GM_STANDARD:
			data.difficultyrating = std_rating;
			break;
		case GM_TAIKO:
			data.difficultyrating = taiko_rating;
			break;
		case GM_MANIA:
			data.difficultyrating = mania_rating;
			break;
		case GM_CTB:
			data.difficultyrating = ctb_rating;
			break;
		}
		readString(in);
		readString(in);
		in.skip(2);
		readString(in);
		in.skip(10);
		data.songfolder = readString(in);
		in.skip(13);
		if(version < 20140609){
			in.skip(2);
		}
		in.skip(5);
		return data;
	}
	
	/**
	 * Reads a single int from 
	 * the database input stream
	 * @param in The open input 
	 *        stream to the database
	 * @return The integer that was read
	 * @throws IOException When an IOException occurs
	 */
	public static int readInt(InputStream in) throws IOException{
		byte[] arr = new byte[4];
		in.read(arr);
		ByteUtils.flipArray(arr);
		return ByteUtils.byteArrayToInt(arr);
	}
	
	/**
	 * Reads a single int-double
	 * pair from the input stream
	 * the read double is only returned
	 * when the int denoting the mod
	 * combination indicates nomod
	 * otherwise -1 is returned
	 * @param in The open input stream
	 *        to the database
	 * @return The double that was read
	 * @throws IOException When an IOException occurs
	 */
	public static double readIntDoublePairNoModRating(InputStream in) throws IOException{
		in.read();
		int i = readInt(in);
		in.read();
		double d = readDouble(in);
		if(i == 0){
			return d;
		}else{
			return -1.0D;
		}
	}
	
	/**
	 * Reads a single float from
	 * the input stream
	 * @param in The open input stream
	 *        to the database
	 * @return The float that was read
	 * @throws IOException When an IOException occurs
	 */
	public static float readFloat(InputStream in) throws IOException {
		byte[] bytes = new byte[4];
		in.read(bytes);
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
		return bb.getFloat();
	}
	
	/**
	 * Reads a single double from
	 * the input stream
	 * @param in The open input stream
	 *        to the database
	 * @return The double that was read
	 * @throws IOException When an IOException occurs
	 */
	public static double readDouble(InputStream in) throws IOException {
		byte[] bytes = new byte[8];
		in.read(bytes);
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
		return bb.getDouble();
	}
	
	/**
	 * Reads a string from
	 * the input stream
	 * @param in The open input stream
	 *        to the database
	 * @return The string that was read
	 * @throws IOException When an IOException occurs
	 */
	public static String readString(InputStream in) throws IOException{
		if(in.read() == 0x00){
			return null;
		}
		byte[] str = new byte[readUnsignedLeb128(in)];
		in.read(str);
		return new String(str, Encryption.CHARSET);
	}

	/**
	 * Reads a single variable length 
	 * integer from the input stream
	 * @param in The open input stream
	 *        to the database
	 * @return The integer that was read
	 * @throws IOException When an IOException occurs
	 */
	public static int readUnsignedLeb128(InputStream in) throws IOException {
		int result = 0;
		int cur;
		int count = 0;
		do {
			cur = in.read() & 0xFF;
			result |= (cur & 0x7F) << (count * 7);
			count++;
		} while (((cur & 0x80) == 0x80) && count < 5);
		return result;
	}
}
