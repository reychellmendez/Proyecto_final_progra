package service;

import java.util.List;

/**
 * Contrato del servicio remoto del cajero.
 * Implementación sugerida: ATMServiceRemote (sockets).
 */
public interface ATMService {
    /** Autentica al cliente y retorna el nombre si es válido. */
    String login(String cedula, String pin) throws Exception;

    /** Devuelve filas "numero;tipo;saldo" de las cuentas del cliente. */
    List<String> listarCuentas(String cedula) throws Exception;

    /** Consulta saldo de una cuenta. */
    double saldo(String numero) throws Exception;

    /** Realiza un depósito y retorna el nuevo saldo. */
    double depositar(String numero, double monto) throws Exception;

    /** Realiza un retiro y retorna el nuevo saldo. */
    double retirar(String numero, double monto) throws Exception;

    /** Transfiere monto de origen a destino y retorna el nuevo saldo de origen. */
    double transferir(String origen, String destino, double monto) throws Exception;

    /** Devuelve últimos N movimientos como "tipo;monto;fecha;meta". */
    List<String> historial(String numero, int n) throws Exception;
}
