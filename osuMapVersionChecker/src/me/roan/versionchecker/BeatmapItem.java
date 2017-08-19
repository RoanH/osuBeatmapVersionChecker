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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import me.roan.infinity.graphics.ui.RListUI.ListRenderable;

public final class BeatmapItem implements ListRenderable{
	public final File file;
	protected static final Font ftitle = new Font("Dialog", Font.BOLD, 12);
	protected static final Font finfo = new Font("Dialog", Font.PLAIN, 11);
	protected static final Font finfob = new Font("Dialog", Font.BOLD, 11);
	public static final Color PINK = new Color(255, 102, 204);
	public static final Color SELECTION_COLOR = new Color(0.0F, 1.0F, 1.0F, 0.3F);
	private Image icon;
	public final BeatmapData local;
	public BeatmapData online;
	private static final ExecutorService imageLoader = Executors.newSingleThreadExecutor();
	private int y;
	private int w;
	private boolean playing = false;

	@Override
	public void paint(Graphics g1, int x, int y, int w, int h, boolean selected) {
		Graphics2D g = (Graphics2D)g1;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		this.y = y;
		this.w = w;
		g.setColor(Color.LIGHT_GRAY.brighter());
		g.fillRect(x, y, x + w, y + h);
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
		g.setColor(Color.BLACK);
		g.setFont(finfob);
		int offset = g.getFontMetrics().stringWidth("CS: ");
		g.drawString("CS: ", (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 180, y + 12 + 14);
		g.drawString("AR: ", (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 180, y + 12 + 14 + 15);
		g.drawString("HP: ", (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 180 + 100, y + 12 + 14);
		g.drawString("OD: ", (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 180 + 100, y + 12 + 14 + 15);

		g.setFont(finfo);
		g.drawString(String.valueOf(local.diff_size),      (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 180 + offset, y + 12 + 14);
		g.drawString(String.valueOf(local.diff_approach),  (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 180 + offset, y + 12 + 14 + 15);
		g.drawString(String.valueOf(local.diff_drain),     (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 180 + offset + 100, y + 12 + 14);
		g.drawString(String.valueOf(local.diff_overall),    (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 180 + offset + 100, y + 12 + 14 + 15);

		if(online != null){
			offset += g.getFontMetrics().stringWidth("10.0 ");
			g.setFont(finfo);
			if(local.diff_size != online.diff_size){
				g.setColor(Color.RED);
				g.drawString(">  " + online.diff_size,      (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 180 + offset, y + 12 + 14);
			}
			if(local.diff_approach != online.diff_approach){
				g.setColor(Color.RED);
				g.drawString(">  " + online.diff_approach,  (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 180 + offset, y + 12 + 14 + 15);
			}
			if(local.diff_drain != online.diff_drain){
				g.setColor(Color.RED);
				g.drawString(">  " + online.diff_drain,     (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 180 + offset + 100, y + 12 + 14);
			}
			if(local.diff_overall != online.diff_overall){
				g.setColor(Color.RED);
				g.drawString(">  " + online.diff_overall,    (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 180 + offset + 100, y + 12 + 14 + 15);
			}
		}

		g.setFont(finfob);
		g.setColor(Color.BLACK);
		g.drawString("Stars: ", (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 375, y + 12 + 14);
		g.drawString("Length: ", (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 375, y + 12 + 14 + 15);
		int soff = g.getFontMetrics().stringWidth("Stars: ");
		int loff = g.getFontMetrics().stringWidth("Length: ");
		g.setFont(finfo);
		g.drawString(String.format("%1$.2f",  local.difficultyrating), (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 375 + soff, y + 12 + 14);
		g.drawString(String.format("%02d:%02d",
			    TimeUnit.MILLISECONDS.toMinutes(local.total_length) % TimeUnit.HOURS.toMinutes(1),
			    TimeUnit.MILLISECONDS.toSeconds(local.total_length) % TimeUnit.MINUTES.toSeconds(1)),  (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 375 + loff, y + 12 + 14 + 15);
		if(online != null){
			System.out.println("Local diff online diff: " + local.difficultyrating + " | " + online.difficultyrating);
			if(starRatingChanged()){
				g.setColor(Color.RED);
				g.drawString("> " + String.format("%1$.2f",  online.difficultyrating),      (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 375 + soff + g.getFontMetrics().stringWidth("00.00 "), y + 12 + 14);
			}
			if(lengthChanged()){
				g.setColor(Color.RED);
				g.drawString("> " + String.format("%02d:%02d",
					    TimeUnit.SECONDS.toMinutes(online.total_length) % TimeUnit.HOURS.toMinutes(1),
					    TimeUnit.SECONDS.toSeconds(online.total_length) % TimeUnit.MINUTES.toSeconds(1)),  (int) (x + ((double)(16 * 2) / 9.0D) * 16.0D) + 6 + 375 + loff + g.getFontMetrics().stringWidth("00:00 "), y + 12 + 14 + 15);
			}
		}
		
		//3 * 16 = 48 | 40 - 38:n 36:y12
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
		g.drawString("Beatmap page", (int) w - 80 - 76 - 4 + ((76 - g.getFontMetrics().stringWidth("Beatmap page")) / 2), y + 17);
		g.drawString("Forum post", (int) w - 80 - 76 - 4 + ((76 - g.getFontMetrics().stringWidth("Forum post")) / 2), y + 12 + 14 + 12);
		g.drawString("Copy title", (int) w - 80 + ((76 - g.getFontMetrics().stringWidth("Copy title")) / 2), y + 17);
		g.drawString("osu! direct", (int) w - 80 + ((76 - g.getFontMetrics().stringWidth("osu! direct")) / 2), y + 12 + 14 + 12);
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
//		try {
//			Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(online.last_update);
//			if(online.approved_date != null){
//				Date a = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(online.approved_date);
//				//System.out.println(a);
//				if(local.last_modification_time.after(a)){
//					return false;
//				}
//			}
//			if(local.last_modification_time.before(d)){
//				return true;
//			}else{
//				return false;
//			}
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
//		return local.diff_approach != online.diff_approach || local.diff_drain != online.diff_drain ||
//			     local.diff_overall != online.diff_overall || local.diff_size != online.diff_size ||
//			     lengthChanged() || starRatingChanged();
	}
	
	private boolean lengthChanged(){
		//System.out.println("Len delta: " + Math.abs((local.total_length / 1000.0D) - online.total_length));
		return Math.abs((local.total_length / 1000.0D) - online.total_length) > 1;//local length in ms, online length in seconds 
	}
	
	private boolean starRatingChanged(){
		return Math.floor(local.difficultyrating * 1000.0D) != Math.floor(online.difficultyrating * 1000.0D);//factor in floating-point rounding errors
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
			}
		} catch (Throwable e1) {
			JOptionPane.showMessageDialog(VersionChecker.frame, "An exception occurred", "Version Checker", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void setOnlineData(BeatmapData data){
		this.online = data;
	}

	protected BeatmapItem(File file, BeatmapData data){
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