package me.roan.versionchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.roan.infinity.util.ByteUtils;
import me.roan.infinity.util.encryption.Encryption;

public class Database {
	
	//https://github.com/ppy/osu-wiki/blob/master/wiki/osu!_File_Formats/Db_(file_format)/en.md
	
	private static int version;//osu! version, not that anyone still uses an old version
	public static final List<BeatmapData> maps = new ArrayList<BeatmapData>();
	
	public static final void readDatabase() throws IOException{
		FileInputStream in = new FileInputStream(new File(VersionChecker.OSUDIR, "osu!.db"));
		version = readInt(in);
		in.skip(13);
		readString(in);
		int numberOfBeatmaps = readInt(in);
		for(int i = 0 ; i < numberOfBeatmaps; i++){
			BeatmapData data = readBeatmapEntry(in);
			maps.add(data);
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
		data.status = in.read();//skip ranked status
		in.skip(2);//skip number of circles
		in.skip(2);//skip number of sliders
		in.skip(2);//skip number of spinners
		in.skip(8);
		if(version < 20140609){
			in.skip(4);
		}else{
			in.skip(16);
		}
		in.skip(8);
		if(version >= 20140609){//TODO read the star ratings
			in.skip(readInt(in) * 14);//skip std star rating
			in.skip(readInt(in) * 14);//skip taiko star rating
			in.skip(readInt(in) * 14);//skip ctb star rating
			in.skip(readInt(in) * 14);//skip mania star rating
		}
		in.skip(12);
		in.skip(readInt(in) * 17);//skip timing points
		data.mapid = readInt(in);
		data.setid = readInt(in);
		in.skip(4);//skip thread id
		in.skip(10);
		in.skip(1);//skip game mode //TODO might e useful
		readString(in);//skip song source
		readString(in);//skip song tags
		in.skip(2);
		readString(in);
		in.skip(10);
		data.songfolder = readString(in);
		in.skip(8);
		in.skip(5);
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
	
	public static final class BeatmapData{
		public String artist;
		public String title;
		public String creator;
		public String diff;
		public String hash;
		public String osufilename;
		public int mapid;
		public int setid;
		public String songfolder;
		public String audiofile;
		public int status;
		
		@Override
		public String toString(){
			return songfolder;
		}
	}
}
