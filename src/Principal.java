import java.net.InetAddress;

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
            //192.168.1.2 502 0 R1F6000,R1F6001,R2B6002,R2F6004,R2F6006,R2F6008,R1F6010 00033,1,1657.6,8.99,0.456,0.123,0
            ip = args[0];
            porta = Integer.parseInt(args [1]);
            offsetRegistros=Integer.parseInt(args[2]);
            configArgs = args[3].split("\\,");//TIPO_QTDEREGISTROS_ORDEM_ENDERECOInicio
            valores = args[4].split("\\,");
            
            //se os valores e configuraçoes nao batem, já mata o processo.
            if(configArgs.length!=valores.length) {
                Utils.escreveTxt("modbusClienteErroSeparandoArgs.txt","\n"+Utils.pegarData2()+" "+Utils.pegarHora()+"\nValues sizes does not match",true);
                System.exit(1);
            }
            
            //cria configuraçoes
            configuracoes = new Configuracoes[configArgs.length];
            for (int i = 0; i < configuracoes.length; i++) {
                String fTipo=configArgs[i].substring(0,1);
                int fqtdeReg=Integer.parseInt(configArgs[i].substring(1,2));
                String fOrdem=configArgs[i].substring(2,3);
                int fEnderecoIni = Integer.parseInt(configArgs[i].substring(3,configArgs[i].length()));
                configuracoes[i]=new Configuracoes(fTipo,fqtdeReg,fOrdem,fEnderecoIni);
            } 
            
           
        } catch (Exception e2) {
            StackTraceElement l = e2.getStackTrace()[0];
            String erro = l.getClassName()+"/"+l.getMethodName()+":"+l.getLineNumber()+" "+l.getFileName()+e2.getMessage() +""+ e2.getStackTrace();
            Utils.escreveTxt("modbusClienteErroSeparandoArgs.txt","\n"+Utils.pegarData2()+" "+Utils.pegarHora() + " " +erro+"", true);
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
        } catch (Exception e) {
            StackTraceElement l = e.getStackTrace()[0];
            String erro = l.getClassName()+"/"+l.getMethodName()+":"+l.getLineNumber()+" "+l.getFileName()+e.getMessage() +""+ e.getStackTrace();
            Utils.escreveTxt("modbusClienteErroConexao.txt","\n"+Utils.pegarData2()+" "+Utils.pegarHora() + " " +erro+"", true);
            System.exit(1);
        }
       
        //trantando os dados.
        try {
            if (valores!=null&&configuracoes!=null) {
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
                            valores[i]=valores[i].replaceAll("[^0-9.-]", "");
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
                        }else if(registros.length==2) {//2 registro 2 words
                            byte[] valorB = null;
                            float valorX=0.0f;
                            valores[i]=valores[i].replaceAll("[^0-9.-]", "");//remove caracteres
                           
                            if(configuracoes[i].getOrdemBytes().equals("F")) {
                                valorX = Float.parseFloat(valores[i]);
                                //passa pra bytes
                                valorB = Utils.getBytesFromFloat((float)valorX, false);
                                //divide em 2 registros
                                registros[0] = new SimpleRegister(valorB[2],valorB[3]);
                                registros[1] = new SimpleRegister(valorB[0],valorB[1]);
                            }else if(configuracoes[i].getOrdemBytes().equals("B")) {
                                valorX = Float.parseFloat(valores[i]);
                                //passa pra bytes
                                valorB = Utils.getBytesFromFloat((float)valorX, true);
                                //divide em 2 registros
                                registros[0] = new SimpleRegister(valorB[0],valorB[1]);
                                registros[1] = new SimpleRegister(valorB[2],valorB[3]);
                            }else if(configuracoes[i].getOrdemBytes().equals("A")) {
                                valorX = Float.parseFloat(valores[i]);
                                //passa pra bytes
                                valorB = Utils.getBytesFromFloat((float)valorX, false);
                                //divide em 2 registros
                                registros[0] = new SimpleRegister(valorB[0],valorB[1]);
                                registros[1] = new SimpleRegister(valorB[2],valorB[3]);
                            }else if(configuracoes[i].getOrdemBytes().equals("C")) {
                                valorX = Float.parseFloat(valores[i]);
                                //passa pra bytes
                                valorB = Utils.getBytesFromFloat((float)valorX, true);
                                //divide em 2 registros
                                registros[0] = new SimpleRegister(valorB[2],valorB[3]);
                                registros[1] = new SimpleRegister(valorB[0],valorB[1]);
                            }else if(configuracoes[i].getOrdemBytes().equals("D")) {
                                valorX = Float.parseFloat(valores[i]);
                                //passa pra bytes
                                valorB = Utils.getBytesFromFloat((float)valorX, false);
                                //divide em 2 registros
                                registros[0] = new SimpleRegister(valorB[0],valorB[1]);
                                registros[1] = new SimpleRegister(valorB[2],valorB[3]);
                            }else if(configuracoes[i].getOrdemBytes().equals("E")) {
                                valorX = Float.parseFloat(valores[i]);
                                //passa pra bytes
                                valorB = Utils.getBytesFromFloat((float)valorX, false);
                                //divide em 2 registros
                                registros[0] = new SimpleRegister(valorB[3],valorB[2]);
                                registros[1] = new SimpleRegister(valorB[1],valorB[0]);
                            }else if(configuracoes[i].getOrdemBytes().equals("G")) {
                                valorX = Float.parseFloat(valores[i]);
                                //passa pra bytes
                                valorB = Utils.getBytesFromFloat((float)valorX, true);
                                //divide em 2 registros
                                registros[0] = new SimpleRegister(valorB[3],valorB[2]);
                                registros[1] = new SimpleRegister(valorB[1],valorB[0]);
                            }
                            
                            //cria o request
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
                        }
                    }
                }
            }
        } catch (Exception e) {
            StackTraceElement l = e.getStackTrace()[0];
            String erro = l.getClassName()+"/"+l.getMethodName()+":"+l.getLineNumber()+" "+l.getFileName()+e.getMessage() +""+ e.getStackTrace();
            Utils.escreveTxt("modbusClienteErroTratandoEnviando.txt","\n"+Utils.pegarData2()+" "+Utils.pegarHora() + " " +erro+"", true);
        }
       
        //fecha a conexão.
        if(con!=null) {
            con.close();
        }
    }
}
