package me.roan.versionchecker;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import me.roan.infinity.graphics.ui.RListUI.ListRenderable;
import me.roan.versionchecker.BeatmapData.LocalBeatmapData;
import me.roan.versionchecker.BeatmapData.OnlineBeatmapData;

public final class BeatmapItem implements ListRenderable{
	public final File file;
	protected static final Font ftitle = new Font("Dialog", Font.BOLD, 12);
	protected static final Font finfo = new Font("Dialog", Font.PLAIN, 11);
	protected static final Font finfob = new Font("Dialog", Font.BOLD, 11);
	public static final Color PINK = new Color(255, 102, 204);
	public static final Color SELECTION_COLOR = new Color(0.0F, 1.0F, 1.0F, 0.3F);
	public static final Color DL_TRUE = new Color(0.0F, 1.0F, 0.0F, 0.15F);
	public static final Color DL_FALSE = new Color(1.0F, 0.0F, 0.0F, 0.15F);
	public static final Color DL_TRUE_2X = new Color(0.0F, 1.0F, 0.0F, 0.3F);
	public static final Color DL_FALSE_2X = new Color(1.0F, 0.0F, 0.0F, 0.3F);
	public static final Color DL_TRUE_4X = new Color(0.0F, 1.0F, 0.0F, 0.6F);
	public static final Color DL_FALSE_4X = new Color(1.0F, 0.0F, 0.0F, 0.6F);
	private Image icon;
	public final LocalBeatmapData local;
	public OnlineBeatmapData online;
	private static final ExecutorService imageLoader = Executors.newSingleThreadExecutor();
	private int y;
	private int w;
	private boolean playing = false;
	private Boolean download = null;
	private boolean showControls = true;

	@Override
	public void paint(Graphics g1, int x, int y, int w, int h, boolean selected) {
		Graphics2D g = (Graphics2D)g1;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		this.y = y;
		this.w = w;
		g.setColor(Color.LIGHT_GRAY.brighter());
		g.fillRect(x, y, x + w, y + h);
		if(download != null){
			if(download){
				g.setColor(selected ? DL_TRUE_2X : DL_TRUE);
				g.fillRect(x, y, x + w, y + h);
			}else{
				g.setColor(selected ? DL_FALSE_2X : DL_FALSE);
				g.fillRect(x, y, x + w, y + h);
			}
		}
		if(selected){
			g.setColor(SELECTION_COLOR);
			g.fillRect(x, y, x + w, y + h);
		}
		g.setColor(Color.GRAY);
		g.drawLine(x, y + h - 1, x + w, y + h - 1);
		g.drawImage(icon, x + 4, y + 4, null);//40 - 71
		if(!playing && selected){
			g.setColor(Color.WHITE);
			int[] xs = new int[]{x + 4 + 15, x + 4 + 36, x + 4 + 15};
			int[] ys = new int[]{y + 4 + 10, y + 4 + 20, y + 4 + 30};
			g.fillPolygon(xs, ys, 3);
			g.setColor(Color.WHITE.darker());
			g.drawPolygon(xs, ys, 3);
		}else if(playing){
			g.setColor(Color.WHITE);
			g.fillRect(x + 4 + 15, y + 4 + 10, 7, 20);
			g.fillRect(x + 4 + 14 + 16, y + 4 + 10, 7, 20);
			g.setColor(Color.WHITE.darker());
			g.drawRect(x + 4 + 15, y + 4 + 10, 7, 20);
			g.drawRect(x + 4 + 14 + 16, y + 4 + 10, 7, 20);
		}
		g.setColor(PINK);
		g.setFont(ftitle);
		g.drawString(local.title + " [" + local.diff + "]", (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6, y + 12);
		g.setColor(Color.BLACK);
		g.setFont(finfo);
		g.drawString("By " + local.creator, (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6, y + 12 + 14);
		//status
		String state = "Status: " + getStatusLocal(local.status);
		g.drawString(state, (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6, y + 12 + 14 + 15);
		if(online != null){
			if(this.stateChanged()){
				g.setColor(Color.RED);
				g.drawString(" > " + getStatusOnline(online.approved), (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 1 + g.getFontMetrics().stringWidth(state), y + 12 + 14 + 15);
			}
		}else{
			g.setColor(Color.GRAY);
			g.drawString(" > Loading", (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 1 + g.getFontMetrics().stringWidth(state), y + 12 + 14 + 15);
		}
		//attributes
		g.setFont(finfob);
		g.setColor(Color.BLACK);
		g.drawString("Stars: ", (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 140, y + 12 + 14);
		g.drawString("Length: ", (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 140, y + 12 + 14 + 15);
		int soff = g.getFontMetrics().stringWidth("Stars: ");
		int loff = g.getFontMetrics().stringWidth("Length: ");
		g.setFont(finfo);
		g.drawString(String.format("%1$.2f",  local.difficultyrating), (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 140 + soff, y + 12 + 14);
		g.drawString(String.format("%02d:%02d",
			    TimeUnit.MILLISECONDS.toMinutes(local.total_length) % TimeUnit.HOURS.toMinutes(1),
			    TimeUnit.MILLISECONDS.toSeconds(local.total_length) % TimeUnit.MINUTES.toSeconds(1)),  (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 140 + loff, y + 12 + 14 + 15);
		
		//3 * 16 = 48 | 40 - 38:n 36:y12
		g.setFont(finfob);
		g.setColor(PINK);
		g.fillRect(w - 80, y + 4, 76, 19);
		g.fillRect(w - 80, y + 25, 76, 19);
		g.fillRect(w - 80 - 76 - 4, y + 4, 76, 19);
		g.fillRect(w - 80 - 76 - 4, y + 25, 76, 19);
		g.setColor(PINK.darker());
		g.drawRect(w - 80, y + 4, 76, 19);
		g.drawRect(w - 80, y + 25, 76, 19);
		g.drawRect(w - 80 - 76 - 4, y + 4, 76, 19);
		g.drawRect(w - 80 - 76 - 4, y + 25, 76, 19);
		g.setColor(Color.WHITE);
		g.drawString("Listing", (int) w - 80 - 76 - 4 + ((76 - g.getFontMetrics().stringWidth("Listing")) / 2), y + 17);
		g.drawString("Forum post", (int) w - 80 - 76 - 4 + ((76 - g.getFontMetrics().stringWidth("Forum post")) / 2), y + 12 + 14 + 12);
		g.drawString("Copy title", (int) w - 80 + ((76 - g.getFontMetrics().stringWidth("Copy title")) / 2), y + 17);
		g.drawString("osu! direct", (int) w - 80 + ((76 - g.getFontMetrics().stringWidth("osu! direct")) / 2), y + 12 + 14 + 12);
		
		//controls
		if(showControls){
			g.setColor(Color.WHITE);
			g.fillRect(w - 80 - 76 - 76 - 8, y + 4, 76, 19);
			g.fillRect(w - 80 - 76 - 76 - 8, y + 25, 76, 19);
			g.setColor(DL_TRUE_2X);
			g.fillRect(w - 80 - 76 - 76 - 8, y + 4, 76, 19);
			g.setColor(Color.GREEN);
			g.drawRect(w - 80 - 76 - 76 - 8, y + 4, 76, 19);
			g.setColor(DL_FALSE_2X);
			g.fillRect(w - 80 - 76 - 76 - 8, y + 25, 76, 19);
			g.setColor(Color.RED);
			g.drawRect(w - 80 - 76 - 76 - 8, y + 25, 76, 19);
			g.setColor(Color.BLACK);
			g.drawString("Update", (int) w - 80 - 78 - 76 - 4 + ((76 - g.getFontMetrics().stringWidth("Update")) / 2), y + 17);
			g.drawString("Don't update", (int) w - 78 - 80 - 76 - 4 + ((76 - g.getFontMetrics().stringWidth("Don't update")) / 2), y + 12 + 14 + 12);
			
		}
	}

	//0=unknow,4=ranked,5=approved,7=loved?,2=graveyard/pending,1=not submited>
	private static String getStatusLocal(int id){
		return id == 1 ? "Not submitted" : (id == 2 ? "Pending" : (id == 4 ? "Ranked" : (id == 5 ? "Approved" : (id == 7 ? "Loved" : "Unknow"))));
	}
	
	//4=loved,3=qualified,2=approved,1=ranked,0=pending,-1= WIP,-2=graveyard
	private static String getStatusOnline(int id){
		return id == 4 ? "Loved" : (id == 3 ? "Qualified" : (id == 2 ? "Approved" : (id == 1 ? "Ranked" : (id == 0 ? "Pending" : (id == -1 ? "WIP" : (id == -2 ? "Graveyard" : "Unknow"))))));
	}
	
	protected boolean mapChanged(){
		return online.generated;
	}
	
	protected boolean stateChanged(){
		if((local.status == 4 && online.approved == 1) ||
				(local.status == 5 && online.approved == 2) ||
				(local.status == 7 && online.approved == 4) ||
				(local.status == 2 && online.approved <= 0)){
			return false;
		}else{
			return true;
		}
	}

	public void cancelPlayingState(){
		playing = false;
	}

	public void onMouseEvent(MouseEvent e){
		try {
			if(e.getX() > 4 && e.getX() < 4 + 71 && e.getY() > y + 4 && e.getY() < y + 4 + 40){
				if(!playing){
					PrevieuwPlayer.playFile(this);
					playing = true;
				}else{
					PrevieuwPlayer.stop();
				}
				e.getComponent().repaint();
			}else if(e.getX() > w - 80 && e.getX() < w - 4 && e.getY() > y + 4 && e.getY() < y + 23){
				StringSelection selection = new StringSelection(local.title + " " + local.diff + " " + local.artist + " " + local.creator);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
			}else if(e.getX() > w - 80 && e.getX() < w - 4 && e.getY() > y + 25 && e.getY() < y + 44){
				Desktop.getDesktop().browse(new URI("osu://dl/" + local.setid));
			}else if(e.getX() > w - 80 - 76 && e.getX() < w - 4 - 76 && e.getY() > y + 4 && e.getY() < y + 23){
				Desktop.getDesktop().browse(new URI("https://osu.ppy.sh/b/" + local.mapid));
			}else if(e.getX() > w - 80 - 76 && e.getX() < w - 4 - 76 && e.getY() > y + 25 && e.getY() < y + 44){
				Desktop.getDesktop().browse(new URI("https://osu.ppy.sh/forum/t/" + local.thread));
			}else if(showControls && e.getX() > w - 80 - 80 - 76 && e.getX() < w - 4 - 80 - 76 && e.getY() > y + 4 && e.getY() < y + 23){
				download = true;
				e.getComponent().repaint();
			}else if(showControls && e.getX() > w - 80 - 80 - 76 && e.getX() < w - 4 - 80 - 76 && e.getY() > y + 25 && e.getY() < y + 44){
				download = false;
				e.getComponent().repaint();
			}
		} catch (Throwable e1) {
			JOptionPane.showMessageDialog(VersionChecker.frame, "An exception occurred", "Version Checker", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void setOnlineData(OnlineBeatmapData data){
		this.online = data;
		showControls = data.generated;
	}

	protected BeatmapItem(File file, LocalBeatmapData data){
		this.file = file;
		imageLoader.submit(()->{
			try {
				File f = new File(VersionChecker.OSUDIR + File.separator + "Data" + File.separator + "bt" + File.separator + data.setid + ".jpg");
				if(!f.exists()){
					f = new File(VersionChecker.OSUDIR + File.separator + "Data" + File.separator + "bt" + File.separator + data.setid + "l.jpg");
				}
				if(f.exists()){
					icon = ImageIO.read(f).getScaledInstance(-1, 16 * 2 + 8, Image.SCALE_SMOOTH);
				}
			} catch (IOException | NullPointerException | ArrayIndexOutOfBoundsException e) {
				//This error is not very important, report it to standard error and move on.
				System.err.println("Couldn't load beatmap icon for: " + data.title + "/" + data.setid + " icons:[small=" + new File(VersionChecker.OSUDIR + File.separator + "Data" + File.separator + "bt" + File.separator + data.setid + ".jpg").exists() + ",large=" + new File(VersionChecker.OSUDIR + File.separator + "Data" + File.separator + "bt" + File.separator + data.setid + "l.jpg").exists() + "]");
				e.printStackTrace();
			}
		});
		this.local = data;
	}
}