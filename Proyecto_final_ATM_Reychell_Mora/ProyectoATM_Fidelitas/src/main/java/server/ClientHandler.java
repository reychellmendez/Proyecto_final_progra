
package server;
import dao.ClienteDAO; import dao.CuentaDAO;
import java.io.*; import java.net.Socket; import java.util.List;
public class ClientHandler implements Runnable {
  private final Socket socket;
  private final ClienteDAO clienteDAO=new ClienteDAO();
  private final CuentaDAO cuentaDAO=new CuentaDAO();
  public ClientHandler(Socket s){ this.socket=s; }
  @Override public void run() {
    try (var in=new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
         var out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"))) {
      String line; while((line=in.readLine())!=null){ String resp=procesar(line.trim()); out.write(resp); out.write("\n"); out.flush(); }
    } catch(Exception e){ e.printStackTrace(); }
  }
  private String procesar(String cmd){
    try{
      if(cmd.isEmpty()) return "ERR;Comando vacío";
      String[] p=cmd.split(";");
      switch(p[0]){
        case "AUTH": { if(p.length<3) return "ERR;Formato AUTH"; var id=clienteDAO.autenticar(p[1],p[2]); if(id==null) return "ERR;Credenciales inválidas"; return "OK;"+clienteDAO.nombrePorCedula(p[1]); }
        case "LIST_CUENTAS": { if(p.length<2) return "ERR;Formato LIST_CUENTAS"; var cs=cuentaDAO.listarPorCedula(p[1]);
          var sb=new StringBuilder(); for(var c:cs) sb.append("CUENTA;").append(c.numero).append(";").append(c.tipo).append(";").append(c.saldo).append("\n"); sb.append("END"); return sb.toString(); }
        case "SALDO": { if(p.length<2) return "ERR;Formato SALDO"; var s=cuentaDAO.saldo(p[1]); return s==null? "ERR;Cuenta no existe":"OK;"+s; }
        case "DEPOSITO": { if(p.length<3) return "ERR;Formato DEPOSITO"; double n=cuentaDAO.depositar(p[1], Double.parseDouble(p[2])); return "OK;"+n; }
        case "RETIRO": { if(p.length<3) return "ERR;Formato RETIRO"; double n=cuentaDAO.retirar(p[1], Double.parseDouble(p[2])); return "OK;"+n; }
        case "TRANSFERIR": { if(p.length<4) return "ERR;Formato TRANSFERIR"; double n=cuentaDAO.transferir(p[1], p[2], Double.parseDouble(p[3])); return "OK;"+n; }
        case "HISTORIAL": { if(p.length<3) return "ERR;Formato HISTORIAL"; var movs=cuentaDAO.historial(p[1], Integer.parseInt(p[2])); var sb=new StringBuilder(); for(String m:movs) sb.append("MOV;").append(m).append("\n"); sb.append("END"); return sb.toString(); }
        default: return "ERR;Comando desconocido";
      }
    } catch(IllegalArgumentException ex){ return "ERR;"+ex.getMessage(); }
      catch(Exception ex){ ex.printStackTrace(); return "ERR;Servidor:"+ex.getMessage(); }
  }
}
