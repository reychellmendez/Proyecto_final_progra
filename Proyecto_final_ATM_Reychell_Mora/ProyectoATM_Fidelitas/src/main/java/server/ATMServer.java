
package server;
import java.net.*; import java.util.concurrent.*;
public class ATMServer {
  public static void main(String[] args) {
    int port=9090;
    try (ServerSocket ss=new ServerSocket(port)) {
      System.out.println("ATMServer escuchando en puerto "+port+" ...");
      ExecutorService pool=Executors.newCachedThreadPool();
      while(true) pool.submit(new ClientHandler(ss.accept()));
    } catch(Exception e){ e.printStackTrace(); }
  }
}
