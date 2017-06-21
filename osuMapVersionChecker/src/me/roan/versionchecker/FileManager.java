package me.roan.versionchecker;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import me.roan.infinity.graphics.ui.RListUI.ListRenderable;
import me.roan.osureplaymap.database.Database;
import me.roan.osureplaymap.database.Database.BeatmapData;
import me.roan.osureplaymap.gui.Colors;
import me.roan.osureplaymap.replay.Rank;
import me.roan.osureplaymap.replay.Replay;

public class FileManager extends Thread{
	
	private static ItemBase[] beatmaps;
	private static final DefaultListModel<ListRenderable> beatmapsModel = new DefaultListModel<ListRenderable>();

	public static ListModel<ListRenderable> getBeatmaps(){
		return beatmapsModel;
	}
	
	@Override
	public void run(){    
		exportedReplays = parseR(new File(Main.OSUDIR, "Replays").listFiles((FilenameFilter) (f, n)->{            
			return n.endsWith(".osr");                                                                                                            
		}));
		System.out.println("Done loading exported replays");
		addAll(exportedReplaysModel, exportedReplays);                                                                                                                                      
		beatmaps        = parseB();      
		addAll(beatmapsModel, beatmaps);
		System.out.println("Done loading beatmaps");
		localReplays    = parseR(new File(new File(Main.OSUDIR, "Data"), "r").listFiles((FilenameFilter) (f, n)->{
			return n.endsWith(".osr");                                                                                                            
		}));
		addAll(localReplaysModel, localReplays);
		System.out.println("Done loading local replays");
	}
	
	protected static void loadFiles(){
		FileManager fm = new FileManager();
		fm.setName("File loader");
		fm.setDaemon(true);
		fm.start();
	}
	
	private static final void addAll(DefaultListModel<ListRenderable> model, ListRenderable[] items){
		for(ListRenderable p : items){
			model.addElement(p);
		}
	}
	
	private static final boolean matchesAll(ItemBase item, String... queries){
		if(item instanceof ReplayItem){
			for(String query : queries){
				if(!(((ReplayItem)item).title.toLowerCase().contains(query.toLowerCase()) || ((ReplayItem)item).diff.toLowerCase().contains(query.toLowerCase()) || ((ReplayItem)item).player.toLowerCase().contains(query.toLowerCase()))){
					return false;
				}
			}
		}else if (item instanceof BeatmapItem){
			for(String query : queries){
				if(!(((BeatmapItem)item).title.toLowerCase().contains(query.toLowerCase()) || ((BeatmapItem)item).diff.toLowerCase().contains(query.toLowerCase()) || ((BeatmapItem)item).creator.toLowerCase().contains(query.toLowerCase()))){
					return false;
				}
			}
		}
		return true;
	}
	
	
	
	private static ItemBase[] parseB(){
		ItemBase[] panels = new ItemBase[Database.maps.size()];
		int i = 0;
		for(BeatmapData data : Database.maps.values()){
			panels[i] = new BeatmapItem(new File(Main.OSUDIR, "Songs" + File.separator + data.songfolder), data.setid, data.creator, data.diff, data.title, data.artist, data);
			i++;
		}
		return panels;
	}
	
	private static abstract class ItemBase implements ListRenderable{
		public final File file;
		protected static final Font ftitle = new Font("Dialog", Font.BOLD, 12);
		protected static final Font finfo = new Font("Dialog", Font.PLAIN, 11);
		protected static final Font fmods = new Font("Dialog", Font.BOLD, 11);
		
		private ItemBase(File file){
			this.file = file;
		}
	}
	
	
	
	public static final class BeatmapItem extends ItemBase{
		private String title;
		private String diff;
		private String creator;
		private Image icon;
		public final BeatmapData data;
		private static final ExecutorService imageLoader = Executors.newSingleThreadExecutor();
		
		public String getTitle(){
			return title;
		}
		
		public String getDiff() {
			return diff;
		}
		
		public String getCreator() {
			return creator;
		}

		@Override
		public void paint(Graphics g, int x, int y, int w, int h, boolean selected) {
			g.setColor(Color.LIGHT_GRAY.brighter());
			g.fillRect(x, y, x + w, y + h);
			if(selected){
				g.setColor(Colors.SELECTION_COLOR);
				g.fillRect(x, y, x + w, y + h);
			}
			g.setColor(Color.GRAY);
			g.drawLine(x, y + h - 1, x + w, y + h - 1);
			g.drawImage(icon, x + 4, y + 4, null);
			g.setColor(Colors.PINK);
			g.setFont(ftitle);
			g.drawString(title, (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6, y + 12);
			g.setColor(Color.BLACK);
			g.setFont(finfo);
			g.drawString(creator, (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6, y + 12 + 14);
			g.drawString(diff, (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6, y + 12 + 14 + 15);
		}
		
		private BeatmapItem(File file, int id, String creator, String diff, String title, String artist, BeatmapData data){
			super(file);
			imageLoader.submit(()->{
				try {
					File f = new File(Main.OSUDIR + File.separator + "Data" + File.separator + "bt" + File.separator + id + ".jpg");
					if(!f.exists()){
						f = new File(Main.OSUDIR + File.separator + "Data" + File.separator + "bt" + File.separator + id + "l.jpg");
					}
					if(f.exists()){
						icon = ImageIO.read(f).getScaledInstance(-1, 16 * 2 + 8, Image.SCALE_SMOOTH);
					}
				} catch (IOException e) {
					//This error is not very important, report it to standard error and move on.
					System.err.println("Couldn't load beatmap icon for: " + title + "/" + id + " icons:[small=" + new File(Main.OSUDIR + File.separator + "Data" + File.separator + "bt" + File.separator + id + ".jpg").exists() + ",large=" + new File(Main.OSUDIR + File.separator + "Data" + File.separator + "bt" + File.separator + id + "l.jpg").exists() + "]");
					e.printStackTrace();
				}
			});
			this.title = artist + " - " + title;
			this.diff = "Difficulty: " + diff;
			this.creator = "By " + creator;
			this.data = data;
		}
	}
}
