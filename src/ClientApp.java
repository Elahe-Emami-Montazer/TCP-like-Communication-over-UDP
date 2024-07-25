
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Elahe
 */
public class ClientApp {
  
  private static final int PORT = 1212;

  public static void main(String args[]) throws Exception{
    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
    ReliableDatagramSocket clientSocket = new ReliableDatagramSocket();
    InetAddress IPAddress = InetAddress.getLocalHost();

    byte[] receivedData = new byte[1024];
    
    while (true) {
      

      System.out.println("enter your sentence: ");
      String sentence = inFromUser.readLine();
      System.out.println("");

      DatagramPacket sendPacket = new DatagramPacket(sentence.getBytes(), sentence.length(), IPAddress, PORT);
      
      clientSocket.rdt_send(sendPacket);
      

      System.out.println("client sent: " + sentence + " on port: " + sendPacket.getPort());

      DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);

      clientSocket.rdt_rcv(receivePacket);

      String fromServer = new String(receivePacket.getData(), 0, receivePacket.getLength());

      System.out.println("From Server: " + fromServer);
      System.out.println("------------------------------------------------------------------------------------------------------");
      
      //clientSocket.close();
    }
  }
  
}
