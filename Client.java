/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package network.project1;

import java.util.Calendar;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
//import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Taylor
 */
public class Client extends JPanel
{
    JLabel userL = new JLabel("User Name");
    JLabel hostL = new JLabel("Hostname");
    JLabel portL = new JLabel("Port Number");
    JTextField user = new JTextField(5);
    JTextField host = new JTextField(5);
    JTextField port = new JTextField(5);
    JTextField cmdLine = new JTextField();
    JScrollPane scroll;
    JTextArea display = new JTextArea();
    
    JButton login = new JButton("Login");
    JButton enterKey = new JButton(">>");
    String userName;
    String hostName;
    String portNum;
    String cmd;
    
    //The nitty-gritty:
    String empty = "";
    String inMessage;
    
    int loggedIn = 0;
    
    Socket socketRequested;
    ObjectInputStream in;
    ObjectOutputStream out;
 	
    
    
    public Client()
    {
        this.scroll = new JScrollPane(display);
        setLayout(new MigLayout("","[grow]15",""));
        guiFunc();
        login.addActionListener(new LoginListener());
        display.setEditable(false);
        enterKey.addActionListener(new EnterListener());
        user.setToolTipText("Type your Name");
        host.setToolTipText("Type in Server Address");
        port.setToolTipText("Type in the port #");
        cmdLine.setToolTipText("Type your Command Arguments");
        enterKey.setToolTipText("Enter Key");
        
        //Note that the cTag String isn'y used here. That's due to the \n in the String.
        display.append("Client> Please login and provide a username and valid "
                + "host name and port number.");
        //set user, host, and port to blank (so they aren't null).
        userName = "";
        hostName = "";
        portNum = "";
        
        user.setText(userName);
        host.setText(hostName);
        port.setText(portNum);
        
//        scroll.addMouseWheelListener(new MouseWhListener());
        
    }//end Client Contructor
    
    private void guiFunc()
    {
        add(userL,"span 1, growx");
        add(hostL,"span 1, growx");
        add(portL,"span 2, wrap");
        add(user,"span 1, growx");
        add(host,"span 1, growx");
        add(port,"span 1, growx");
        add(login,"span 1, w 80!, wrap");
        add(scroll,"span 4, growx, growy, push, wrap");
        add(cmdLine, "span 3, growx, growy 50");
        add(enterKey, "span 1, w 80!, wrap");
    }

//    private static class MouseWhListener implements MouseWheelListener {
//
//        public void actionPerformed(ActionEvent event)
//        {
//            scroll.
//        }
//    }

        /**
         * This method appends a message to the main display of the 
         * Client.
         * 
         * <p><b>KEY:</b></p>
         * <p>type "client" to add Client's built in tag to message.</p>
         * <p>type "user" to add user's current tag to message.</p>
         * 
         * @param tag name of the tag to appear on the scroll pane.
         * @param message the message to appear in the pane.
         */
        private void append(String tag, String message)
        {
            
            String cTag = "\nClient> ";
            String uTag = "\n" + userName + "> ";
            
            if(tag.equalsIgnoreCase("client"))
                display.append(cTag + message);
            else if(tag.equalsIgnoreCase("user") || tag.equalsIgnoreCase("username"))
                display.append(uTag + message);
            
        }//end append() method
        
        public ObjectInputStream getIn()
        {
            return in;
        }//end getIn() method
        
        public ObjectOutputStream getOut()
        {
            return out;
        }//end getIn() method
    
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
    
    private class EnterListener implements ActionListener 
    {
        @Override
        public void actionPerformed(ActionEvent event)
        {
            
            cmd = cmdLine.getText();
            display.setLineWrap(false);
            display.setWrapStyleWord(false);
            //display.append(cmd + " " + userName + " " + hostName + " " + portNum + "\n");
            
            if(cmd.equalsIgnoreCase("--close") || cmd.equalsIgnoreCase("close"))
                System.exit(1);
            if(cmd.equalsIgnoreCase("--exit") || cmd.equalsIgnoreCase("exit"))
            {
                messageOut(cmd);
            }//loggedIn = 0;
            else
                messageOut(cmd);
                
            cmdLine.setText("");
            
        }//end actionPerformed
        
        private void messageOut(String message)
        {
            
            try 
            {
                if(out != null)
                {
                    //write message out
                    out.writeObject(message);
                    //flush output stream
                    out.flush();
                }//end if
                //append message
                System.out.println(userName + "> " + message);
                append("user", message);
                
            }//end try
            
            catch (IOException ex)
            {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }//end catch
            
        }//end messageOut() method
        
    }//end subclass

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
    
    private class LoginListener implements ActionListener 
    {

        @Override
        public void actionPerformed(ActionEvent event)
        {
            
            System.out.println("User pressed \"Login\" button.");
            
            //If the user isn't currently logged in
            if(loggedIn == 0)
            {
                //parse out the user entered data
                parseUserLoginData();
                
                //if both host name and port number are empty:
                if(hostName.matches(empty.trim()) && portNum.matches(empty.trim()))
                append("client", "Host Name and Port Number are missing. "
                        + "Please validate and try again.");
                //if only host name is empty:
                else if(hostName.matches(empty.trim()))
                    append("client", "Host Name is missing. "
                        + "Please validate and try again.");
                //if only port number is empty:
                else if(portNum.matches(empty.trim()))
                    append("client", "Port Number is missing. "
                        + "Please validate and try again.");
                //if both have data entered into them:
                else
                {
                    
                    //append("client", "Please wait. Connecting...");
                    System.out.println("Connecting...");
                    //try to log in. If log in returns as true:
                    if(login() == true)
                    {
                        //set variable loggedIn as 1 to exit if.
                        System.out.println("User logged in sucessfully.");
                        
                        //create thread to let client listen to host.
                        Runnable run = new Runnable() 
                        {
                             public void run() 
                             {
                                 serverListener();
                             }//end run() method
                        };
                        //new Thread(r).start();
                        ExecutorService executor = Executors.newCachedThreadPool();
                        executor.submit(run);
                        
                        loggedIn++;
                    }
                    else
                    {
                        //Not logged in.
                    }//end else
                    
                }//end else
                
            }//end if
            //If the user is already successfully loged in and attempts to relogin:
            else if(loggedIn==1)
            {
                append("client", userName + ", you're already logged in.");
                System.err.println(userName + " attempted to login even though "
                        + "they already are.");
            }//end else
            
            //System print loggedIn variable.
            System.out.println("loggedIn: " + loggedIn);
            
        }//end actionPerformed
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * This method receives input from Server.
         */
        private void serverListener()
        {
            try 
            {
                inMessage = in.readObject().toString();
            }//end try
            catch (IOException ex) 
            {
                System.err.println("I/O error in serverListener() method.");
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }//end catch
            catch (ClassNotFoundException ex) 
            {
                System.err.println("ClassNotFoundException error in "
                        + "serverListener() method.");
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }//end catch
            
        }//end run() method
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * Gets user inputted data for user name, host name and port 
         * number.
         */
        private void parseUserLoginData()
        {
            
            //get user name, host name, and port number.
            userName = user.getText();
            hostName = host.getText().trim();
            portNum = port.getText().trim();
            
            //If the user din't enter a username, set as Anon:
            if(userName.matches(empty))//(empty.trim())
                makeAnonUserName();
            
        }//end parseData() method
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * Creates the temp user name for an anon user.
         */
        private void makeAnonUserName()
        {
            
            //get current time and day
            Calendar calen = Calendar.getInstance();
            calen.getTime();
            //Format by Day, Hour, min, sec
            SimpleDateFormat format1 = new SimpleDateFormat("DDHHmmss");
            
            //set anon using this info.
            userName = "Anonymous" + format1.format(calen.getTime());
            
            System.out.println("Login info provided:\n\tUser Name: " + userName 
                    + " \n\tHost Name: " + hostName + "\n\tPort Number: " 
                    + portNum);
        
        }//end makeAnonUserName() method
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * This method connects to server using the entered login 
         * criteria.
         */
        private boolean login()
        {
            
            //set user to not logged in as default
            boolean loggedIn = false;
            
            //NOTE: the actual port num is checked before this method, so its valid to set the number here to 0.
            int portNumber = 0;
            
            try
            {
            //get port number integer
            portNumber = Integer.parseInt(portNum);
            }
            catch(NumberFormatException e)
            {
                append("client", "Notice: Only type in numbers for a port number.");
            }
           
            try 
            {
                //Create socket.
                //NOTE: socketRequested = example's requestedSocket.
                socketRequested = new Socket(hostName, portNumber);
                
                //get output stream
                out = new ObjectOutputStream(socketRequested.getOutputStream());
                //flush output stream to refresh data flow.
                out.flush();
                //get input stream
                in = new ObjectInputStream(socketRequested.getInputStream());
                
                //Connection now is set.
                append("client", "Connection Successful. " + userName 
                        + " has logged in.");
                
                //all went well, set as logged in.
                loggedIn = true;
                
            }//end try
            
            catch (UnknownHostException ex) 
            {
                System.err.println("The user tried to enter an unknown host. "
                        + "This threw an UnknownHostException. See the following "
                        + "stack trace if needed: \n");
                append("client", "Unknown Host. Please review your login data and"
                        + " try again.");
                Logger.getLogger(Client.class.getName()).log(Level.WARNING, null, ex);
            }//end catch
            
            
            
            catch (IOException ex) 
            {
                System.err.println("It seems an IO exception occoured.");
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                append("client", "Connection Refused. Please review your port "
                        + "number and host name.");
            }//end catch
            
            System.out.println("In method login(), loggedIn = " + loggedIn + ".");
            
            return loggedIn;
            
        }//end login() method
        
    }//end subclass
    
    
}//end Client
