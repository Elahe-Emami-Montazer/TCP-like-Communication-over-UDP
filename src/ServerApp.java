
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Elahe
 */
public class ServerApp {
  
  private static final int PORT = 1212; 

  public static void main(String args[]) throws Exception {
    
    ReliableDatagramSocket serverSocket = new ReliableDatagramSocket(PORT);
    
    System.out.println("server is listening ...");
    System.out.println("");
    
    byte[] receiveData = new byte[1024];
       
    while(true) {
      
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      
      serverSocket.rdt_rcv(receivePacket);
      
      //System.out.println("server recieved a packet on port: " + receivePacket.getPort());
      
      String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
      InetAddress IPAddress = receivePacket.getAddress();
      int port = receivePacket.getPort();
      
      System.out.println("server recieved: " + sentence + " on port: " + port);
      
      String modifiedSentence = '*' + sentence.toUpperCase() + '*';
      
      DatagramPacket sendPacket = new DatagramPacket(modifiedSentence.getBytes(), modifiedSentence.length(), IPAddress, port);
      
      serverSocket.rdt_send(sendPacket);  
      
      System.out.println("server sent: " + modifiedSentence);
      System.out.println("------------------------------------------------------------------------------------------------------");
    
      //serverSocket.close();
      
    }
  
  }
  
}
