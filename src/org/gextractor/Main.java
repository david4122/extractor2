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
	JTextArea additional=new JTextArea();
	JCheckBox fields=new JCheckBox("Fields");
	JCheckBox ctors=new JCheckBox("Constructors");
	JCheckBox methods=new JCheckBox("Methods");
	JCheckBox shortNames=new JCheckBox("Short names");
	JCheckBox ifaces=new JCheckBox("Intefaces");
	JCheckBox tree=new JCheckBox("Base classes");
	JTextField searchPhrase=new JTextField(10);
	JButton research=new JButton("Research");
	JCheckBox deep=new JCheckBox("Deep");

	Main(){
		super("Java Class Extractor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 700);
		setLayout(new BorderLayout());
		JPanel topbar=new JPanel(new FlowLayout());
		topbar.add(new JLabel("Full class name: "));
		topbar.add(query);
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
		opts.add(ifaces);
		ifaces.setSelected(true);
		opts.add(tree);
		tree.setSelected(true);
		opts.add(sep);
		opts.add(shortNames);
		shortNames.setSelected(true);
		opts.add(deep);
		sep=new JSeparator();
		sep.setMaximumSize(new Dimension(1000, 10));
		opts.add(sep);
		opts.add(research);
		research.setEnabled(false);
		add(opts, BorderLayout.WEST);
		JPanel center=new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center.add(new JScrollPane(results));
		results.setEditable(false);
		add(center, BorderLayout.CENTER);
		add(additional, BorderLayout.SOUTH);
		additional.setPreferredSize(new Dimension(getSize().width, 200));
		additional.setEditable(false);

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
					if(deep.isSelected()){
						Class<?>base=cl;
						do{
							for(Constructor<?>i: base.getConstructors()){
								if(phrase.matcher(i.toString()).find())
									results.append(p.matcher(i.toString()).replaceAll("")+'\n');
							}
							base=base.getSuperclass();
						} while(base!=Object.class);
					}
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
				if(ifaces.isSelected()){
					additional.append("Implemented interfaces:\n");
					for(Class<?>i: cl.getInterfaces())
						additional.append(i.getName()+"\n");
				}
				if(!research.isEnabled())
					research.setEnabled(true);
			}
		};
		query.addActionListener(al);
		searchPhrase.addActionListener(al);
		
		research.addActionListener(new ActionListener(){
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
