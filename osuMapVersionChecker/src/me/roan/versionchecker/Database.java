package me.roan.versionchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.roan.infinity.util.ByteUtils;
import me.roan.infinity.util.encryption.Encryption;

public class Database {
	
	//https://github.com/ppy/osu-wiki/blob/master/wiki/osu!_File_Formats/Db_(file_format)/en.md
	
	private static int version;//osu! version, not that anyone still uses an old version
	public static final List<BeatmapData> maps = new ArrayList<BeatmapData>();
	public static final int GM_STANDARD= 0;
	public static final int GM_CTB= 2;
	public static final int GM_MANIA= 3;
	public static final int GM_TAIKO= 1;
	
	public static final void readDatabase() throws IOException{
		FileInputStream in = new FileInputStream(new File(VersionChecker.OSUDIR, "osu!.db"));
		version = readInt(in);
		in.skip(13);
		readString(in);
		int numberOfBeatmaps = readInt(in);
		for(int i = 0 ; i < numberOfBeatmaps; i++){
			BeatmapData data = readBeatmapEntry(in);
			if(data.status != 4 && data.status != 5 && data.status != 1 && data.status != 7//ignore: ranked, approved, unsubmitted and loved
			   && !(data.creator.equals("") && data.title.equals("") && data.diff.equals(""))){//ignore: empty entries
				maps.add(data);
			}
		}
		in.skip(4);
		in.close();
	}
	
	private static final BeatmapData readBeatmapEntry(InputStream in) throws IOException{
		BeatmapData data = new BeatmapData();
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
		in.skip(6);
		data.last_modification_time = readDate(in);
		if(version < 20140609){
			data.diff_approach = in.read();
			data.diff_size = in.read();
			data.diff_drain = in.read();
			data.diff_overall = in.read();
		}else{
			data.diff_approach = readFloat(in);
			data.diff_size = readFloat(in);
			data.diff_drain = readFloat(in);
			data.diff_overall = readFloat(in);
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
	
	public static int readInt(InputStream in) throws IOException{
		byte[] arr = new byte[4];
		in.read(arr);
		ByteUtils.flipArray(arr);
		return ByteUtils.byteArrayToInt(arr);
	}
	
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
	
	public static float readFloat(InputStream in) throws IOException {
		byte[] bytes = new byte[4];
		in.read(bytes);
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
		return bb.getFloat();
	}
	
	public static double readDouble(InputStream in) throws IOException {
		byte[] bytes = new byte[8];
		in.read(bytes);
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
		return bb.getDouble();
	}
	
	public static String readString(InputStream in) throws IOException{
		if(in.read() == 0x00){
			return null;
		}
		byte[] str = new byte[readUnsignedLeb128(in)];
		in.read(str);
		return new String(str, Encryption.CHARSET);
	}

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
	
	public static long readLong(InputStream in) throws IOException {
		byte[] bytes = new byte[8];
		in.read(bytes);
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
		return bb.getLong();
	}
	
	public static Date readDate(InputStream in) throws IOException {
		long ticks = readLong(in);
		final long TICKS_AT_EPOCH = 621355968000000000L;
		final long TICKS_PER_MILLISECOND = 10000;
		return new Date((ticks - TICKS_AT_EPOCH) / TICKS_PER_MILLISECOND);
	}
}
