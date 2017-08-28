package me.roan.versionchecker;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import me.roan.infinity.graphics.ui.RListUI.ListRenderable;
import me.roan.versionchecker.BeatmapData.LocalBeatmapData;
import me.roan.versionchecker.BeatmapData.OnlineBeatmapData;

/**
 * This class combines the online data
 * of a beatmap, the local data of a beatmap
 * , the graphical display of a beatmap and
 * other types of metadata.
 * @author RoanH
 */
public final class BeatmapItem implements ListRenderable{
	/**
	 * The .osu file for this beatmap
	 */
	public final File file;
	/**
	 * Font used for the beatmap title
	 */
	protected static final Font ftitle = new Font("Dialog", Font.BOLD, 12);
	/**
	 * Font used for normal text
	 */
	protected static final Font finfo = new Font("Dialog", Font.PLAIN, 11);
	/**
	 * |Font used for normal bold text
	 */
	protected static final Font finfob = new Font("Dialog", Font.BOLD, 11);
	/**
	 * Pink foreground color
	 */
	public static final Color PINK = new Color(255, 102, 204);
	/**
	 * Color used for beatmaps that are selected
	 */
	public static final Color SELECTION_COLOR = new Color(0.0F, 1.0F, 1.0F, 0.3F);
	/**
	 * Background color used when the map is set to be downloaded
	 */
	public static final Color DL_TRUE = new Color(0.0F, 1.0F, 0.0F, 0.15F);
	/**
	 * Background color used when the map is set to not be downloaded
	 */
	public static final Color DL_FALSE = new Color(1.0F, 0.0F, 0.0F, 0.15F);
	/**
	 * Background color for the 'download' button
	 */
	public static final Color DL_TRUE_2X = new Color(0.0F, 1.0F, 0.0F, 0.3F);
	/**
	 * Background color for the 'don't download' button
	 */
	public static final Color DL_FALSE_2X = new Color(1.0F, 0.0F, 0.0F, 0.3F);
	/**
	 * Local data for this beatmap from osu!.db
	 */
	public final LocalBeatmapData local;
	/**
	 * Online data for this beatmap
	 */
	public OnlineBeatmapData online;
	/**
	 * Most recent y coordinate for this
	 * list item in the list
	 */
	private int y;
	/**
	 * Most recent width for this list item
	 */
	private int w;
	/**
	 * Whether or not the preview is playing
	 */
	private boolean playing = false;
	/**
	 * The 'to update' status for this beatmap
	 */
	protected Boolean download = null;
	/**
	 * Whether or not 'update' selection
	 * controls are shown
	 */
	protected boolean showControls = false;
	/**
	 * Total number of maps that have
	 * their 'update' state selected.
	 * The update button is enabled when
	 * this number is equal to the number
	 * of beatmaps with an update
	 */
	protected static int choiceMade = 0;

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
		g.drawImage(FileManager.icons.get(local.setid), x + 4, y + 4, null);//40 - 71
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

	/**
	 * Converts the local status ID
	 * code to something readable
	 * @param id The status ID
	 * @return The status as text
	 */
	//0=unknow,4=ranked,5=approved,7=loved?,2=graveyard/pending,1=not submited>
	private static String getStatusLocal(int id){
		return id == 1 ? "Not submitted" : (id == 2 ? "Pending" : (id == 4 ? "Ranked" : (id == 5 ? "Approved" : (id == 7 ? "Loved" : "Unknown"))));
	}
	
	/**
	 * Converts the online status ID
	 * code to something readable
	 * @param id The status ID
	 * @return The status as text
	 */
	//4=loved,3=qualified,2=approved,1=ranked,0=pending,-1= WIP,-2=graveyard
	private static String getStatusOnline(int id){
		return id == 4 ? "Loved" : (id == 3 ? "Qualified" : (id == 2 ? "Approved" : (id == 1 ? "Ranked" : (id == 0 ? "Pending" : (id == -1 ? "WIP" : (id == -2 ? "Graveyard" : "Unknown"))))));
	}
	
	/**
	 * Check to see if an update
	 * exists for this beatmap
	 * @return Whether or not an
	 *         update exists
	 */
	protected boolean mapChanged(){
		return online.generated;
	}
	
	/**
	 * Checks to see if the ranked state
	 * of this beatmap changed
	 * @return Whether or not the ranked
	 *         status of this beatmap changed
	 */
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

	/**
	 * Stop the visual state associated
	 * with playing the beatmap preview
	 */
	public void cancelPlayingState(){
		playing = false;
	}

	/**
	 * Called when a mouse event happens
	 * on this beatmap item
	 * @param e The mouse event that occurred
	 */
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
				if(download == null){
					choiceMade++;
				}
				download = true;
				VersionChecker.enableUpdateButton();
				e.getComponent().repaint();
			}else if(showControls && e.getX() > w - 80 - 80 - 76 && e.getX() < w - 4 - 80 - 76 && e.getY() > y + 25 && e.getY() < y + 44){
				if(download == null){
					choiceMade++;
				}
				download = false;
				e.getComponent().repaint();
			}
		} catch (Throwable e1) {
			JOptionPane.showMessageDialog(VersionChecker.frame, "An exception occurred", "Version Checker", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Sets the online data for this beatmap item
	 * @param data The online data for this map
	 */
	public void setOnlineData(OnlineBeatmapData data){
		this.online = data;
		showControls = data.generated;
	}

	/**
	 * Creates a new beatmap item
	 * with the given .osu file 
	 * and local beatmap data
	 * @param file The .osu file
	 *        for this beatmap
	 * @param data The local data
	 *        for this beatmap
	 */
	protected BeatmapItem(File file, LocalBeatmapData data){
		this.file = file;
		this.local = data;
		FileManager.getBeatmapIcon(this);
	}
}