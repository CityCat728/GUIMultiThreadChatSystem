import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.TextArea;
import java.awt.BorderLayout;
import java.awt.TextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Font;

//ChatClient
import java.net.*;
import java.util.Scanner;
import java.io.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GUIClient {

	private JFrame frame;

	//ChatClient
	String hostname="localhost";//Specified the hostname
	int port=8591;              //Specified the port number
	private String userName;	//store the user name which used in connected
	ArrayList<String> users=new ArrayList();
	
	Socket socket;
	BufferedReader reader;	//sending message through reader and writer
	PrintWriter writer;     //whenever there receive a respond and make an response
	
	private JButton checkBtn;		//User name check use
	private JTextArea onlineText;	//online clients list
	private JTextArea chatText;		//Chat history bloack
	private JTextField messageField;//User can type message in it
	private JTextField nameField;	//User can type name and use it in connecting to server
	private JButton sendBtn;		//message sending button
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JLabel lblNewLabel_2;
	Boolean isConnected = false;	//Determine whether the connection is done
	private JButton updateBtn;		//used to display online list instantly
	private JButton connectBtn;		//used to receive the intention about connection
	private JButton disconnectBtn;	//used to receive the intention about disconnection
	
	 //Receive the message from server
	public void ListenThread()
    {
         Thread IncomingReader = new Thread(new IncomingReader());
         IncomingReader.start();
    }
	
	//Make a new user name add into list
	public void userAdd(String data)
    {
         users.add(data);
    }
	
	//Make the dialogue complete
	public void userRemove(String data) 
    {
		 chatText.append(data + " is now offline.\n");
    }
	
	//Respond the message received from server
	public void writeUsers() 
    {
         String[] tempList = new String[(users.size())];
         users.toArray(tempList);
         for (String token:tempList) 
         {
             //users.append(token + "\n");
         }
    }
	
	//make a message to tell server there is someone left
	public void sendDisconnect() 
    {
        String bye = (userName + ": :out");
        try
        {
            writer.println(bye); 
            writer.flush(); 
        } catch (Exception e) 
        {
            chatText.append("Could not send Disconnect message.\n");
        }
    }
	
	//disconnect from server and send message to dialogue window
	public void Disconnect() 
    {
        try 
        {
            chatText.append("Disconnected.\n");
            socket.close();
        } catch(Exception ex) {
            chatText.append("Failed to disconnect. \n");
        }
        isConnected = false;
        nameField.setEditable(true);

    }
	
	
		
		
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUIClient window = new GUIClient();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		//if(args.length<2) return;
		
		
	}

	/**
	 * Create the application.
	 */
	public GUIClient() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 700, 425);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Chat System - Client");
		
		//Display online users
		onlineText = new JTextArea();
		onlineText.setFont(new Font("微軟正黑體", Font.PLAIN, 18));
		onlineText.setBounds(10, 41, 253,275);
		frame.getContentPane().add(onlineText);
		
		//Display the chat history
		chatText = new JTextArea();
		chatText.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
		chatText.setBounds(332, 41, 342, 275);
		frame.getContentPane().add(chatText);
		
		//Receive the message that user want to deliver
		messageField = new JTextField();
		messageField.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
		messageField.setBounds(332, 326, 259, 50);
		frame.getContentPane().add(messageField);
		
		//Receive what the user name is
		nameField = new JTextField();
		nameField.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
		nameField.setBounds(120, 320, 122, 32);
		frame.getContentPane().add(nameField);
		
		//Whenever Clicked,deliver the message to server and there will be a
		//message send back from server by "broadcast"
		sendBtn = new JButton("Send");
		sendBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String nothing = "";
		        if ((messageField.getText()).equals(nothing)) {
		            messageField.setText("");
		            messageField.requestFocus();
		        } else {
		            try {
		               writer.println(userName + ":" + messageField.getText() + ":" + "Chat");
		               writer.flush(); // flushes the buffer
		            } catch (Exception ex) {
		                chatText.append("Message was not sent. \n");
		            }
		            messageField.setText("");
		            messageField.requestFocus();
		        }

		        messageField.setText("");
		        messageField.requestFocus();
			}
		});
		sendBtn.setFont(new Font("新細明體", Font.PLAIN, 18));
		sendBtn.setBounds(597, 326, 78, 50);
		frame.getContentPane().add(sendBtn);
		
		lblNewLabel = new JLabel("Online");
		lblNewLabel.setFont(new Font("微軟正黑體", Font.BOLD, 20));
		lblNewLabel.setBounds(24, 9, 94, 21);
		frame.getContentPane().add(lblNewLabel);
		
		lblNewLabel_1 = new JLabel("Chat History");
		lblNewLabel_1.setFont(new Font("微軟正黑體", Font.BOLD, 20));
		lblNewLabel_1.setBounds(536, 10, 122, 30);
		frame.getContentPane().add(lblNewLabel_1);
		
		lblNewLabel_2 = new JLabel("User Name");
		lblNewLabel_2.setFont(new Font("微軟正黑體", Font.PLAIN, 20));
		lblNewLabel_2.setBounds(10, 315, 108, 30);
		frame.getContentPane().add(lblNewLabel_2);
		
		//User name entered and checked
		checkBtn = new JButton("Confirm");
		checkBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setTitle("Chat System - Client "+nameField.getText());
			}
		});
		checkBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//userAdd(nameField.getText());
				//GUIClient client= new GUIClient("localhost",8989);
				//client.execute(textField_1.getText());
			}
		});
		checkBtn.setBounds(242, 324, 88, 23);
		frame.getContentPane().add(checkBtn);
		
		//Update the online user list instantly
		updateBtn = new JButton("Update");
		updateBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onlineText.setText("");
				for (String current_user : users)
		        {
		            onlineText.append(current_user);
		            onlineText.append("\n");
		        }
			}
		});
		updateBtn.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
		updateBtn.setBounds(145, 8, 104, 23);
		frame.getContentPane().add(updateBtn);
		
		//New user would like to make a connect by click the button
		//procedure take user deliver intention that makes server receive and deal with 
		connectBtn = new JButton("Connect");
		connectBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isConnected == false) 
		        {
		            userName = nameField.getText();
		            nameField.setEditable(false);

		            try 
		            {
		                socket = new Socket(hostname, port);
		                InputStreamReader streamreader = new InputStreamReader(socket.getInputStream());
		                reader = new BufferedReader(streamreader);
		                writer = new PrintWriter(socket.getOutputStream());
		                writer.println(userName + ":has connected.:in");
		                writer.flush(); 
		                //userAdd(userName);
		                isConnected = true; 
		            } 
		            catch (Exception ex) 
		            {
		                chatText.append("Cannot Connect: Try Again. \n");
		                nameField.setEditable(true);
		            }
		            
		            ListenThread();
		            
		        } else if (isConnected == true) 
		        {
		            chatText.append("Clent"+userName+"You are already connected. \n");
		        }
			}
		});
		connectBtn.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
		connectBtn.setBounds(10, 355, 108, 30);
		frame.getContentPane().add(connectBtn);
		
		//Whenever the user want to leave the chat system
		disconnectBtn = new JButton("Disconnect");
		disconnectBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendDisconnect();
		        Disconnect();
			}
		});
		disconnectBtn.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
		disconnectBtn.setBounds(140, 355, 128, 30);
		frame.getContentPane().add(disconnectBtn);
	}
	
	//Handle the problem of receiving message
	public class IncomingReader implements Runnable
    {
        @Override
        public void run() 
        {
        	//Set up to represent four kinds of message and represent four statement respectively
            String[] data;
            String stream, end = "end", connect = "in", disconnect = "out", chat = "Chat";

            try 
            {
                while ((stream = reader.readLine()) != null) 
                {
                	//split the message into four phases
                     data = stream.split(":");
                     //check if there is user name exists already
                     boolean flag=true;
                     for (String current_user : users)
     		        {
                    	 if(current_user.equals(data[0]))
                    		 flag=false; 
     		        }
                     //add the user name if there is no recoed in list
                     if(flag==true)
                    	 users.add(data[0]);
                     
                     //When Clients chat
                     if (data[2].equals(chat)) 
                     {
                        chatText.append(data[0] + ": " + data[1] + "\n");
                        chatText.setCaretPosition(chatText.getDocument().getLength());
                     } 
                     //there is intention to connect 
                     else if (data[2].equals(connect))
                     {
                    	 //users.add(data[0]);
                     } 
                     //there is intention to disconnect 
                     else if (data[2].equals(disconnect)) 
                     {
                         userRemove(data[0]);
                     } 
                     //there is intention to close the whole connection
                     else if (data[2].equals(end)) 
                     {
                        //users.setText("");
                        writeUsers();
                        users.clear();
                     }
                }
           }catch(Exception ex) { }
        }
    }
}
