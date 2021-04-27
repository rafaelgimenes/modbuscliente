import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.msg.WriteMultipleRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleInputRegister;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;

public class Principal {

	public static void main(String[] args) {
		String versao = "0.5";
		TCPMasterConnection con=null;
		String ip="";
		int porta=502;
		String[] configArgs = null; //Configurações de endereço e Registros, R1F90 (R=Registro,1=qtdeRegistros,F=final endian,90=AdressINI)
		String[] valores = null; //valores das variaveis tem que bater com os valores das configurações.
		int offsetRegistros = 0;
		Configuracoes[] configuracoes = null;

		/*  //passa pra bytes
        byte[] x = Utils.getBytesFromFloat((float)1562.1, false);

        System.out.println(Utils.getHex(x));
        System.exit(0);
        //divide em 2 registros
		 */        

		System.out.println("ModbusClient/Server:"+versao);
		
		
		
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
				System.out.println("Error: Getting args.");
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
			System.out.println("Error: Deatach Args");
			System.exit(1);
		}

		
		boolean isServeClosed= Utils.available(porta);
		System.out.println("isServerClosed:"+isServeClosed);
		
		
		if(!isServeClosed&&(ip.equals("slaveServer"))){
			try {
				ip = InetAddress.getLocalHost().getHostAddress()+"";
				System.out.println(ip);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		if(!ip.equals("slaveServer")){

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
				System.out.println("Error: Opening the Connection.");
				System.exit(1);
			}

			//trantando os dados.
			try {
				if (valores!=null&&configuracoes!=null) {
					for (int i = 0; i < valores.length; i++) {
						String valorOrig=valores[i] ;
						try {
							if (configuracoes[i].getTipo().equals("R")) {//tipo registros;
								Register[] registros = new Register[configuracoes[i].getQtdReg()];
								int enderecoIni = offsetRegistros + configuracoes[i].getEnderecoInicial();
								if (registros.length == 1) {//1 registro uma WORD inteiro direto
									//tratando casas
									int qtdeCasas = 0;
									int mult = 1;
									int valorEnv = 0;
									double valorEnD = 0.0d;
									
									valores[i] = valores[i].replaceAll("[^0-9.-]", "");
									if (valores[i].contains(".")) {
										qtdeCasas = valores[i].substring(valores[i].lastIndexOf(".") + 1).length();
										if (qtdeCasas == 1)
											mult = 10;
										if (qtdeCasas == 2)
											mult = 100;
										if (qtdeCasas == 3)
											mult = 1000;
										valorEnD = Double.parseDouble(valores[i]);
										valorEnD = valorEnD * mult;
										valorEnv = (int) valorEnD;
									} else {
										valorEnv = Integer.parseInt(valores[i]);
									}
									registros[0] = new SimpleRegister(valorEnv);
									try {
										WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(
												enderecoIni, registros);
								
										ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
										trans.setRequest(request);
										trans.execute();
										WriteMultipleRegistersResponse response = new WriteMultipleRegistersResponse();
										response = (WriteMultipleRegistersResponse) trans.getResponse();
										System.out.println(valorOrig + " Written:" + response.getHexMessage() + "WordCount:"
												+ response.getWordCount());
										
									} catch (Exception e) {
										System.out.println("Error: Writing the values R1");
									}
								} else if (registros.length == 2) {//2 registro 2 words
									byte[] valorB = null;
									float valorX = 0.0f;
									valores[i] = valores[i].replaceAll("[^0-9.-]", "");//remove caracteres

									if (configuracoes[i].getOrdemBytes().equals("F")) {
										valorX = Float.parseFloat(valores[i]);
										//passa pra bytes
										valorB = Utils.getBytesFromFloat((float) valorX, false);
										//divide em 2 registros
										registros[0] = new SimpleRegister(valorB[2], valorB[3]);
										registros[1] = new SimpleRegister(valorB[0], valorB[1]);
									} else if (configuracoes[i].getOrdemBytes().equals("B")) {
										valorX = Float.parseFloat(valores[i]);
										//passa pra bytes
										valorB = Utils.getBytesFromFloat((float) valorX, true);
										//divide em 2 registros
										registros[0] = new SimpleRegister(valorB[0], valorB[1]);
										registros[1] = new SimpleRegister(valorB[2], valorB[3]);
									} else if (configuracoes[i].getOrdemBytes().equals("A")) {
										valorX = Float.parseFloat(valores[i]);
										//passa pra bytes
										valorB = Utils.getBytesFromFloat((float) valorX, false);
										//divide em 2 registros
										registros[0] = new SimpleRegister(valorB[0], valorB[1]);
										registros[1] = new SimpleRegister(valorB[2], valorB[3]);
									} else if (configuracoes[i].getOrdemBytes().equals("C")) {
										valorX = Float.parseFloat(valores[i]);
										//passa pra bytes
										valorB = Utils.getBytesFromFloat((float) valorX, true);
										//divide em 2 registros
										registros[0] = new SimpleRegister(valorB[2], valorB[3]);
										registros[1] = new SimpleRegister(valorB[0], valorB[1]);
									} else if (configuracoes[i].getOrdemBytes().equals("D")) {
										valorX = Float.parseFloat(valores[i]);
										//passa pra bytes
										valorB = Utils.getBytesFromFloat((float) valorX, false);
										//divide em 2 registros
										registros[0] = new SimpleRegister(valorB[0], valorB[1]);
										registros[1] = new SimpleRegister(valorB[2], valorB[3]);
									} else if (configuracoes[i].getOrdemBytes().equals("E")) {
										valorX = Float.parseFloat(valores[i]);
										//passa pra bytes
										valorB = Utils.getBytesFromFloat((float) valorX, false);
										//divide em 2 registros
										registros[0] = new SimpleRegister(valorB[3], valorB[2]);
										registros[1] = new SimpleRegister(valorB[1], valorB[0]);
									} else if (configuracoes[i].getOrdemBytes().equals("G")) {
										valorX = Float.parseFloat(valores[i]);
										//passa pra bytes
										valorB = Utils.getBytesFromFloat((float) valorX, true);
										//divide em 2 registros
										registros[0] = new SimpleRegister(valorB[3], valorB[2]);
										registros[1] = new SimpleRegister(valorB[1], valorB[0]);
									}

									//cria o request
									try {
										WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(enderecoIni, registros);
										//System.out.println("request: "+request.getHexMessage());
										ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
										trans.setRequest(request);
										trans.execute();
										WriteMultipleRegistersResponse response = new WriteMultipleRegistersResponse();
										response = (WriteMultipleRegistersResponse) trans.getResponse();
										System.out.println(valorOrig + " Written:" + response.getHexMessage() + "WordCount:"
												+ response.getWordCount());
									} catch (Exception e) {
										System.out.println("Error: Writing the values R2");
									}

								}
							} 
						} catch (Exception e) {
							StackTraceElement l = e.getStackTrace()[0];
							String erro = l.getClassName()+"/"+l.getMethodName()+":"+l.getLineNumber()+" "+l.getFileName()+e.getMessage() +""+ e.getStackTrace();
							Utils.escreveTxt("modbusClienteErroTratandoEnviando.txt","\n"+Utils.pegarData2()+" "+Utils.pegarHora() + " " +erro+"", true);
							System.out.println("Error: Individual Handingle the data:"+valorOrig+" "+configuracoes[i].toString());
						}
					}
				}
			} catch (Exception e) {
				StackTraceElement l = e.getStackTrace()[0];
				String erro = l.getClassName()+"/"+l.getMethodName()+":"+l.getLineNumber()+" "+l.getFileName()+e.getMessage() +""+ e.getStackTrace();
				Utils.escreveTxt("modbusClienteErroTratandoEnviando.txt","\n"+Utils.pegarData2()+" "+Utils.pegarHora() + " " +erro+"", true);
				System.out.println("Error: Handingle the data");
			}

			//fecha a conexão.
			if(con!=null) {
				con.close();
			}

		}else{//localServer
			ModbusTCPListener listener = null;
			SimpleProcessImage spi = null;
			
			
			try {
				System.out.println("Modbus Slave (Server)");
				//1. prepare a process image
				spi = new SimpleProcessImage();
				
				for (int i = 0; i < valores.length; i++) {
					String valorOrig=valores[i] ;
					try {
						System.out.println("value " + i + ":" + valores[i]);
						Register[] registros = new Register[configuracoes[i].getQtdReg()];
						if (registros.length == 1) {//1 registro uma WORD inteiro direto
							//tratando casas
							int qtdeCasas = 0;
							int mult = 1;
							int valorEnv = 0;
							double valorEnD = 0.0d;
							valores[i] = valores[i].replaceAll("[^0-9.-]", "");
							if (valores[i].contains(".")) {
								qtdeCasas = valores[i].substring(valores[i].lastIndexOf(".") + 1).length();
								if (qtdeCasas == 1)
									mult = 10;
								if (qtdeCasas == 2)
									mult = 100;
								if (qtdeCasas == 3)
									mult = 1000;
								valorEnD = Double.parseDouble(valores[i]);
								valorEnD = valorEnD * mult;
								valorEnv = (int) valorEnD;
							} else {
								valorEnv = Integer.parseInt(valores[i]);
							}
							registros[0] = new SimpleRegister(valorEnv);
							spi.addRegister(registros[0]);
						} else if (registros.length == 2) {//2 registro 2 words
							byte[] valorB = null;
							float valorX = 0.0f;
							valores[i] = valores[i].replaceAll("[^0-9.-]", "");//remove caracteres

							if (configuracoes[i].getOrdemBytes().equals("F")) {
								valorX = Float.parseFloat(valores[i]);
								//passa pra bytes
								valorB = Utils.getBytesFromFloat((float) valorX, false);
								//divide em 2 registros
								registros[0] = new SimpleRegister(valorB[2], valorB[3]);
								registros[1] = new SimpleRegister(valorB[0], valorB[1]);

							} else if (configuracoes[i].getOrdemBytes().equals("B")) {
								valorX = Float.parseFloat(valores[i]);
								//passa pra bytes
								valorB = Utils.getBytesFromFloat((float) valorX, true);
								//divide em 2 registros
								registros[0] = new SimpleRegister(valorB[0], valorB[1]);
								registros[1] = new SimpleRegister(valorB[2], valorB[3]);
							}
							spi.addRegister(registros[0]);
							spi.addRegister(registros[1]);
						} 
					} catch (Exception e) {
						StackTraceElement l = e.getStackTrace()[0];
						String erro = l.getClassName()+"/"+l.getMethodName()+":"+l.getLineNumber()+" "+l.getFileName()+e.getMessage() +""+ e.getStackTrace();
						Utils.escreveTxt("modbusClienteErroTratandoEnviando.txt","\n"+Utils.pegarData2()+" "+Utils.pegarHora() + " " +erro+"", true);
						System.out.println("Error: Individual Handingle the data:"+valorOrig+" "+configuracoes[i].toString());
					
					}
				}

				
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				spi.addRegister(new SimpleRegister(0));
				
				//2. create the coupler holding the image
				ModbusCoupler.getReference().setProcessImage(spi);
				ModbusCoupler.getReference().setMaster(false);
				ModbusCoupler.getReference().setUnitID(15);
				System.out.println("registers count:" + spi.getRegisterCount());
				//3. create a listener with 3 threads in pool
				if (Modbus.debug) System.out.println("Listening...");
				listener = new ModbusTCPListener(50);
				InetAddress ia = InetAddress.getByName("0.0.0.0");  
				listener.setAddress(ia);
				listener.setPort(porta);
				listener.start();

			}catch (Exception e) {

				StackTraceElement l = e.getStackTrace()[0];
				String erro = l.getClassName()+"/"+l.getMethodName()+":"+l.getLineNumber()+" "+l.getFileName()+e.getMessage() +""+ e.getStackTrace();
				Utils.escreveTxt("modbusClienteErro.txt","\n"+Utils.pegarData2()+" "+Utils.pegarHora() + " " +erro+"", true);
				System.out.println("ErrorServer: Handingle the data");

			}
		}
	}
}
