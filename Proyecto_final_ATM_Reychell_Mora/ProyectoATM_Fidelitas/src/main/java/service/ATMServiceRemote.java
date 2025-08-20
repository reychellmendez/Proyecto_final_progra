
package service;
import java.io.*; import java.net.Socket; import java.util.*;
public class ATMServiceRemote implements ATMService {
  private final String host; private final int port;
  public ATMServiceRemote(String host,int port){ this.host=host; this.port=port; }
  private String send(String line) throws Exception {
    try(Socket s=new Socket(host,port);
        var in=new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
        var out=new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"))){
      out.write(line); out.write("\n"); out.flush();
      StringBuilder sb=new StringBuilder(); String r;
      while((r=in.readLine())!=null){ sb.append(r).append("\n"); if(r.equals("END")||r.startsWith("OK;")||r.startsWith("ERR;")) break; }
      return sb.toString().trim();
    }
  }
  public String login(String cedula,String pin) throws Exception { String resp=send("AUTH;"+cedula+";"+pin); if(resp.startsWith("OK;")) return resp.substring(3); throw new IllegalArgumentException(resp.substring(4)); }
  public List<String> listarCuentas(String cedula) throws Exception { String resp=send("LIST_CUENTAS;"+cedula); List<String> out=new ArrayList<>(); for(String line:resp.split("\n")){ if(line.equals("END")) break; if(line.startsWith("CUENTA;")) out.add(line.substring(7)); } return out; }
  public double saldo(String numero) throws Exception { String resp=send("SALDO;"+numero); if(resp.startsWith("OK;")) return Double.parseDouble(resp.substring(3)); throw new IllegalArgumentException(resp.substring(4)); }
  public double depositar(String numero,double monto) throws Exception { String resp=send("DEPOSITO;"+numero+";"+monto); if(resp.startsWith("OK;")) return Double.parseDouble(resp.substring(3)); throw new IllegalArgumentException(resp.substring(4)); }
  public double retirar(String numero,double monto) throws Exception { String resp=send("RETIRO;"+numero+";"+monto); if(resp.startsWith("OK;")) return Double.parseDouble(resp.substring(3)); throw new IllegalArgumentException(resp.substring(4)); }
  public double transferir(String origen,String destino,double monto) throws Exception { String resp=send("TRANSFERIR;"+origen+";"+destino+";"+monto); if(resp.startsWith("OK;")) return Double.parseDouble(resp.substring(3)); throw new IllegalArgumentException(resp.substring(4)); }
  public List<String> historial(String numero,int n) throws Exception { String resp=send("HISTORIAL;"+numero+";"+n); List<String> out=new ArrayList<>(); for(String line:resp.split("\n")){ if(line.equals("END")) break; if(line.startsWith("MOV;")) out.add(line.substring(4)); } return out; }
}
