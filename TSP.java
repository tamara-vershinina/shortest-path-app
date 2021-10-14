import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

public class TSP {
	private static Window window;
	
	public static void main(String[] args) {
		//open the window with the program's interface
		window = new TSP.Window();	 

	}
	
	//runs when the "Build Path" button is pressed
	public static ArrayList<Order> run(String text, JTextArea output) {
		ArrayList<Order> ordersLeft = new ArrayList<Order>();
		ArrayList<Order> ordersSorted = new ArrayList<Order>();

		double fStartingPoint = 53.38197;
		double lStartingPoint = -6.59274;
		
		Order startingPoint = new TSP.Order(0, 0, fStartingPoint, lStartingPoint); 
		
		//split the text input, create Order objects
		 for(String orderStr : text.split("\n")) {
			 String[] array = orderStr.split(",");
			 int id = Integer.parseInt(array[0]);
			 int minutes = Integer.parseInt(array[2]);
			 double latitude = Double.parseDouble(array[3]);
			 double longitude = Double.parseDouble(array[4]);
			 Order newOrder = new TSP.Order(id, minutes, latitude, longitude); 
			 ordersLeft.add(newOrder);
		 }
		 
		 //System.out.println("Shortest: " + findShortest(startingPoint, orders, deliveredOrders).id);
		 double totalAngryTime = 0;   //gets calculated as we get to each order; 1km/1min
		 double totalTimePassed = 0;  //time from the moment when the delivery man left the restaurant
		 
		 //build the delivery path
		 IndexDistance indDis = findShortest(startingPoint, ordersLeft);
		 int index = indDis.index;
		 Order closestOrder = ordersLeft.get(index); //closest order just found
		 ordersLeft.remove(index);
		 ordersSorted.add(closestOrder);
		 totalTimePassed += indDis.distance;
		 if((totalTimePassed + closestOrder.minutes) > 30) {
			 totalAngryTime += (totalTimePassed + closestOrder.minutes) - 30;
		 }
		 
		 while(!ordersLeft.isEmpty()) {
			 indDis = findShortest(closestOrder, ordersLeft);
			 index = indDis.index;
			 closestOrder = ordersLeft.get(index); //closest order just found
			 ordersLeft.remove(index);
			 ordersSorted.add(closestOrder);
			 totalTimePassed += indDis.distance;
			 if((totalTimePassed + closestOrder.minutes) > 30) {
				 totalAngryTime += (totalTimePassed + closestOrder.minutes) - 30;
			 }
		 }
		System.out.println("Total Angry Time = " + totalAngryTime);
		 
		 String path = new String("");
		 for(int i = 0; i < ordersSorted.size(); i++) {
			 if(i != ordersSorted.size()-1) {
				 path += ordersSorted.get(i).id + ",";
			 }
			 else {
				 path += ordersSorted.get(i).id + "";
			 }
		 }
		output.setText(path);
		 
		//return an arrayList of sorted orders
		return ordersSorted;
	}
	
	//create new instances of Order objects
	public static class Order {
		private int id;
		private int minutes;
		private double latitude;
		private double longitude;
		
		public Order(int id, int minutes, double latitude, double longitude) {
			this.id = id;
			this.minutes = minutes;
			this.latitude = latitude;
			this.longitude = longitude;
		}
	}
	
	//create IndexDistance objects
	public static class IndexDistance {  //placeholder for two values
		private int index;
		private double distance;
		
		public IndexDistance(int index, double distance) {
			this.index = index;
			this.distance = distance;
		}
	}
	
	//calculate the distance between two order locations
	public static double findDistanceBetween(Order order1, Order order2) {
		double f1 = Math.toRadians(order1.latitude);  //latitude
		double l1 = Math.toRadians(order1.longitude);  //longitude
		
		double f2 = Math.toRadians(order2.latitude);
		double l2 = Math.toRadians(order2.longitude);
		  
		 
		double radius = 6371;
		double distance; 
	 
		//From chord length
		double deltaX = Math.cos(f2) * Math.cos(l2) - Math.cos(f1) * Math.cos(l1);
		double deltaY = Math.cos(f2) * Math.sin(l2) - Math.cos(f1) * Math.sin(l1);
		double deltaZ = Math.sin(f2) - Math.sin(f1);
		double C = Math.sqrt(Math.pow(deltaX,2) + Math.pow(deltaY,2) + Math.pow(deltaZ,2));
		double centralAngle = 2 * Math.asin(C/2);
		
		distance = radius * centralAngle;
		return distance;
	}
	
	//find the path to the nearest order location
	public static IndexDistance findShortest(Order order, ArrayList<Order> ordersLeft) {
		double minDist = 100000; //random big number to begin with
		int orderWithMinDist = 100000;
	
		for(int i = 0; i < ordersLeft.size(); i++) {
			double dist = findDistanceBetween(order, ordersLeft.get(i));
			if(dist < minDist) {
				minDist = dist;
				orderWithMinDist = i;
			}
			
		}
		IndexDistance indDis = new IndexDistance(orderWithMinDist, minDist);
		return indDis;
	}
	
	//handle rendering the image of the map
	//and drawing the path on the map
	public static class ImagePanel extends JPanel{
		
		private BufferedImage map;
		private ArrayList<Point> deliveries;
		private static final double MIN_LONG = -6.71319;
		private static final double MAX_LONG = -6.45509;
		private static final double MIN_LAT = 53.28271;
		private static final double MAX_LAT = 53.41318;
		
		private static final double START_LAT = 53.38197;
		private static final double START_LONG = -6.59274;
		
		public ImagePanel() {
			super(new BorderLayout());
			try {
				map = ImageIO.read(new File("resources/map.png"));
			}
			catch(IOException ex) {
	            System.out.println("Error");				
			}
			deliveries = new ArrayList<>();
		}
		
		public void updateRoute(ArrayList<Order> route) {
	        deliveries.clear();

	        for (final Order order : route) {
	            final Point point = new Point();
	            point.x = (int) Math.round(
	                getSize().width * 1.0 / (MAX_LONG - MIN_LONG) * (order.longitude - MIN_LONG)
	            );
	            point.y = (int) Math.round(
	                getSize().height - getSize().height * 1.0 / (MAX_LAT - MIN_LAT) * (order.latitude - MIN_LAT)
	            );
	            deliveries.add(point);
	        }
	        repaint();
	    }
		
		@Override
		public void paintComponent(Graphics g) {
		     g.drawImage(map, 0, 0, getSize().width, getSize().height, null);

		      final Point start = new Point();
		      start.x = (int) Math.round(
		        getSize().width * 1.0 / (MAX_LONG - MIN_LONG) * (START_LONG - MIN_LONG)
		      );
		      start.y = (int) Math.round(
		        getSize().height - getSize().height * 1.0 / (MAX_LAT - MIN_LAT) * (START_LAT - MIN_LAT)
		      );

		      g.setColor(Color.RED);
		      g.fillOval(start.x - 5, start.y - 5, 10, 10);

		      g.setColor(Color.BLACK);
		      Point prevPoint = start;
		      for (final Point point : deliveries) {
		        g.drawLine(point.x, point.y, prevPoint.x, prevPoint.y);
		        g.fillOval(point.x - 5, point.y - 5, 10, 10);
		        prevPoint = point;
		      }
		}	
	}
	
	//create windowFrame
	//position the main elements of the GUI
	public static class Window {
		JFrame windowFrame;
		
		
		public Window() {
			JPanel p = new JPanel(new BorderLayout());
			p.setBackground(Color.decode("#960900"));
			
			//pW
			JPanel pW = new JPanel(new BorderLayout());
			pW.setBackground(Color.decode("#960900"));
			pW.setPreferredSize(new Dimension(25, 25));
			p.add(pW, BorderLayout.WEST);
			
			
			//pE
			JPanel pE = new JPanel(new BorderLayout());
			pE.setBackground(Color.decode("#960900"));
			pE.setPreferredSize(new Dimension(25, 25));
			p.add(pE, BorderLayout.EAST);
			
			//pN
			
			ApacheLogoPanel apachePanel = new ApacheLogoPanel();
			apachePanel.setPreferredSize(new Dimension(600, 50));
			p.add(apachePanel, BorderLayout.NORTH);
			
			//pC
			ImagePanel pC = new ImagePanel(); //MAP HERE
			pC.setBackground(Color.decode("#960900"));
			pC.setPreferredSize(new Dimension(600, 405));
			p.add(pC, BorderLayout.CENTER); 
			
			
			//pS
			JPanel pS = new JPanel(new BorderLayout());
			pS.setBackground(Color.decode("#960900"));
			pS.setPreferredSize(new Dimension(600, 245));
			p.add(pS, BorderLayout.SOUTH);
			
				//pSN
			JPanel pSN = new JPanel(new BorderLayout());
			pSN.setPreferredSize(new Dimension(600, 125));
			pS.add(pSN, BorderLayout.NORTH);
			
			JPanel pSNN = new JPanel();
			pSNN.setPreferredSize(new Dimension(600, 65));
			pSNN.setBackground(Color.decode("#960900"));
			pSN.add(pSNN, BorderLayout.NORTH);
			JTextArea input = new JTextArea(150, 31); //INPUT  (rows,cols)
			input.setEnabled(true);
			input.setEditable(true);
			JScrollPane scroll1 = new JScrollPane(input, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll1.setPreferredSize(new Dimension(400, 60));
			input.setWrapStyleWord(true);
			pSNN.add(scroll1);
			
			JPanel pSNS = new JPanel(new BorderLayout());
			pSNS.setBackground(Color.decode("#960900"));
			pSNS.setPreferredSize(new Dimension(600, 60));
			pSN.add(pSNS, BorderLayout.SOUTH);
			JButton submitBtn = new JButton("Build Path");
			JTextArea output = new JTextArea(150, 31); //OUTPUT
			submitBtn.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					ArrayList<Order> route = run(input.getText(), output);
					pC.updateRoute(route);
				}
			});
			
			JPanel btnPnl = new JPanel();
			btnPnl.setBackground(Color.decode("#960900"));
			btnPnl.add(submitBtn);
			pSNS.add(btnPnl, BorderLayout.CENTER); //BUILD PATH 

			
				//pSS
			JPanel pSS = new JPanel(new BorderLayout());
			pSS.setPreferredSize(new Dimension(600, 120));
			pSS.setBackground(Color.decode("#960900"));
			pS.add(pSS, BorderLayout.SOUTH);
			
			JPanel pSSN = new JPanel();
			pSSN.setPreferredSize(new Dimension(500, 65));
			pSSN.setBackground(Color.decode("#960900"));
			pSS.add(pSSN, BorderLayout.NORTH);
			output.setEnabled(true);
			output.setEditable(true);
			JScrollPane scroll2 = new JScrollPane(output, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll2.setPreferredSize(new Dimension(400, 60));
			output.setWrapStyleWord(true);
			pSSN.add(scroll2);
							
			JPanel pSSS = new JPanel(new BorderLayout());
			pSSS.setPreferredSize(new Dimension(600, 60));
			pSSS.setBackground(Color.decode("#960900"));
			pSS.add(pSSS, BorderLayout.SOUTH);
			JButton restartBtn = new JButton("Reset"); //RESTART
			JPanel btnPnl2 = new JPanel();
			btnPnl2.setBackground(Color.decode("#960900"));
			btnPnl2.add(restartBtn);
			pSSS.add(btnPnl2);
			restartBtn.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					input.setText("");
					output.setText("");
					ArrayList<Order> emptyRoute = new ArrayList<Order>();
					pC.updateRoute(emptyRoute);
				}
			});
			
			windowFrame = new JFrame();
			//windowFrame.setBounds(0,0,100,100);
			windowFrame.setSize(new Dimension(600, 700)); //width, height
			windowFrame.setLocationRelativeTo(null);
			windowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			windowFrame.setTitle("Apache Pizza Delivery App");
			windowFrame.setResizable(false);
			windowFrame.setContentPane(p);
			//windowFrame.setLayout(null);
			
			windowFrame.setVisible(true);
		}
	}	
	
	//create and draw the Apache Logo
	public static class ApacheLogoPanel extends JPanel {
		public void paintComponent(final Graphics g) {
			g.setColor(Color.decode("#960900"));
			//g.setColor(Color.BLUE);
			g.fillRect(0, 0, getSize().width, getSize().height);
			
			g.setColor(Color.WHITE);
			g.setFont(new Font("Times New Roman", Font.BOLD, 25));
			g.drawString("Apache Pizza", getSize().width - 160, 30);
		}
	}
}
