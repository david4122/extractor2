package org.gextractor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.lang.reflect.*;
import java.util.regex.*;
import java.io.*;
import java.net.*;
import javax.swing.filechooser.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.util.*;
import javax.imageio.*;

public class Main extends JFrame{

	class HistWindow extends JFrame {
		JList<String>list=new JList<String>();
		DefaultListModel<String>lmodel=new DefaultListModel<String>();
		JButton clear=new JButton("Clear history");
		JTextField tf=new JTextField(20);
	
		HistWindow(){
			super("History");
			try{
				setIconImage(ImageIO.read(getClass().getResource("/resources/extract.png")));
			} catch(IOException e){
				System.err.println("Cannot load icon image: "+e);
			}
			setSize(500, 400);
			setLocation(300, 500);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setLayout(new BorderLayout());
			add(new JScrollPane(list), BorderLayout.CENTER);
			list.setModel(lmodel);
			loadHistory();
			JPanel topbar=new JPanel(new FlowLayout());
			add(topbar, BorderLayout.NORTH);
			topbar.add(clear);
			topbar.add(new JLabel("Search in history"));
			topbar.add(tf);
			
			list.addListSelectionListener(new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent e){
					try{
						Main.this.last=Class.forName(lmodel.getElementAt(list.getSelectedIndex()));
						Main.this.scan(Main.this.last);
					} catch(ClassNotFoundException ex){
						JOptionPane.showMessageDialog(HistWindow.this, "Class not found", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			clear.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					lmodel.clear();
					hist.delete();
				}
			});
			tf.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					lmodel.clear();
					loadHistory();
				}
			});
		}
		
		void loadHistory(){
			try{
				BufferedReader history=new BufferedReader(new FileReader(hist));
				String line;
				Pattern p=Pattern.compile(tf.getText());
				while((line=history.readLine())!=null){
					if(p.matcher(line).find())
						lmodel.addElement(line);
				}
				history.close();
				setVisible(true);
			} catch(FileNotFoundException e){
				JOptionPane.showMessageDialog(this, "No history", "Error", JOptionPane.ERROR_MESSAGE);
			} catch(IOException e){
				JOptionPane.showMessageDialog(this, "Error while reading file", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	JTextField query=new JTextField(20);
	DefaultListModel<String>resultsModel=new DefaultListModel<String>();
	JList<String>results=new JList<String>(resultsModel);
	JCheckBox fields=new JCheckBox("Fields");
	JCheckBox ctors=new JCheckBox("Constructors");
	JCheckBox methods=new JCheckBox("Methods");
	JCheckBox shortNames=new JCheckBox("Short names");
	JTextField searchPhrase=new JTextField(10);
	JButton clear=new JButton();
	JTextArea ifaces=new JTextArea();
	JTextArea superclasses=new JTextArea();
	JCheckBox declared=new JCheckBox("Declared");
	JFileChooser fileChooser=new JFileChooser();
	JButton openFile=new JButton("<html><center>Load class from<br/>*.jar file");
	JButton rescan=new JButton();
	File hist=new File("hist");
	JButton showHist=new JButton();
	Class<?>last;

	Main(){
		super("Java Class Extractor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try{
			setIconImage(ImageIO.read(getClass().getResource("/resources/extract.png")));
			clear.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/delete.png"))));
		} catch(IOException e){
			System.err.println("Could not load icon image: "+e);
		}
		setSize(800, 550);
		setLocation(100, 100);
		setLayout(new BorderLayout());
		JPanel topbar=new JPanel(new FlowLayout());
		topbar.add(new JLabel("Full class name: "));
		topbar.add(query);
		query.requestFocus(true);
		topbar.add(new JLabel("Keyword or regex to search for:"));
		JPanel searchPanel=new JPanel(new FlowLayout());
		topbar.add(searchPanel);
		((FlowLayout)(searchPanel.getLayout())).setHgap(0);
		searchPanel.add(searchPhrase);
		searchPanel.add(clear);
		clear.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				searchPhrase.setText("");
				scan(last);
			}
		});
		add(topbar, BorderLayout.NORTH);
		JPanel opts=new JPanel();
		opts.setBorder(new TitledBorder("Options"));
		opts.setLayout(new BoxLayout(opts, BoxLayout.Y_AXIS));
		opts.add(fields);
		fields.setToolTipText("Print fields");
		opts.add(ctors);
		ctors.setSelected(true);
		ctors.setToolTipText("Print class' constructors");
		opts.add(methods);
		methods.setSelected(true);
		methods.setToolTipText("Print methods");
		JSeparator sep=new JSeparator();
		sep.setMaximumSize(new Dimension(1000, 10));
		opts.add(sep);
		opts.add(shortNames);
		shortNames.setToolTipText("Print only simple name of type");
		shortNames.setSelected(true);
		opts.add(declared);
		declared.setToolTipText("Print declared fields and methods instead of accessible");
		sep=new JSeparator();
		sep.setMaximumSize(new Dimension(1000, 10));
		opts.add(sep);
		opts.add(openFile);
		opts.add(Box.createRigidArea(new Dimension(0, 10)));
		opts.add(showHist);
		showHist.setMaximumSize(openFile.getMaximumSize());
		opts.add(Box.createRigidArea(new Dimension(0, 10)));
		opts.add(rescan);
		rescan.setMaximumSize(openFile.getMaximumSize());
		opts.add(Box.createRigidArea(new Dimension(0,20)));
		add(opts, BorderLayout.WEST);
		JPanel center=new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center.add(new JScrollPane(results));
		results.setDropTarget(new DropTarget(){
			public void drop(DropTargetDropEvent e){
				try{
					e.acceptDrop(DnDConstants.ACTION_COPY);
					java.util.List<File>files=(java.util.List<File>)e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
					String className=JOptionPane.showInputDialog(Main.this, "Enter full class name", "");
					URL urls[]=new URL[files.size()];
					for(int i=0;i<files.size();i++)
						urls[i]=files.get(i).toURL();
					last=new URLClassLoader(urls).loadClass(className);
					addToHistory(last);
					scan(last);
				} catch(NoClassDefFoundError er){
					JOptionPane.showMessageDialog(Main.this, "Class def not found:\n"+er, "ERROR", JOptionPane.ERROR_MESSAGE);
				} catch(ClassNotFoundException ex){
					JOptionPane.showMessageDialog(Main.this, "Class not found: "+ex, "ClassNotFoundException", JOptionPane.ERROR_MESSAGE);
				} catch(MalformedURLException ex){
					JOptionPane.showMessageDialog(Main.this, "URL malformed!", "MalformedURLException", JOptionPane.ERROR_MESSAGE);
				} catch(UnsupportedFlavorException ex){
					JOptionPane.showMessageDialog(Main.this, ""+ex, "Exception", JOptionPane.ERROR_MESSAGE);
				} catch(PatternSyntaxException ex){
					JOptionPane.showMessageDialog(Main.this, "Pattern exception: "+ex, "Pattern", JOptionPane.ERROR_MESSAGE);
				} catch(Exception ex){
					JOptionPane.showMessageDialog(Main.this, "EXCEPTION: "+ex, "Exception", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		add(center, BorderLayout.CENTER);
		JPanel south=new JPanel();
		south.setLayout(new BoxLayout(south, BoxLayout.X_AXIS));
		south.add(ifaces);
		ifaces.setEditable(false);
		ifaces.setBorder(new TitledBorder("Implemented interfaces"));
		south.add(superclasses);
		superclasses.setEditable(false);
		superclasses.setBorder(new TitledBorder("Hierarchy"));
		add(south, BorderLayout.SOUTH);
		fileChooser.setFileFilter(new FileNameExtensionFilter("JAR files", "jar"));

		final ActionListener al=new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					last=Class.forName(query.getText());
					addToHistory(last);
					scan(last);
				} catch(ClassNotFoundException ex){
					JOptionPane.showMessageDialog(Main.this, "Class not found:\n"+ex, "ClassNotFoundException", JOptionPane.ERROR_MESSAGE);
					return;
				} catch(PatternSyntaxException ex){
					JOptionPane.showMessageDialog(Main.this, "Pattern exception: "+ex, "Pattern", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		query.addActionListener(al);
		searchPhrase.addActionListener(al);

		Action rescanAction=new AbstractAction("Rescan"){
			@Override
			public void actionPerformed(ActionEvent e){
				try{
					scan(last);
				} catch(PatternSyntaxException ex){
					JOptionPane.showMessageDialog(Main.this, "Pattern exception: "+ex, "Pattern", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		rescan.setAction(rescanAction);
		rescan.setEnabled(false);
		rescan.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "Rescan");
		rescan.getActionMap().put("Rescan", rescanAction);

		openFile.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int respond=fileChooser.showOpenDialog(Main.this);
				if(respond==JFileChooser.APPROVE_OPTION){
					File file=fileChooser.getSelectedFile();
					String className=JOptionPane.showInputDialog(Main.this, "Enter full class name", "");
					try{
						URL url=file.toURL();
						last=new URLClassLoader(new URL[]{url}).loadClass(className);
						addToHistory(last);
						scan(last);
					} catch(NoClassDefFoundError er){
						JOptionPane.showMessageDialog(Main.this, "Class def not found:\n"+er, "ERROR", JOptionPane.ERROR_MESSAGE);
					} catch(ClassNotFoundException ex){
						JOptionPane.showMessageDialog(Main.this, "Class not found:\n"+file+' '+ex, "ClassNotFoundException", JOptionPane.ERROR_MESSAGE);
					} catch(MalformedURLException ex){
						JOptionPane.showMessageDialog(Main.this, "URL malformed!", "MalformedURLException", JOptionPane.ERROR_MESSAGE);
					} catch(PatternSyntaxException ex){
						JOptionPane.showMessageDialog(Main.this, "Pattern exception: "+ex, "Pattern", JOptionPane.ERROR_MESSAGE);
					} catch(Exception ex){
						JOptionPane.showMessageDialog(Main.this, "EXCEPTION: "+ex, "Exception", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		Action histAction=new AbstractAction("Show history"){
			@Override
			public void actionPerformed(ActionEvent e){
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						new HistWindow();
					}
				});
			}
		};
		showHist.setAction(histAction);
		showHist.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK), "Show history");
		showHist.getActionMap().put("Show history", histAction);
		
		setVisible(true);
	}

	void scan(Class<?>cl)throws PatternSyntaxException{
		if(!rescan.isEnabled())
			rescan.setEnabled(true);
		resultsModel.clear();
		ifaces.setText("");
		superclasses.setText("");
		query.setText(cl.getName());
		Pattern p;
		Pattern phrase;
		if(searchPhrase.getText().length()>0)
			phrase=Pattern.compile(searchPhrase.getText());
		else
			phrase=Pattern.compile(".*");
		
		if(shortNames.isSelected())
			p=Pattern.compile("\\w+\\.");
		else
			p=Pattern.compile("");
		String s;
		if(fields.isSelected()){
			resultsModel.addElement(String.format("<html><div style=\"margin: 5px; width: %d; text-align: center;\">FIELDS</div></html>", results.getWidth()-50));
			if(declared.isSelected()){
				for(Field i: cl.getDeclaredFields())
					if(phrase.matcher(i.toString()).find())
						resultsModel.addElement(p.matcher(i.toString()).replaceAll("")+'\n');
			} else{
				for(Field i: cl.getFields()){
					if(phrase.matcher(i.toString()).find())
						resultsModel.addElement(p.matcher(i.toString()).replaceAll("")+'\n');
				}
			}
		}
		if(ctors.isSelected()){
			resultsModel.addElement(String.format("<html><div style=\"margin: 5px; width: %d; text-align: center;\">CONSTRUCTORS</div></html>", results.getWidth()-50));
			for(Constructor<?>i: cl.getConstructors())
				if(phrase.matcher(i.toString()).find())
					resultsModel.addElement(p.matcher(i.toString()).replaceAll("")+'\n');
		}
		if(methods.isSelected()){
			resultsModel.addElement(String.format("<html><div style=\"margin: 5px; width: %d; text-align: center;\">METHODS</div></html>", results.getWidth()-50));
			if(declared.isSelected()){
				for(Method i: cl.getDeclaredMethods())
					if(phrase.matcher(i.toString()).find())
						resultsModel.addElement(p.matcher(i.toString()).replaceAll("")+'\n');
			} else {
				for(Method i: cl.getMethods())
					if(phrase.matcher(i.toString()).find())
						resultsModel.addElement(p.matcher(i.toString()).replaceAll("")+'\n');
			}
		}
		Class<?>c=cl;
		for(Class<?>i: cl.getInterfaces())
			ifaces.append(i.getName()+"\n");
		while(c!=null){
			superclasses.insert(c.getName()+'\n', 0);
			c=c.getSuperclass();
		}
		if(cl.isInterface())
			JOptionPane.showMessageDialog(Main.this, "This is an intefrace!", "Java class extractor", JOptionPane.INFORMATION_MESSAGE);
	}
	
	void addToHistory(Class<?>cl){
		try {
			PrintWriter history=new PrintWriter(new FileOutputStream("hist", true));
			history.println(cl.getName());
			history.close();
		} catch(FileNotFoundException e){
			JOptionPane.showMessageDialog(Main.this, "No history file", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void main(String[] args){
		try{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		} catch(ClassNotFoundException|InstantiationException|IllegalAccessException|UnsupportedLookAndFeelException e){
			System.err.println("Could not load GTK look and feel: "+e);
		}
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				new Main();
			}
		});
	}
}
