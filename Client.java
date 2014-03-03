package network.project1;

import java.util.Calendar;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.CharBuffer;
import java.text.SimpleDateFormat;
//import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

/**
 * The Class defining the GUI design and Client Threading.
 * 
 * @author Taylor   (GUI development)
 * @author Jeremiah (major coding, GUI development)
 */
public class Client extends JPanel
{
    
    //Instance variables of labels and text fields
    JLabel userL = new JLabel("User Name");
    JLabel hostL = new JLabel("Host Name");
    JLabel portL = new JLabel("Port Number");
    JTextField user = new JTextField(5);
    JTextField host = new JTextField(5);
    JTextField port = new JTextField(5);
    JTextField cmdLine = new JTextField();
    //create instance variable of scrollable pane and display.
    JScrollPane scroll;
    JTextArea display = new JTextArea();
    
    //create instance variable of buttons
    JButton login = new JButton("Login");
    JButton enterKey = new JButton(">>");
    
    //instance variables for parsing user login data later...
    String userName;
    String hostName;
    String portNum;
    //instance variable for client command.
    String cmd;
    
    
    
    //The nitty-gritty:
    
    String empty = "";
    //Instace variable for recieving message from Server.
    String inMessage;
    //If loggedIn is 0, user isn't logged in. loggedIn = 1 for logged in.
    int loggedIn = 0;
    //the socket for the connection to server.
    Socket socketRequested;
    
    //In receives data, out prints out data to Server.
    BufferedReader in;
    PrintWriter out;
 	
    
    /**
     * Constructor for the Client class.& Used to implement the GUI.
     */
    public Client()
    {
        
        //set display to scrollable plane. Set Line wrap/word wrap style
        display.setEditable(false);
        display.setLineWrap(false);
        display.setWrapStyleWord(false);
        this.scroll = new JScrollPane(display);
        
        //set layout for class
        setLayout(new MigLayout("","[grow]15",""));
        //create GUI
        guiFunc();
        
        //add listeners
        login.addActionListener(new LoginListener());
        login.addKeyListener(new LoginListener());
        enterKey.addActionListener(new EnterListener());
        enterKey.addKeyListener(new EnterListener());
        cmdLine.addKeyListener(new EnterListener());
        user.addKeyListener(new LoginListener());
        host.addKeyListener(new LoginListener());
        port.addKeyListener(new LoginListener());
        
        
        
        //set tool tips where needed.
        user.setToolTipText("Type your Name");
        host.setToolTipText("Type in Server Address");
        port.setToolTipText("Type in the port #");
        cmdLine.setToolTipText("Type your Command Arguments");
        enterKey.setToolTipText("Enter Message or Command");
        
        //Opening message. Note append() isn't used here.
        display.append("Client> Please login and provide a username, valid "
                + "host name and port number.");
        
        //display first instance of menu.
        menu();
        
        //set user, host, and port to blank (so they aren't null).
        userName = "";
        hostName = "";
        portNum = "";
        user.setText(userName);
        host.setText(hostName);
        port.setText(portNum);
        
    }//end Client Contructor
    
//////////////////////////////////////////////////////////////////////////////
    
    /**
     * This method incorporates the components in the project to the main
     * JPanel.
     * 
     * @param none
     * @return none
     */
    private void guiFunc()
    {
        
        //add components to display
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
        
    }//end guifunc() method

//////////////////////////////////////////////////////////////////////////////
    
    /**
     * This method appends a message to the main display of the 
     * Client.
     * 
     * <p><b>KEY:</b></p>
     * <p>type "client" to add Client's built in tag to message.</p>
     * <p>type "user" to add user's current tag to message.</p>
     * <p>type "server" to add server tag to message.</p>
     * <p>type "admin" to append admin tag to message.</p>
     * <p>type "" to append nothing to a message.</p>
     * 
     * @param tag name of the tag to appear on the scroll pane.
     * @param message the message to appear in the pane.
     * @return none
     */
    private void append(String tag, String message)
    {
        
        //These are the "tags" used to identify who is sending a message.
        String cTag = "\nClient> ";
        String uTag = "\n" + userName + "> ";
        String sTag = "\nServer> ";
        String aTag = "\nAdmin> ";
        String bTag = "\n ";
        String tTag = "\n\t";
        
        //append tag based on parameter. Append to "display."
        if(tag.equalsIgnoreCase("client"))
            display.append(cTag + message);
        else if(tag.equalsIgnoreCase("user") || tag.equalsIgnoreCase("username"))
            display.append(uTag + message);
        else if(tag.equalsIgnoreCase("server"))
            display.append(sTag + message);
        else if(tag.equalsIgnoreCase("admin"))
            display.append(aTag + message);
        else if(tag.equalsIgnoreCase("tab"))
            display.append(tTag + message);
        else if(tag.equals(empty.trim()))
            display.append(bTag + message);
        

    }//end append() method
        
//////////////////////////////////////////////////////////////////////////////
    
    /**
     * Appends the menu to the display.
     * 
     * @param none
     * @return none
     */
    private void menu()
    {
        
        //set tab constant(and tab with a skip line character)
        final String tab = "\t\t";
        final String nTab = "\n" + "   ";
        //set message constant
        final String message;
        
        message = nTab + "The following list of commands are available for use:"
                + nTab + " -t, --time" + tab + "Host current Date & Time."
                + nTab + "-u, --uptime" + tab + "Host Uptime."
                + nTab + "-m, --memory" + tab + "Host Memory Use."
                + nTab + "-n, --netstat" + tab + "Host Netstat."
                + nTab + "-p, --processes" + "\t" + "Host Running Processes."
                + nTab + "-s, --simulation" + "\t" + "Multi Client Simulation."
                + nTab + "-c c, --connection close" + "\t" + "Log out from server."
                + nTab + "-c, --close" + tab + "Close Client GUI."
                + "\n";
        
        System.out.println(message);
        
        //append menu to display
        append("", message);
        
    }//end menu()
    
    //////////////////////////////////////////////////////////////////////////////
    
    /**
     * Appends the menu to the display.
     * 
     * @param none
     * @return none
     */
    private void simulationMenu()
    {
        
        //set tab constant(and tab with a skip line character)
        final String tab = "\t\t";
        final String nTab = "\n" + "   ";
        //set message constant
        final String message;
        
        message = nTab + "The following list of commands are available for use:"
                + nTab + " -t, --time" + tab + "Host current Date & Time."
                + nTab + "-u, --uptime" + tab + "Host Uptime."
                + nTab + "-m, --memory" + tab + "Host Memory Use."
                + nTab + "-n, --netstat" + tab + "Host Netstat."
                + nTab + "-p, --processes" + "\t" + "Host Running Processes."
                + nTab + "-c c, --connection close" + "\t" + "Log out from server."
                + nTab + "-c, --close" + tab + "Close Client GUI."
                + "\n";
        
        System.out.println(message);
        
        //append menu to display
        append("", message);
        
    }//end simulationMenu()
    
    
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
    
    /**
     * This subclass defines what the ">>" button does
     */
    private class EnterListener implements ActionListener, KeyListener 
    {
        
        /**
         * Defines the action taken.
         * 
         * @param event unused
         */
        @Override
        public void actionPerformed(ActionEvent event)
        {
            
            enterAction();
            
        }//end actionPerformed
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * Defines the action taken when the "Enter" key is pressed.
         * 
         * @param e 
         */
        @Override
        public void keyPressed(KeyEvent e) 
        {
            if (e.getKeyCode()==KeyEvent.VK_ENTER)
            {
                //System.out.println("Hello");
                enterAction();
            }
        }//end keyPressed() method
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * Unused but required.
         * 
         * @param arg0 
         */
        @Override
        public void keyReleased(KeyEvent arg0) 
        {
            
            //Unused but required field.

        }//end keyReleased() method
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * Unused but required.
         * 
         * @param arg0 
         */
        @Override
        public void keyTyped(KeyEvent arg0) 
        {
            //Unused but required field.
        }//end keyTyped() method
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * The main action performed for all overrided methods.
         */
        private void enterAction()
        {
            
            boolean justLoggedOut = false;
            //get user command
            cmd = cmdLine.getText();
            
            //Now, based on user command, something happens.
            
            if(cmd.equalsIgnoreCase("--close") || cmd.equalsIgnoreCase("-c"))
                System.exit(1);
            //log out algorithm:
            else if((cmd.equalsIgnoreCase("--connection close") 
                    || cmd.equalsIgnoreCase("-c c"))
                    && loggedIn == 1)
            {
                messageOut(cmd);
                loggedIn = 0;
                append("client", "You have successfully logged out from the "
                        + "server.");
                justLoggedOut = true;
            }//end else if
            else if(cmd.equalsIgnoreCase("--simulation help") 
                    || cmd.equalsIgnoreCase("--simulation -help")
                    || cmd.equalsIgnoreCase("-s h")
                    || cmd.equalsIgnoreCase("-s -h"))
            {
                
                append("client", "This is the format for \"--simulation:\""
                        + "\n--simulation  HOSTNAME  PORT#  #CLIENTS"
                        + "\n or"
                        + "\n-s  HOSTNAME  PORT#  #CLIENTS");
                
            }//end else if
            else if((cmd.startsWith("--simulation"))
                    || (cmd.startsWith("-s")))
            {
                
                append("client", "Simulation Mode activated.");
                
                simulation();
                
            }//end else if
            
            //if logged in
            if(loggedIn != 0)
            {
                messageOut(cmd);
                menu();
            }//end if
            else if(loggedIn == 0 && !(justLoggedOut = true))
            {
                append("user", cmd);
                menu();
            }//end else if
            else if(loggedIn == 0 && (justLoggedOut = true))
            {
                menu();
                justLoggedOut=false;
            }//end else if
            
            //set the cmdLine to blank, to remove old command.
            cmdLine.setText("");
            
        }//end EnterAction() method
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * 
         */
        private void simulation()
        {
            
            try
            {
                String host = null;
                int portN = 0;
                int numClients;
                
                //set up test array
                
                //set up tokenizer
                String delims = "[ ]+"; //delims is used for tokens. Leave as "[ ]+".
                String[] tokens;
                tokens = cmd.split(delims);

                //If one param is null but not all three...
//                if(tokens[2] == null || tokens[3] == null && !(tokens[1] == null && tokens[2] == null && tokens[3] == null))
                    
                try
                {
                //if both params were entered...
                if(tokens[1] != null && tokens[2] != null && tokens[3] != null)
                {

                    host = tokens[1];
                    portN = Integer.parseInt(tokens[2]);
                    numClients = Integer.parseInt(tokens[3]);

                    //if logged in already, log 'em out.
                    if(loggedIn == 1)
                    {

                        append("client","You are being logged out so the simulation"
                                + " can log you in."
                                + "\nNote that the simulation can be controlled like"
                                + " a normal Client.");
                        loggedIn=0;
                        try 
                        {
                            Thread.sleep(100);
                        } //end try
                        catch (InterruptedException ex) 
                        {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }//end catch

                    }//end inner if

                    append("client","Creating simulated client threads...");

                    //creat final variables for runnable
                    final String finalHost = host;
                    final int finalPortN = portN;



                    for(int index = 0; index <= numClients-1; index++)
                    {
                        //make runnable object
                        Runnable run = new Runnable() 
                        {

                            public void run() 
                            {

                                while (true) 
                                {

                                        final ClientThreadSimulation se = new ClientThreadSimulation(finalHost, finalPortN);
                                        se.run();

                                }//end while loop

                            }//end run() method
                        };
                        new Thread(run).start();


                    }//end while

                }//end if
                
                append("client","You are now successfully in Simulation Mode.");
                }//end try
                catch(ArrayIndexOutOfBoundsException e)
                {
                    
                    append("Client", "An error occoured. The format for Client Simulation is:\n"
                            + "\t" + tokens[0] + "  HOSTNAME  PORT#  #CLIENTS\n"
                            + "Please review your simulation login data and try again.");
                    
                    e.printStackTrace();
                    
                }//end catch
                
            }//end try
            catch(NumberFormatException e)
            {
                append("client", "Notice: Only type in numbers for a port number"
                        + ".\nSimulation aborted.");
            }
            
        }//end simulation() method
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * This method sends messages out to the server and appends 
         * them to the display.
         * 
         * @param message the command written  by the user
         * @return none
         */
        private void messageOut(String message)
        {
            
            if(out != null)
            {
                //write message out
                out.println(message);
                //flush output stream
                out.flush();
            }//end if
            
            //Debug message:
            System.out.println(userName + "> " + message);
            //append message
            append("user", message);
            
        }//end messageOut() method
        
    }//end subclass

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
    
    /**
     * Subclass defines the login operation of Client.
     */
    private class LoginListener implements ActionListener, KeyListener
    {
        
        //boolean clientThreadConnected = false;
        
        /**
         * Defines default login action.
         * 
         * @param event 
         * @return none
         */
        @Override
        public void actionPerformed(ActionEvent event)
        {
            
            System.out.println("User pressed \"Login\" button.");
            
            loginAction();
            
        }//end actionPerformed
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * Defines the default Enter key press action.
         * 
         * @param e 
         * @return none
         */
        @Override
        public void keyPressed(KeyEvent e) 
        {
            if (e.getKeyCode()==KeyEvent.VK_ENTER)
            {
                loginAction();
            }//end if
        }//end keyPressed() method
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * Unused method but required.
         * 
         * @param e 
         * @return none
         */
        @Override
        public void keyReleased(KeyEvent e) 
        {
            
            //Unused but required field.

        }//end keyRelease() overrided method
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * Unused method but required.
         * 
         * @param e 
         * @return none
         */
        @Override
        public void keyTyped(KeyEvent e) 
        {
            //Unused but required field.
        }//end keyTyped() method
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * The main action of the login command.
         * 
         * @param none
         * @return none
         */
        private void loginAction()
        {
            
            //If the user isn't currently logged in
            if(loggedIn == 0)
            {
                //parse out the user entered data
                parseUserLoginData();
                
                //if both host name and port number are empty:
                if(hostName.matches(empty.trim()) && portNum.matches(empty.trim()))
                {
                    append("client", "Host Name and Port Number are missing. "
                            + "Please validate and try again.");
                    
                    //set fields to empty in case someone put "        " in it.
                    host.setText("");
                    port.setText("");
                
                }//end if
                
                //if only host name is empty:
                else if(hostName.matches(empty.trim()))
                {
                    //append message saying so.
                    append("client", "Host Name is missing. "
                        + "Please validate and try again.");
                    
                    //set host field to empty in case someone put spaces in it.
                    host.setText("");
                    
                }//end else if
                
                //if only port number is empty:
                else if(portNum.matches(empty.trim()))
                {
                    //append message saying so.
                    append("client", "Port Number is missing. "
                        + "Please validate and try again.");
                    
                    //set port field to empty in case someone put spaces in it.
                    port.setText("");
                }//end else if
                
                //if both have data entered into them:
                else
                {
                    
                    //Debug message:
                    System.out.println("Attempting Connecting...");
                    
                    //try to log in. 
                    login();
                    
                    //If loggedIn returns as 1 (true):
                    if(loggedIn==1)
                    {
                        //set variable loggedIn as 1 to exit if.
                        System.out.println("User logged in sucessfully.");
                        
                    }//end if
                    else
                    {
                        //Not logged in.
                    }//end inner else
                    
                }//end else
                
            }//end if
            
            //If the user is already successfully logged in and attempts to relogin:
            else if(loggedIn==1)
            {
                append("client", userName + ", you're already logged in.");
                System.err.println(userName + " attempted to login even though "
                        + "they already are.");
            }//end else
            
            //System print loggedIn variable.
            System.out.println("loggedIn: " + loggedIn);
            
        }//end loginAction()
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * Gets user inputted data for user name, host name and port 
         * number.
         * 
         * @param none
         * @return none
         */
        private void parseUserLoginData()
        {
            
            //get user name, host name, and port number.
            userName = user.getText();
            hostName = host.getText().trim();
            portNum = port.getText().trim();
            
            //If the user din't enter a username, set as Anon:
            if(userName.matches(empty.trim()))//(empty.trim())
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
            
            //Debug message: user name, host name, port number.
            System.out.println("Login info provided:\n\tUser Name: " + userName 
                    + " \n\tHost Name: " + hostName + "\n\tPort Number: " 
                    + portNum);
        
        }//end makeAnonUserName() method
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * This method connects to server using the entered login 
         * criteria.
         * 
         * @param none
         * @return variable stating if the user logged in successfully.
         */
        private void login()
        {
            
            
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
            
            //port number needs to be from a variable marked final. Thus:
            final int finalPortNum = portNumber;
            
            //make runnable instance of Client thread.
            Runnable run = new Runnable() 
                {
                     public void run() 
                     {
                         clientThread(finalPortNum);
                         
                     }//end run() method
                };
//            new Thread(r).start();
            
            //execute the runnable
            ExecutorService executor = Executors.newCachedThreadPool();
            executor.submit(run);  
                
            System.out.println("In method login(), loggedIn = " + loggedIn + ".");
            
            
        }//end login() method
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * The technical "run" method for Client.java.
         * 
         * Note that this is run from a "Runnable" variable. Thus, everything
         * in this method acts as though it were is a "while(true)" loop,
         * and runs in the background of the program.
         * 
         * @param portNumber the port number selected by the client/user.
         * @return none
         */
        private synchronized void clientThread(int portNumber)
        {
            try
            {
                
                //try to connect
                socketRequested = new Socket(hostName, portNumber);
                
                //Connection now is set.
                append("client", "Connection Successful. " + userName 
                        + " has logged in.");
                
                //set up in and out connetion
                in = new BufferedReader(new InputStreamReader(socketRequested.getInputStream()));
                out = new PrintWriter(socketRequested.getOutputStream());
                
                //flush output stream to refresh data flow.
                out.flush();
                
                //at this point, the user is logged in. So:
                loggedIn = 1;
                
                do
                {
                    try 
                    {                
                        //read in message from server
                        inMessage = in.readLine();

                        System.out.println("inMessage: " + inMessage);
                        
//                        if(in != null)
//                            System.out.println("\tin: ain't null.");
                        
                        
                        append("server", inMessage);
                        
                        
                        //If client exited disconnected from server
                        if(inMessage.equalsIgnoreCase("--connection close"))//--connection close
                            //set logged in as zero. (Not logged in.)
                            loggedIn = 0; 
                        
                        
                    }//end try
                    catch(SocketException se)
                    {
                        System.err.println("The server closed prematurely.");
                        
                        loggedIn=0;
                        
                        append("client", "The connection to the server was lost."
                                + " Thus, you have been logged out.");
                        
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, se);
                    
                    }//end SocketException catch
                    catch (IOException ex) 
                    {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }//end catch

                }//end do
                while(loggedIn==1);
                
            }//end try
            
            catch(ConnectException ce)
            {
                System.err.println("The user tried to enter an unknown port #. "
                        + "This threw a ConnectionException. See the following "
                        + "stack trace if needed: \n");
                append("client", "Unknown Port Number. Please review your login data and"
                        + " try again.");
                Logger.getLogger(Client.class.getName()).log(Level.WARNING, null, ce);
            }//end ConnectException catch 
            
            catch(IOException ex)
            {
                System.err.println("I/O error. The user tried to enter an unknown host. "
                        + "This threw an UnknownHostException. See the following "
                        + "stack trace if needed: \n");
                append("client", "Unknown Host. Please review your login data and"
                        + " try again.");
                Logger.getLogger(Client.class.getName()).log(Level.WARNING, null, ex);
            
            } //end IOExeption catch
            finally
            {
                try 
                {
                    //close connections
                    in.close();
                    out.close();
                    socketRequested.close();
                }//end try
                catch (IOException ex) 
                {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }//end catch
                
            }//end finally
            
        }//end clientThread();
        
    }//end subclass
    
    
    /**
     *
     * @author Jeremiah Doody
     */
    public class ClientThreadSimulation 
    {
        //create instance variables
        BufferedReader in;
        PrintWriter out;

        String hostName;
        int portNumber;

        String inMessage;
        Socket socketRequested;

        int loggedIn = 0;


        /**
         * 
         * 
         * @param in
         * @param out
         * @param hostName
         * @param portNumber 
         */
        public ClientThreadSimulation(String hostName, int portNumber)
        {
            //set instane variables to parameter variables
            this.hostName = hostName;
            this.portNumber = portNumber;

        }//end ClientThreadSimulation() constructor method

//////////////////////////////////////////////////////////////////////////////
        
        public synchronized void run()
        {

            try
            {
                
                //try to connect
                socketRequested = new Socket(hostName, portNumber);
                
                //Connection now is set.
                append("client", "Connection Successful. " + userName 
                        + " has logged in.");
                
                //set up in and out connetion
                in = new BufferedReader(new InputStreamReader(socketRequested.getInputStream()));
                out = new PrintWriter(socketRequested.getOutputStream());
                
                //flush output stream to refresh data flow.
                out.flush();
                
                //at this point, the user is logged in. So:
                loggedIn = 1;
                
                do
                {
                    try 
                    {                
                        //read in message from server
                        inMessage = in.readLine();

                        System.out.println("inMessage: " + inMessage);
                        
//                        if(in != null)
//                            System.out.println("\tin: ain't null.");
                        
                        
                        append("server", inMessage);
                        
                        
                        //If client exited disconnected from server
                        if(inMessage.equalsIgnoreCase("--connection close"))//--connection close
                            //set logged in as zero. (Not logged in.)
                            loggedIn = 0; 
                        
                        
                    }//end try
                    catch(SocketException se)
                    {
                        System.err.println("The server closed prematurely.");
                        
                        loggedIn=0;
                        
                        append("client", "The connection to the server was lost."
                                + " Thus, you have been logged out.");
                        
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, se);
                    
                    }//end SocketException catch
                    catch (IOException ex) 
                    {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }//end catch

                }//end do
                while(loggedIn==1);
                
            }//end try
            
            catch(ConnectException ce)
            {
                System.err.println("The user tried to enter an unknown port #. "
                        + "This threw a ConnectionException. See the following "
                        + "stack trace if needed: \n");
                append("client", "Unknown Port Number. Please review your login data and"
                        + " try again.");
                Logger.getLogger(Client.class.getName()).log(Level.WARNING, null, ce);
            }//end ConnectException catch 
            
            catch(IOException ex)
            {
                System.err.println("I/O error. The user tried to enter an unknown host. "
                        + "This threw an UnknownHostException. See the following "
                        + "stack trace if needed: \n");
                append("client", "Unknown Host. Please review your login data and"
                        + " try again.");
                Logger.getLogger(Client.class.getName()).log(Level.WARNING, null, ex);
            
            } //end IOExeption catch
            finally
            {
                try 
                {
                    //close connections
                    in.close();
                    out.close();
                    socketRequested.close();
                }//end try
                catch (IOException ex) 
                {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }//end catch
                
            }//end finally

        }//end run method
        
//////////////////////////////////////////////////////////////////////////////
        
        /**
         * This method sends messages out to the server and appends 
         * them to the display.
         * 
         * @param message the command written  by the user
         * @return none
         */
        private void messageOut(String message)
        {
            
            if(out != null)
            {
                //write message out
                out.println(message);
                //flush output stream
                out.flush();
            }//end if
            
            //Debug message:
            System.out.println(userName + "> " + message);
            //append message
            append("user", message);
            
        }//end messageOut() method


    }//end ClientThreadSimulation class

    
}//end Client
