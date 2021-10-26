import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.TextArea;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

//ChatServer
import java.io.*;
import java.net.ServerSocket;
import java.net.*;
import java.util.*;
import javax.swing.JTextArea;

public class GUIServer {

	ArrayList clientOutputStreams;
	ArrayList<String> users;
	
	int port=8591;  	//Set a number for connection between server and client
	private JFrame frame;
	private JTextArea onlineText;  //used to provide a Area that display the online user 
	private JTextArea stateText;   //used to make people understand what is going on in server
	
	public class UserThread implements Runnable
	{
		BufferedReader reader;//Receive the reaction from others
		Socket socket;
		PrintWriter client;	  //Take action when there is something received
		
		public UserThread(Socket clientSocket,PrintWriter user)
		{
			client=user;
			try
			{
				socket=clientSocket;
				InputStreamReader isReader=new InputStreamReader(socket.getInputStream());
				reader=new BufferedReader(isReader);
			}
			catch (Exception ex) 
            {
                stateText.append("Unexpected error occurs!! \n");
            }
		}
		
		public void run() //Receive the message from other clients
	       {
				//Set up to represent which state the client respond
				//And define how to react the request
	            String text, connect = "in", disconnect = "out", chat = "Chat" ;
	            String[] data;

	            try 
	            {
	            	//If there is any message receive
	                while ((text = reader.readLine()) != null) 
	                {
	                	//split the whole sentence into phases by recognize symbol ':'
	                	stateText.append("R: " + text + "\n");
	                    data = text.split(":");
	                    
	                    //If there is someone new connect
	                    if (data[2].equals(connect)) 
	                    {
	                        broadCast((data[0] + ":" + data[1] + ":" + chat));
	                    	stateText.append(data[0]+" "+data[1]+"\n");
	                        userAdd(data[0]);
	                    } 
	                    //If there is someone disconnect
	                    else if (data[2].equals(disconnect)) 
	                    {
	                        broadCast((data[0] + ":lost a connection" + ":" + chat));
	                        stateText.append(data[0]+" "+data[1]+"\n");
	                        userRemove(data[0]);
	                    } 
	                    //If there is chat occurs
	                    else if (data[2].equals(chat)) 
	                    {
	                        broadCast(text);
	                    } 
	                    //Can not determine which statement is
	                    else 
	                    {
	                        stateText.append("Error happens.Check again \n");
	                    }
	                } 
	             } 
	             catch (Exception ex) 
	             {
	                stateText.append("Lost a connection. \n");
	                ex.printStackTrace();
	                clientOutputStreams.remove(client);
	             } 
		}
	}
	//Whenever there is new client click connect,add the userName to list
	public void userAdd (String data) 
    {
        String message, add = ": :in", end = "Server: :end", name = data;
        users.add(name);
    }
	//Whenever the client click disconnect and edit the user list
	 public void userRemove (String data) 
	    {
	        String message, add = ": :in", end = "Server: :end", name = data;
	        users.remove(name);
	        
	        //String[] tempList = new String[(users.size())];
	        //users.toArray(tempList);

	        //for (String token:tempList) 
	        //{
	        //    message = (token + add);
	        //    broadCast(message);
	        //}
	        //broadCast(end);
	    }
	 
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUIServer window = new GUIServer();
					window.frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		
	}

	//Describe how the server construct
	public class ServerStart implements Runnable
    {
        @Override
        public void run() 
        {
            clientOutputStreams = new ArrayList();
            users = new ArrayList();  

            try 
            {
                ServerSocket serverSock = new ServerSocket(port);

                while (true) 
                {
				Socket clientSock = serverSock.accept();//Sending Input and output through the bottom level
				PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
				clientOutputStreams.add(writer);

				//Receive the message respond from clients
				Thread listener = new Thread(new UserThread(clientSock, writer));
				listener.start();
				stateText.append("Got a connection. \n");
                }
            }
            catch (Exception ex)
            {
                stateText.append("Error making a connection. \n");
            }
        }
    }
	
	
	public void broadCast(String text) 
    {
	Iterator it = clientOutputStreams.iterator();

        while (it.hasNext()) 
        {
            try 
            {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(text);
                stateText.append("S: " + text + "\n");
                writer.flush();
                stateText.setCaretPosition(stateText.getDocument().getLength());

            } 
            catch (Exception ex) 
            {
            	stateText.append("There Is an Error. \n");
            }
        } 
    }
	
	/**
	 * Create the application.
	 */
	public GUIServer() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 600, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Chat System - Server");
		
		//Display the online client list
		onlineText = new JTextArea();
		onlineText.setFont(new Font("微軟正黑體", Font.PLAIN, 18));
		onlineText.setBounds(10, 42, 205, 285);
		frame.getContentPane().add(onlineText);
		
		//Display what happen in the system
		stateText = new JTextArea();
		stateText.setBounds(292, 42, 292, 285);
		frame.getContentPane().add(stateText);

        
        
		JLabel lblNewLabel = new JLabel("Online");
		lblNewLabel.setFont(new Font("微軟正黑體", Font.BOLD, 20));
		lblNewLabel.setBounds(23, 10, 120, 26);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Server State");
		lblNewLabel_1.setFont(new Font("微軟正黑體", Font.BOLD, 20));
		
		lblNewLabel_1.setBounds(306, 10, 129, 22);
		frame.getContentPane().add(lblNewLabel_1);
		
		//If there is nowhere to display the information 
		//about what happened in system,would clear up the textArea
		JButton clearBtn = new JButton("Clear");
		clearBtn.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
		clearBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stateText.setText("");
			}
		});
		clearBtn.setBounds(445, 6, 129, 32);
		frame.getContentPane().add(clearBtn);
		
		
		//Set up a port that making server receives the other connection request
		JButton startBtn = new JButton("Set Up \r\nthe Server");
		startBtn.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread starter = new Thread(new ServerStart());
		        starter.start();
		        stateText.append("Server is setting up...\n");
		        stateText.append("Chat Server is listening on port "+port+".\n");
			}
		});
		startBtn.setBounds(134, 337, 205, 64);
		frame.getContentPane().add(startBtn);
		
		//Online user list will be update whenever the button click
		JButton updateBtn = new JButton("Update");
		updateBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onlineText.setText("");
				//Every user in the thread will be display
				for (String current_user : users)
		        {
		            onlineText.append(current_user);
		            onlineText.append("\n");
		        }
			}
		});
		updateBtn.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
		updateBtn.setBounds(113, 9, 102, 27);
		frame.getContentPane().add(updateBtn);
		
		//When the server would like to close the connection with other clients
		JButton endBtn = new JButton("Shut Down the Server");
		endBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try 
		        {
		            Thread.sleep(1500);                 //close the thread within the specified time 1.5secs
		        } 
		        catch(InterruptedException ex) {Thread.currentThread().interrupt();}
		        
		        broadCast("Announcement!! Server is closing and all users will be disconnected!\n");
		        stateText.append("Server closing... \n");
		        
		        //stateText.setText("");
			}
		});
		endBtn.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
		endBtn.setBounds(362, 337, 212, 64);
		frame.getContentPane().add(endBtn);
		
	}
}


