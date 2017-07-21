package me.roan.versionchecker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import me.roan.infinity.graphics.ui.RListUI;
import me.roan.infinity.graphics.ui.RListUI.ListRenderable;
import me.roan.versionchecker.Database.BeatmapData;

public class VersionChecker {
	
	public static File OSUDIR = new File("D://osu!");

	public static void main(String[] args){
		try {
			Database.readDatabase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(BeatmapData data : Database.maps){
			if(!(data.status == 4 || data.status == 5)){//4=ranked,5=approved,7=loved?,2=graveyard/pending,1=not submited>
				System.out.println(data.status + " " + data.title);
			}
		}
		
		
	}
	

	

	public class ReplaySelectionTab extends JPanel{

		/**
		 * 
		 */
		private static final long serialVersionUID = -2744302397899309168L;

		protected ReplaySelectionTab(){
			this.setLayout(new BorderLayout());
			JPanel content = new JPanel(new GridLayout(1, 2));
			JPanel mappanel = new JPanel(new BorderLayout());
			JPanel playpanel = new JPanel(new BorderLayout());
			JList<ListRenderable> beatmaps = new JList<ListRenderable>(FileManager.getBeatmaps());
			DefaultListModel<ListRenderable> m = new DefaultListModel<ListRenderable>();
			JList<ListRenderable> beatmapsimp = new JList<ListRenderable>(m);
			
			
			beatmaps.setUI(new RListUI());
			beatmapsimp.setUI(new RListUI());
			
			((RListUI)beatmaps.getUI()).setBackground(Color.LIGHT_GRAY.brighter());
			beatmaps.setFixedCellHeight(16 * 3);
			beatmaps.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			JTabbedPane beatmap = new JTabbedPane();
			beatmap.add(new JScrollPane(beatmaps), "Local beatmaps");
			
			mappanel.add(beatmap, BorderLayout.CENTER);
			
			
			JPanel searchrpanel = new JPanel(new FlowLayout());
			JPanel searchbpanel = new JPanel(new FlowLayout());
			
			
			playpanel.add(searchrpanel, BorderLayout.PAGE_START);
			mappanel.add(searchbpanel, BorderLayout.PAGE_START);
			
			content.add(playpanel);
			content.add(mappanel);
			
			this.add(content, BorderLayout.CENTER);
			
			JPanel header = new JPanel(new BorderLayout());
			
			
			


			this.add(header, BorderLayout.PAGE_START);
		}
	}

}
