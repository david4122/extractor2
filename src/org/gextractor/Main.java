package org.gextractor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.lang.reflect.*;
import java.util.regex.*;

public class Main extends JFrame{
	JTextField query=new JTextField(20);
	JTextArea results=new JTextArea();
	JCheckBox fields=new JCheckBox("Fields");
	JCheckBox ctors=new JCheckBox("Constructors");
	JCheckBox methods=new JCheckBox("Methods");
	JCheckBox shortNames=new JCheckBox("Short names");
	JTextField searchPhrase=new JTextField(10);
	JButton rescan=new JButton("Research");
	JCheckBox deep=new JCheckBox("Deep");
	JTextArea ifaces=new JTextArea();
	JTextArea tree=new JTextArea();

	Main(){
		super("Java Class Extractor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 700);
		setLocation(50,50);
		setLayout(new BorderLayout());
		JPanel topbar=new JPanel(new FlowLayout());
		topbar.add(new JLabel("Full class name: "));
		topbar.add(query);
		topbar.add(new JLabel("Keyword or pattern to search:"));
		topbar.add(searchPhrase);
		add(topbar, BorderLayout.NORTH);
		JPanel opts=new JPanel();
		opts.setLayout(new BoxLayout(opts, BoxLayout.Y_AXIS));
		opts.add(fields);
		fields.setSelected(true);
		opts.add(ctors);
		ctors.setSelected(true);
		opts.add(methods);
		methods.setSelected(true);
		JSeparator sep=new JSeparator();
		sep.setMaximumSize(new Dimension(1000, 10));
		opts.add(sep);
		opts.add(shortNames);
		shortNames.setSelected(true);
		opts.add(deep);
		sep=new JSeparator();
		sep.setMaximumSize(new Dimension(1000, 10));
		opts.add(sep);
		opts.add(rescan);
		rescan.setEnabled(false);
		add(opts, BorderLayout.WEST);
		JPanel center=new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center.add(new JScrollPane(results));
		results.setEditable(false);
		add(center, BorderLayout.CENTER);
		JPanel south=new JPanel();
		south.setLayout(new BoxLayout(south, BoxLayout.X_AXIS));
		south.add(ifaces);
		south.add(tree);
		add(south, BorderLayout.SOUTH);

		final ActionListener al=new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Class<?>cl;
				try{
					cl=Class.forName(query.getText());
				} catch(ClassNotFoundException ex){
					results.setText("Class not found: "+ex);
					return;
				}
				results.setText("");
				ifaces.setText("");
				tree.setText("");
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
					results.append("\tFIELDS:\n");
					for(Field i: cl.getDeclaredFields()){
						if(phrase.matcher(i.toString()).find())
							results.append(p.matcher(i.toString()).replaceAll("")+'\n');
					}
					if(deep.isSelected()){
						Class<?>base=cl;
						do{
							for(Field i: base.getDeclaredFields()){
								if(phrase.matcher(i.toString()).find())
									results.append(p.matcher(i.toString()).replaceAll("")+'\n');
							}
							base=base.getSuperclass();
						} while(base!=Object.class);
					}
				}
				if(ctors.isSelected()){
					results.append("\tCONSTRUCTORS:\n");
					for(Constructor<?>i: cl.getConstructors())
						if(phrase.matcher(i.toString()).find())
							results.append(p.matcher(i.toString()).replaceAll("")+'\n');
				}
				if(methods.isSelected()){
					results.append("\tMETHODS\n");
					for(Method i: cl.getDeclaredMethods())
						if(phrase.matcher(i.toString()).find())
							results.append(p.matcher(i.toString()).replaceAll("")+'\n');
					if(deep.isSelected()){
						Class<?>base=cl;
						do{
							for(Method i: base.getDeclaredMethods()){
								if(phrase.matcher(i.toString()).find())
									results.append(p.matcher(i.toString()).replaceAll("")+'\n');
							}
							base=base.getSuperclass();
						} while(base!=Object.class);
					}
				}
				Class<?>c=cl;
				for(Class<?>i: cl.getInterfaces())
					ifaces.append(i.getName()+"\n");
				while(cl!=Object.class){
					tree.append(cl.getName()+'\n');
					cl=cl.getSuperclass();
				}
				if(!rescan.isEnabled())
					rescan.setEnabled(true);
			}
		};
		query.addActionListener(al);
		searchPhrase.addActionListener(al);
		
		rescan.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				al.actionPerformed(new ActionEvent(al, 0, ""));
			}
		});

		setVisible(true);
	}

	public static void main(String[] args){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				new Main();
			}
		});
	}
}
