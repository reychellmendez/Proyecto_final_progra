
package dao;
import db.Conexion;
import java.sql.*; import java.util.*;
public class CuentaDAO {
  public static class CuentaInfo { public String numero,tipo; public double saldo;
    public CuentaInfo(String n,String t,double s){ numero=n; tipo=t; saldo=s; } }
  public List<CuentaInfo> listarPorCedula(String cedula) throws Exception {
    String sql = "SELECT c.numero,c.tipo,c.saldo FROM cuenta c JOIN cliente cl ON cl.id=c.cliente_id WHERE cl.cedula=? ORDER BY c.numero";
    try (Connection cn=Conexion.get(); PreparedStatement ps=cn.prepareStatement(sql)) {
      ps.setString(1, cedula); try (ResultSet rs=ps.executeQuery()) {
        List<CuentaInfo> out=new ArrayList<>(); while(rs.next())
          out.add(new CuentaInfo(rs.getString(1), rs.getString(2), rs.getDouble(3)));
        return out;
      }
    }
  }
  public Double saldo(String numero) throws Exception {
    try (Connection cn=Conexion.get(); PreparedStatement ps=cn.prepareStatement("SELECT saldo FROM cuenta WHERE numero=?")) {
      ps.setString(1, numero); try (ResultSet rs=ps.executeQuery()) { return rs.next()? rs.getDouble(1): null; }
    }
  }
  public double depositar(String numero,double monto) throws Exception {
    try (Connection cn=Conexion.get()) { cn.setAutoCommit(false);
      try { actualizarSaldo(cn, numero, +monto); insertarMov(cn, numero, "DEPOSITO", monto, null);
            double nuevo=saldoConn(cn, numero); cn.commit(); return nuevo;
      } catch(Exception e){ cn.rollback(); throw e; } }
  }
  public double retirar(String numero,double monto) throws Exception {
    try (Connection cn=Conexion.get()) { cn.setAutoCommit(false);
      try { double s=saldoConn(cn, numero); if(s<monto) throw new IllegalArgumentException("Fondos insuficientes");
            actualizarSaldo(cn, numero, -monto); insertarMov(cn, numero, "RETIRO", monto, null);
            double nuevo=saldoConn(cn, numero); cn.commit(); return nuevo;
      } catch(Exception e){ cn.rollback(); throw e; } }
  }
  public double transferir(String origen,String destino,double monto) throws Exception {
    if (origen.equals(destino)) throw new IllegalArgumentException("Cuentas iguales");
    try (Connection cn=Conexion.get()) { cn.setAutoCommit(false);
      try { double s=saldoConn(cn, origen); if(s<monto) throw new IllegalArgumentException("Fondos insuficientes");
            actualizarSaldo(cn, origen, -monto); actualizarSaldo(cn, destino, +monto);
            insertarMov(cn, origen, "TRANSFERENCIA", monto, destino); insertarMov(cn, destino, "DEPOSITO", monto, origen);
            double nuevo=saldoConn(cn, origen); cn.commit(); return nuevo;
      } catch(Exception e){ cn.rollback(); throw e; } }
  }
  public List<String> historial(String numero,int n) throws Exception {
    String sql="SELECT tipo,monto,fecha,meta FROM transaccion t JOIN cuenta c ON c.id=t.cuenta_id WHERE c.numero=? ORDER BY t.id DESC LIMIT ?";
    try (Connection cn=Conexion.get(); PreparedStatement ps=cn.prepareStatement(sql)) {
      ps.setString(1, numero); ps.setInt(2, n); try (ResultSet rs=ps.executeQuery()) {
        List<String> out=new ArrayList<>(); while(rs.next()) out.add(rs.getString(1)+';'+rs.getBigDecimal(2)+';'+rs.getTimestamp(3)+';'+rs.getString(4)); return out; }
    }
  }
  private void actualizarSaldo(Connection cn,String numero,double delta) throws Exception {
    try (PreparedStatement ps=cn.prepareStatement("UPDATE cuenta SET saldo=saldo+? WHERE numero=?")) {
      ps.setDouble(1, delta); ps.setString(2, numero); if(ps.executeUpdate()==0) throw new IllegalArgumentException("Cuenta no existe: "+numero); }
  }
  private void insertarMov(Connection cn,String numero,String tipo,double monto,String meta) throws Exception {
    String sql="INSERT INTO transaccion (cuenta_id,tipo,monto,meta) VALUES ((SELECT id FROM cuenta WHERE numero=?),?,?,?)";
    try (PreparedStatement ps=cn.prepareStatement(sql)) { ps.setString(1, numero); ps.setString(2, tipo); ps.setDouble(3, monto); ps.setString(4, meta); ps.executeUpdate(); }
  }
  private double saldoConn(Connection cn,String numero) throws Exception {
    try (PreparedStatement ps=cn.prepareStatement("SELECT saldo FROM cuenta WHERE numero=?")) {
      ps.setString(1, numero); try (ResultSet rs=ps.executeQuery()) { if(!rs.next()) throw new IllegalArgumentException("Cuenta no existe: "+numero); return rs.getDouble(1); }
    }
  }
}
