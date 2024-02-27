/* Server program for the OnlineOrder app

   @author Alden Sprackling, Alex Ceithamer, Danial Moczynski

   @version CS 391 - Spring 2024 - A2
*/

import java.net.*;
import java.io.*;

public class OOServer
{
    static ServerSocket serverSocket = null;  // listening socket
    static int portNumber = 55555;            // port on which server listens
    static Socket clientSocket = null;        // socket to a client

    /* Start the server then repeatedly wait for a connection request, accept,
       and start a new thread to handle one online order
    */
    public static void main(String[] args)
    {
      try {
         serverSocket = new ServerSocket(portNumber);
         System.out.println("Server started: " + serverSocket);

         while (true)
         {
            clientSocket = serverSocket.accept();
            System.out.println("New connection established: " + 
                               clientSocket);
            (new Thread( new OO(clientSocket))).start();
         }
      } catch (IOException e)
      {
            System.out.println("Server encountered an error. Shutting down...");
      }
    }// main method

}// OOServer class

class OO implements Runnable
{
    static final int MAIN = 0;          // M state
    static final int PIZZA_SLICE = 1;   // PS state
    static final int HOT_SUB = 2;       // HS state
    static final int DISPLAY_ORDER = 3; // DO state
    static final Menu mm =              // Main Menu
        new Menu( new String[] { "Main Menu:", "Pizza Slices", "Hot Subs",
        "Display order" } );
    static final Menu psm =             // Pizza Slice menu
        new Menu( new String[] { "Choose a Pizza Slice:", "Cheese", "Pepperoni",
        "Sausage", "Back to Main Menu", "Display Order" } );
    static final Menu hsm =             // Hot Sub menu
        new Menu( new String[] { "Choose a Hot Sub:", "Italian", "Meatballs",
        "Back to Main Menu", "Display Order"  } );
    static final Menu dom =             // Display Order menu
        new Menu( new String[] { "What next?", "Proceed to check out",
        "Go Back to Main Menu"  } );
    int state;                          // current state
    Order order;                        // current order
    Socket clientSocket = null;         // socket to a client
    DataInputStream in = null;          // input stream from client
    DataOutputStream out = null;        // output stream to client

    /* Init client socket, current state, and order, and open the necessary
       streams
     */
    OO(Socket clientSocket)
    {
       this.clientSocket = clientSocket;
       state = MAIN;
       order = new Order();
       try
       {               
          openStreams(clientSocket);
       } catch (IOException e)
       {
          System.out.println(e.getMessage());     
       } 
    }// OO constuctor

    /* each execution of this thread corresponds to one online ordering session
     */
    public void run()
    {
       try
       {
          placeOrder();         
       } catch (EOFException e)
       {
          System.out.println("          Client died unexpectedly"); 
          System.out.println("          " + clientSocket); 
       } catch (IOException e)
       {
           System.out.println(e);
       }
    }// run method

    /* implement the OO protocol as described by the FSM in the handout
       Note that, before reading the first query (i.e., option), the server
       must display the welcome message shown in the trace in the handout,
       followed by the main menu.
     */
    void placeOrder() throws IOException
    {   
      String request, reply, formatLine;
      formatLine = "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n";
      reply = formatLine
            + "     Welcome to Hot Subs & Wedges!     \n"
            + formatLine
            + "\n"
            + mm.toString();
      out.writeUTF(reply);

      while (true)
      {
            request = in.readUTF();

            switch (state) 
            {
            case MAIN:

               switch (request)
               {
               case "1":
                  state = PIZZA_SLICE;
                  reply = formatLine 
                        + psm.toString();
                  break;
               case "2":
                  state = HOT_SUB;
                  reply = formatLine 
                        + hsm.toString();
                  break;
               case "3":
                  state = DISPLAY_ORDER;
                  reply = formatLine 
                        + order.toString()
                        + "\n"
                        + formatLine 
                        + dom.toString();
                  break;
               default:
                  reply = "Invalid option!\n"
                        + mm.toString();
                  break;
               }  
               break;

            case PIZZA_SLICE:
               
               switch (request) 
               {
               case "1":
               case "2":
               case "3":
                  order.addItem(psm.getOption(Integer.parseInt(request)));
                  break;
               case "4":
                  state = MAIN;
                  reply = formatLine 
                        + mm.toString();
                  break;
               case "5": 
                  state = DISPLAY_ORDER;
                  reply = formatLine 
                        + order.toString() 
                        + "\n"
                        + formatLine 
                        + dom.toString();
                  break;
               default:
                  reply = "Invalid option!\n"
                        + psm.toString();
                  break;
               }
               break;

            case HOT_SUB:
               
               switch (request) 
               {
               case "1":
               case "2":
                  order.addItem(hsm.getOption(Integer.parseInt(request)));
                  break;
               case "3":
                  state = MAIN;
                  reply = formatLine 
                        + mm.toString();
                  break;
               case "4":
                  state = DISPLAY_ORDER;
                  reply = formatLine 
                        + order.toString() 
                        + "\n"
                        + formatLine 
                        + dom.toString();
                  break;
               }
               break;

            case DISPLAY_ORDER: 

               switch (request) 
               {
               case "1":
                  reply = "Thank you for your visit!";
                  break;
               case "2":
                  state = MAIN;
                  reply = formatLine 
                        + mm.toString();
                  break;
               }
               break;
            }  

            out.writeUTF(reply);
            if (reply.equals("Thank you for your visit!")) { break; }
      }
      System.out.println("One more order processed");
      close();
    }// placeOrder method

   /* open the necessary I/O streams and initialize the in and out
      static variables; this method does not catch any exceptions
    */
    void openStreams(Socket socket) throws IOException
    {
      in = new DataInputStream(clientSocket.getInputStream());
      out = new DataOutputStream(clientSocket.getOutputStream());
    }// openStreams method

    /* close all open I/O streams and sockets
     */
    void close()
    {
      try
      {
            if (in != null)           { in.close();           } 
            if (out != null)          { out.close();          } 
            if (clientSocket != null) { clientSocket.close(); }            
      } catch (IOException e)
      {
         System.err.println("Error in close(): " + e.getMessage());
      }  
    }// close method

}// OO class
