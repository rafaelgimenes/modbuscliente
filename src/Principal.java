import java.net.InetAddress;
import java.net.UnknownHostException;

import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.ModbusIOException;
import net.wimpi.modbus.ModbusSlaveException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.msg.WriteMultipleRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;


public class Principal {

    public static void main(String[] args) {
        TCPMasterConnection con=null;
        String ip="";
        int porta=502;
        String[] configArgs = null; //Configurações de endereço e Registros, R1F90 (R=Registro,1=qtdeRegistros,F=final endian,90=AdressINI)
        String[] valores = null; //valores das variaveis tem que bater com os valores das configurações.
        int offsetRegistros = 0;
        Configuracoes[] configuracoes = null;
        //pegando os valores
        try {
            //192.168.25.5 502 0 R1F90,R1F70 1539,0.444
            //                 
            ip = args[0];
            porta = Integer.parseInt(args [1]);
            offsetRegistros=Integer.parseInt(args[2]);
            configArgs = args[3].split("\\,");//TIPO_QTDEREGISTROS_ORDEM_ENDERECOInicio
            valores = args[4].split("\\,");
            
            //se os valores e configuraçoes nao batem, já mata o processo.
            if(configArgs.length!=valores.length) {
                System.out.println("Configuration and values sizes does not match");
                System.exit(1);
            }
            
            //cria configuraçoes
            configuracoes = new Configuracoes[configArgs.length];
            for (int i = 0; i < configuracoes.length; i++) {
                String fTipo=configArgs[i].substring(0,1);
                int fqtdeReg=Integer.parseInt(configArgs[i].substring(1,2));
                boolean fOrdem;
                if(configArgs[i].substring(2,3).equals("B")) {
                    fOrdem=true;
                }else {
                    fOrdem=false;
                }
                int fEnderecoIni = Integer.parseInt(configArgs[i].substring(3,configArgs[i].length()));
                configuracoes[i]=new Configuracoes(fTipo,fqtdeReg , fOrdem,fEnderecoIni );
            } 
            
           
        } catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
            System.exit(1);
        }
        
        //iniciando a conexão.
        try {
            con = new TCPMasterConnection(InetAddress.getByName(ip));
            con.setPort(porta);
            con.setTimeout(1000);
            con.connect();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
        //trantando os dados.
        
        for (int i = 0; i < valores.length; i++) {
            if(configuracoes[i].getTipo().equals("R")) {//tipo registros;
                Register[] registros = new Register[configuracoes[i].getQtdReg()];
                int enderecoIni = offsetRegistros+configuracoes[i].getEnderecoInicial();
                if(registros.length==1) {//1 registro uma WORD inteiro direto
                    //tratando casas
                    int qtdeCasas=0;
                    int mult=1;
                    int valorEnv=0;
                    double valorEnD=0.0d;
                    if(valores[i].contains(".")) {
                        qtdeCasas = valores[i].substring(valores[i].lastIndexOf(".") + 1).length();
                        if(qtdeCasas==1)mult=10;
                        if(qtdeCasas==2)mult=100;
                        if(qtdeCasas==3)mult=1000;
                        valorEnD = Double.parseDouble(valores[i]);
                        valorEnD = valorEnD*mult;
                        valorEnv=(int) valorEnD;
                    }else {
                        valorEnv=Integer.parseInt(valores[i]);
                    }
                    registros[0] = new SimpleRegister(valorEnv);
                    WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(enderecoIni,registros);
                    System.out.println("request: "+request.getHexMessage());
                    ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
                    trans.setRequest(request);
                    try {
                        trans.execute();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    WriteMultipleRegistersResponse response = new WriteMultipleRegistersResponse();
                    response = (WriteMultipleRegistersResponse) trans.getResponse();
                    System.out.println("response:"+response.getHexMessage() + "WordCount:"+response.getWordCount());
                }//1
                
            }
            
        }
        
        double temp = 1433.99;
        int ref = 30;
        byte[] tempB = null;
        tempB = Utils.getBytesFromFloat((float)temp, false);
        Register[] registros = new Register[10];
        
        //quebrando o valor de temperatura em 2 words 4bytes
      /*Register reg = new SimpleRegister(tempB[0],tempB[1]);
        Register reg2 = new SimpleRegister(tempB[2],tempB[3]);
        Register reg3 = new SimpleRegister(tempB[0],tempB[3]);*/
        
        registros[0] = new SimpleRegister(tempB[0],tempB[1]);
        registros[1] = new SimpleRegister(tempB[2],tempB[3]);
        registros[2] = new SimpleRegister(0);
        registros[3] = new SimpleRegister(0);
        registros[4] = new SimpleRegister(0);
        registros[5] = new SimpleRegister(0);
        registros[6] = new SimpleRegister(0);
        registros[7] = new SimpleRegister(0);
        registros[8] = new SimpleRegister(0);
        registros[9] = new SimpleRegister(0);
        
        //quando tratata-se de um WriteMultipleRequest ele já começa no endereço 4000, então se o endereço/ref for 10 na verdade é 4010
        //WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(ref,new Register[]{reg,reg2,reg3});
        WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(ref,registros);
        System.out.println("request: "+request.getHexMessage());
        
        //4. Prepare the transaction
        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        //trans.setRequest(ReadReq);
        trans.setRequest(request);
        
        try {
            //aqui realmente que faz a gravação
            trans.execute();
        } catch (ModbusIOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ModbusSlaveException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ModbusException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        // pegando a resposta da transção
        WriteMultipleRegistersResponse response = new WriteMultipleRegistersResponse();
        response = (WriteMultipleRegistersResponse) trans.getResponse();
        System.out.println("response:"+response.getHexMessage() + "WordCount:"+response.getWordCount());
       
       
        //fecha a conexão.
        if(con!=null) {
            con.close();
        }
    }
}
