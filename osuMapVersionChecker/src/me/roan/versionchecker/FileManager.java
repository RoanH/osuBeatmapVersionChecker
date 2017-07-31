package me.roan.versionchecker;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import me.roan.infinity.graphics.ui.RListUI.ListRenderable;

public class FileManager{
	
	private static ItemBase[] beatmaps;
	private static final DefaultListModel<ListRenderable> beatmapsModel = new DefaultListModel<ListRenderable>();

	public static ListModel<ListRenderable> getBeatmaps(){
		return beatmapsModel;
	}
	
	protected static void init(){
		addAll(beatmapsModel, parseB());
	}
	
	private static final void addAll(DefaultListModel<ListRenderable> model, ListRenderable[] items){
		for(ListRenderable p : items){
			model.addElement(p);
		}
	}
	
	private static ItemBase[] parseB(){
		ItemBase[] panels = new ItemBase[Database.maps.size()];
		int i = 0;
		for(BeatmapData data : Database.maps){
			panels[i] = new BeatmapItem(new File(VersionChecker.OSUDIR, "Songs" + File.separator + data.songfolder), data);
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
		public static final Color PINK = new Color(255, 102, 204);
		public static final Color SELECTION_COLOR = new Color(0.0F, 1.0F, 1.0F, 0.3F);
		private Image icon;
		public final BeatmapData local;
		public BeatmapData online;
		private static final ExecutorService imageLoader = Executors.newSingleThreadExecutor();
		private int y;
		private boolean playing = false;

		@Override
		public void paint(Graphics g, int x, int y, int w, int h, boolean selected) {
			this.y = y;
			g.setColor(Color.LIGHT_GRAY.brighter());
			g.fillRect(x, y, x + w, y + h);
			if(selected){
				g.setColor(SELECTION_COLOR);
				g.fillRect(x, y, x + w, y + h);
			}
			g.setColor(Color.GRAY);
			g.drawLine(x, y + h - 1, x + w, y + h - 1);
			g.drawImage(icon, x + 4, y + 4, null);//40 - 71
			if(selected){
				System.out.println("drawing");
				g.setColor(Color.WHITE);
				if(!playing){
					g.fillPolygon(new int[]{4 + 10, 4 + 30, 4 + 10}, new int[]{4 + 10, 4 + 20, 4 + 30}, 3);
				}else{
					g.fillRect(4 + 10, 4 + 10, 7, 20);
				}
			}
			g.setColor(PINK);
			g.setFont(ftitle);
			g.drawString(local.title + " [" + local.diff + "]", (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6, y + 12);
			g.setColor(Color.BLACK);
			g.setFont(finfo);
			g.drawString(local.creator, (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6, y + 12 + 14);
			g.drawString(local.hash, (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6, y + 12 + 14 + 15);
		}
		
		public void cancelPlayingState(){
			playing = false;
		}
		
		public void onMouseEvent(MouseEvent e){
			System.out.println("click at: " + e.getPoint() + " y: " + y);
			if(e.getX() > 4 && e.getX() < 4 + 71 && e.getY() > y + 4 && e.getY() < y + 4 + 40){
				if(!playing){
					PrevieuwPlayer.playFile(this);
					playing = true;
				}else{
					PrevieuwPlayer.stop();
				}
			}
		}
		
		public void setOnlineData(BeatmapData data){
			this.online = data;
		}
		
		private BeatmapItem(File file, BeatmapData data){
			super(file);
			imageLoader.submit(()->{
				try {
					File f = new File(VersionChecker.OSUDIR + File.separator + "Data" + File.separator + "bt" + File.separator + data.setid + ".jpg");
					if(!f.exists()){
						f = new File(VersionChecker.OSUDIR + File.separator + "Data" + File.separator + "bt" + File.separator + data.setid + "l.jpg");
					}
					if(f.exists()){
						icon = ImageIO.read(f).getScaledInstance(-1, 16 * 2 + 8, Image.SCALE_SMOOTH);
					}
				} catch (IOException e) {
					//This error is not very important, report it to standard error and move on.
					System.err.println("Couldn't load beatmap icon for: " + data.title + "/" + data.setid + " icons:[small=" + new File(VersionChecker.OSUDIR + File.separator + "Data" + File.separator + "bt" + File.separator + data.setid + ".jpg").exists() + ",large=" + new File(VersionChecker.OSUDIR + File.separator + "Data" + File.separator + "bt" + File.separator + data.setid + "l.jpg").exists() + "]");
					e.printStackTrace();
				}
			});
			this.local = data;
		}
	}
}
