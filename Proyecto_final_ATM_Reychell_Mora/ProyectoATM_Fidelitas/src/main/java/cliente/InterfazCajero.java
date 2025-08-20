package cliente;
import service.ATMService; import service.ATMServiceRemote;
import javax.swing.*; import javax.swing.border.EmptyBorder;
import java.awt.*; import java.awt.event.ActionEvent; import java.util.List;

public class InterfazCajero extends JFrame {
  private final ATMService svc = new ATMServiceRemote("127.0.0.1", 9090);
  private CardLayout cards=new CardLayout(); private JPanel root=new JPanel(cards);
  private JTextField tfCedula=new JTextField(12); private JPasswordField pfPin=new JPasswordField(4); private JLabel lbLoginMsg=new JLabel(" ");
  private JLabel lbBienvenido=new JLabel(" "); private JComboBox<String> cbCuentas=new JComboBox<>(); private JTextField tfMonto=new JTextField(10);
  private JTextField tfDestino=new JTextField(10); private JTextArea taHist=new JTextArea(10,40); private JLabel lbSaldo=new JLabel("Saldo: -");
  private String cedulaActual;

  public InterfazCajero(){ super("ATM Cliente/Servidor - Fidelitas"); setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(780,560); setLocationRelativeTo(null);
    root.add(panelLogin(),"login"); root.add(panelMain(),"main"); setContentPane(root); }

  private JPanel panelLogin(){ JPanel p=new JPanel(new GridBagLayout()); p.setBorder(new EmptyBorder(40,40,40,40)); var c=new GridBagConstraints(); c.insets=new Insets(8,8,8,8); c.fill=GridBagConstraints.HORIZONTAL;
    JLabel t=new JLabel("Inicio de Sesión",SwingConstants.CENTER); t.setFont(t.getFont().deriveFont(Font.BOLD,20f)); c.gridwidth=2; c.gridx=0; c.gridy=0; p.add(t,c);
    c.gridwidth=1; c.gridy=1; p.add(new JLabel("Cédula:"),c); c.gridx=1; p.add(tfCedula,c);
    c.gridx=0; c.gridy=2; p.add(new JLabel("PIN:"),c); c.gridx=1; p.add(pfPin,c);
    JButton b=new JButton("Entrar"); b.addActionListener(this::doLogin); c.gridx=0; c.gridy=3; c.gridwidth=2; p.add(b,c);
    lbLoginMsg.setForeground(Color.RED); c.gridy=4; p.add(lbLoginMsg,c); return p; }

  private JPanel panelMain(){ JPanel p=new JPanel(new BorderLayout(10,10)); p.setBorder(new EmptyBorder(10,10,10,10));
    JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT)); lbBienvenido.setFont(lbBienvenido.getFont().deriveFont(Font.BOLD,16f)); top.add(lbBienvenido);
    JButton btnOut=new JButton("Salir"); btnOut.addActionListener(e->cards.show(root,"login")); top.add(Box.createHorizontalStrut(20)); top.add(btnOut); p.add(top,BorderLayout.NORTH);
    JPanel left=new JPanel(); left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));
    left.add(new JLabel("Cuenta:")); left.add(cbCuentas); JButton btnRef=new JButton("Refrescar cuentas"); btnRef.addActionListener(e->cargarCuentas()); left.add(Box.createVerticalStrut(6)); left.add(btnRef); left.add(Box.createVerticalStrut(12));
    JButton btnSaldo=new JButton("Consultar saldo"); btnSaldo.addActionListener(e->consultarSaldo()); left.add(btnSaldo); left.add(lbSaldo); left.add(Box.createVerticalStrut(12));
    JPanel pMonto=new JPanel(new FlowLayout(FlowLayout.LEFT)); pMonto.add(new JLabel("Monto:")); tfMonto.setColumns(10); pMonto.add(tfMonto); left.add(pMonto);
    JPanel ops=new JPanel(new FlowLayout(FlowLayout.LEFT)); JButton btnDep=new JButton("Depositar"); btnDep.addActionListener(e->operar("DEP")); JButton btnRet=new JButton("Retirar"); btnRet.addActionListener(e->operar("RET")); ops.add(btnDep); ops.add(btnRet); left.add(ops);
    JPanel tr=new JPanel(new FlowLayout(FlowLayout.LEFT)); tr.add(new JLabel("Destino:")); tfDestino.setColumns(8); tr.add(tfDestino); JButton btnTr=new JButton("Transferir"); btnTr.addActionListener(e->operar("TRA")); tr.add(btnTr); left.add(tr);
    JButton btnHist=new JButton("Historial (10)"); btnHist.addActionListener(e->cargarHistorial()); left.add(btnHist); p.add(left,BorderLayout.WEST);
    taHist.setEditable(false); p.add(new JScrollPane(taHist),BorderLayout.CENTER); return p; }

  private void doLogin(ActionEvent e){ lbLoginMsg.setText(" "); try{ String ced=tfCedula.getText().trim(); String pin=new String(pfPin.getPassword());
      String nombre=svc.login(ced,pin); cedulaActual=ced; lbBienvenido.setText("Bienvenido, "+nombre+" ("+ced+")"); cards.show(root,"main"); cargarCuentas();
    } catch(Exception ex){ lbLoginMsg.setText(ex.getMessage()); } }

  private void cargarCuentas(){
    try{
      cbCuentas.removeAllItems();
      List<String> filas=svc.listarCuentas(cedulaActual);
      for(String f:filas){
        String[] a=f.split(";");
        cbCuentas.addItem(a[0]+" | "+a[1]+" | ₡"+a[2]);
      }

      // AUTO: seleccionar 1ra cuenta y cargar saldo + historial
      if (cbCuentas.getItemCount() > 0) {
        cbCuentas.setSelectedIndex(0);
        consultarSaldo();
        cargarHistorial();
      }

    } catch(Exception ex){
      JOptionPane.showMessageDialog(this, ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
    }
  }

  private String cuentaSel(){ Object sel=cbCuentas.getSelectedItem(); if(sel==null) return null; String s=sel.toString(); int i=s.indexOf(" | "); return (i>0)? s.substring(0,i):s; }

  private void consultarSaldo(){ String cuenta=cuentaSel(); if(cuenta==null) return; try{ double s=svc.saldo(cuenta); lbSaldo.setText("Saldo: ₡"+s); } catch(Exception ex){ JOptionPane.showMessageDialog(this, ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); } }

  private void operar(String tipo){ String cuenta=cuentaSel(); if(cuenta==null) return; try{ double monto=Double.parseDouble(tfMonto.getText().trim()); double nuevo;
      if("DEP".equals(tipo)) nuevo=svc.depositar(cuenta,monto);
      else if("RET".equals(tipo)) nuevo=svc.retirar(cuenta,monto);
      else { String dest=tfDestino.getText().trim(); nuevo=svc.transferir(cuenta,dest,monto); }
      lbSaldo.setText("Saldo: ₡"+nuevo); cargarCuentas(); cargarHistorial();
    } catch(NumberFormatException ex){ JOptionPane.showMessageDialog(this,"Monto inválido","Error",JOptionPane.ERROR_MESSAGE);
    } catch(Exception ex){ JOptionPane.showMessageDialog(this, ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); } }

  private void cargarHistorial(){ String cuenta=cuentaSel(); if(cuenta==null) return; try{ List<String> movs=svc.historial(cuenta,10); StringBuilder sb=new StringBuilder();
      for(String m:movs){ String[] a=m.split(";"); sb.append(String.format("%-14s  ₡%-12s  %s   %s%n", a[0], a[1], a[2], (a.length>3? a[3]:""))); } taHist.setText(sb.toString());
    } catch(Exception ex){ JOptionPane.showMessageDialog(this, ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); } }
}
