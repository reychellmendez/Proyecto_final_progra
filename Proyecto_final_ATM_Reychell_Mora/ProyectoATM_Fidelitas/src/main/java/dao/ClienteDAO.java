
package dao;
import db.Conexion;
import java.sql.*;
public class ClienteDAO {
  public Integer autenticar(String cedula, String pin) throws Exception {
    String sql = "SELECT id FROM cliente WHERE cedula=? AND pin=?";
    try (Connection cn=Conexion.get(); PreparedStatement ps=cn.prepareStatement(sql)) {
      ps.setString(1, cedula); ps.setString(2, pin);
      try (ResultSet rs=ps.executeQuery()) { return rs.next()? rs.getInt(1) : null; }
    }
  }
  public String nombrePorCedula(String cedula) throws Exception {
    String sql="SELECT nombre FROM cliente WHERE cedula=?";
    try (Connection cn=Conexion.get(); PreparedStatement ps=cn.prepareStatement(sql)) {
      ps.setString(1, cedula); try (ResultSet rs=ps.executeQuery()) { return rs.next()? rs.getString(1) : null; }
    }
  }
}
