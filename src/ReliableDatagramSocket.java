
import java.io.IOException;
import java.net.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Elahe
 */


public class ReliableDatagramSocket {
  
  DatagramSocket socket;
  boolean send_state = false;
  boolean rcv_state = false;

  public ReliableDatagramSocket(int port) throws SocketException {
    //server
    socket = new DatagramSocket(port);
    
  }

  public ReliableDatagramSocket() throws SocketException {
    //client
    socket = new DatagramSocket();
    
  }
  
  public void rdt_send(DatagramPacket packet) throws IOException {
    //System.out.println("send_state: " + send_state);
    
    InetAddress IPAddress = packet.getAddress();
    int port = packet.getPort();
    String data = new String(packet.getData(), 0, packet.getLength()); 
    
    int checksum = calcChecksum(data);
    
    DatagramPacket sndpkt = make_packet(send_state, data, checksum, IPAddress, port);
    DatagramPacket sndpkt2 = make_packet(send_state, data+"h", checksum, IPAddress, port);
    
    //send some packets with problem!
    int rnd = (int) (Math.random()*10);
    //System.out.println("rnd: " + rnd);
    
    
    if (rnd == 5) {
      socket.send(sndpkt2);
      
    } else {
      socket.send(sndpkt);
      
    }
    
    //socket.send(sndpkt);
    
    //System.out.println("rdtsend: send " + getPktSeq(sndpkt) + " " + getPktData(sndpkt) + " on port: " + sndpkt.getPort());
    
    byte[] rcvpktData = new byte[1024];
    DatagramPacket rcvpkt = new DatagramPacket(rcvpktData, rcvpktData.length);
    
    boolean b = true;
    while(b) {
      
      socket.receive(rcvpkt);
      //System.out.println("rdt_send: rcv " + getPktSeq(rcvpkt) + " " + getPktData(rcvpkt) + " on port: " + rcvpkt.getPort());
      
      if ((isCorrupted(rcvpkt)) || (isAck(rcvpkt, !send_state))) {
        System.err.println("packet has a problem!");
        //System.out.println("send_state: " + send_state);
        
        socket.send(sndpkt);
        //System.out.println("problem send: " + getPktSeq(sndpkt) + getPktData(sndpkt));
        b = true;
        
      } else if ((!isCorrupted(rcvpkt)) && (isAck(rcvpkt, send_state))) {
        //System.out.println("rdtsend true ack");
        b = false;
        
        send_state = !send_state;
        //System.out.println("send_state changed to: " + send_state);
        
      } else {
        System.err.println("rdt_send: error in getting ACK!");
        b = false;
        
      }
      
    }
    
    
  }
  
  
  
  public void rdt_rcv(DatagramPacket rcvpkt) throws IOException {
    //System.out.println("rcv_state: " + rcv_state);
    
    
    boolean b = true;
    while (b) {
      
      socket.receive(rcvpkt);
      //System.out.println("rdt_rcv: rcv " + getPktSeq(rcvpkt) + " " + getPktData(rcvpkt) + " on port: " + rcvpkt.getPort());
    
      InetAddress adress = rcvpkt.getAddress();
      int port = rcvpkt.getPort();
    
      DatagramPacket sndpkt;
      
      if ((isCorrupted(rcvpkt)) || (getPktSeq(rcvpkt) != rcv_state)) {
        System.err.println("packet has a problem!");
        
        sndpkt = make_packet(!rcv_state, "ACK", calcChecksum("ACK"), adress, port);
        socket.send(sndpkt);
        //System.out.println("rdt_rcv: send " + getPktSeq(sndpkt) + " " + getPktData(sndpkt) + " on port: " + sndpkt.getPort());
        
        b = true;
        
      } else if ((!isCorrupted(rcvpkt)) && (getPktSeq(rcvpkt) == rcv_state)) {
        b = false;

        sndpkt = make_packet(rcv_state, "ACK", calcChecksum("ACK"), adress, port);
        socket.send(sndpkt);
        //System.out.println("rdt_rcv: send " + getPktSeq(sndpkt) + " " + getPktData(sndpkt) + " on port: " + sndpkt.getPort());

        rcv_state = !rcv_state;
        //System.out.println("rcv_state changed to: " + rcv_state);
        
      }

    }
    rcvpkt.setData(getPktData(rcvpkt).getBytes());
    //System.out.println("rcvpkt setdata");
        
  }
  
  private boolean isCorrupted(DatagramPacket packet) {
    //if returns true means data is corrupted
    boolean b = true;
    
    String data = getPktData(packet);
    int rcv_checksum = getPktChecksum(packet);
    
    int data_checksum = calcChecksum(data);
    
    if (data_checksum == rcv_checksum) {
      b = false;
      
    }
    
    return b;
    
  }
  
  public void close() {
    socket.close();
  }
  
  private int calcChecksum(String data) {
    int checksum = 0;
    
    for (int i = 0; i < data.length(); i++) {
      checksum += (int) data.charAt(i);
      
      //check if has carry
      boolean carry = false;
      carry = ((checksum > 0xffff) ? (true) : (false));
      
      if (carry) {
        checksum = checksum & 0x0ffff;
        checksum++;
        carry = false;
      }
       
    }
    
    return  checksum;
  }
  
  private String getPktData(DatagramPacket packet) {
    String ret = "";
    
    String pktStr = new String(packet.getData(), 0, packet.getLength());
    
    ret = pktStr.split(";")[1];
    
    return ret;
    
  }
  
  private boolean getPktSeq(DatagramPacket packet) {
    boolean ret = false;
    
    String pktStr = new String(packet.getData(), 0, packet.getLength());
    
    int s = Integer.parseInt(pktStr.split(";")[0]);
    ret = (s == 0) ? (false) : (true);
    
    return ret;
    
  }
  
  private int getPktChecksum(DatagramPacket packet) {
    int ret = -1;
    
    String pktStr = new String(packet.getData(), 0, packet.getLength());
    
    ret = Integer.parseInt(pktStr.split(";")[2]);
    
    return ret;
    
  }
  
  private DatagramPacket make_packet(boolean seq, String data, int checksum, InetAddress ip, int port) {
    DatagramPacket packet = null;
    
    int s = (seq) ? (1) : (0);
    String packetData = s + ";" + data + ";" + checksum;
    packet = new DatagramPacket(packetData.getBytes(), packetData.length(), ip, port);
    
    return packet;
    
  }
  
  private boolean isAck(DatagramPacket packet, boolean seq) {
    boolean ret = false;
    
    if ((getPktData(packet).equalsIgnoreCase("ACK")) && (getPktSeq(packet) == seq))
      ret = true;
    
    return ret;
    
  }
  
}
