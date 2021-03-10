
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils
{
    public static PrintStream p = null;
    public static boolean ArquivoExiste(String caminho){
        
        boolean existe = (new File(caminho)).exists();   
        return existe;
    } 
    
    public static String usedAddress = "ss";
    public static boolean available(int port) {
        

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            usedAddress=ss.getInetAddress()+"";
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }
          
            if (ss != null) {
            	  usedAddress=ss.getInetAddress()+"";
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }
    public static void  escreveTxt(String Arquivo, String Texto, boolean append) {
        FileOutputStream arquivoSaida = null;
        if(append) {
            try {
                arquivoSaida = new FileOutputStream (Arquivo,true);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                System.out.println("Erro append arquivo ou caminho Inexistente:"+pegarHora()+" " +e.toString());
                if(e.toString().contains("sendo usado por outro processo")){
                    System.out.println("Erro append arquivo Sendo Usado"+pegarHora()+" " +e.toString());
                }
                try {
                    arquivoSaida = new FileOutputStream (Arquivo);
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    System.out.println("Erro append arquivo Sendo Usado:"+pegarHora()+" " +e.toString());
                }
            }
        }
        else {//não é append
            try {
                arquivoSaida = new FileOutputStream (Arquivo);
            } catch (FileNotFoundException e) {
                System.out.println("Erro arquivo ou caminho Inexistente:"+pegarHora()+" " +e.toString());
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        
        try {
            FileChannel channel = arquivoSaida.getChannel();
            FileLock lock = channel.tryLock();
            
            if(lock==null){
                System.out.println("Arquivo Esta Bloqueado "+pegarHora()+" ");
            }else{
                //grava
                try {
                    p = new  PrintStream ( arquivoSaida );
                    p.print(Texto);
                    lock.release();
                    p.close();
                } catch (Exception e) {
                    System.out.println("Erro PrintStream "+pegarHora()+" " + e.toString());
                }
                
                try {
                    
                    arquivoSaida.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }
    
    
    @SuppressWarnings("deprecation")
    public static String pegarData2() {
           String Data;
           Date cal = new Date();
           SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
           Data = DATE_FORMAT.format(cal);
           return Data;
       }
    
    @SuppressWarnings("deprecation")
    public static String pegarHora() {
           String Data;
           Date cal = new Date();
           SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
           Data = DATE_FORMAT.format(cal);
           return Data;
       }
    public static File[] ListaDiretorio(String Caminho, String Extensoes){

            File diretorio = new File(Caminho);
            
            String[] lista = diretorio.list();
            if (lista == null) {
                System.out.println("Diretorio nao existe");
            } else {
                for (int i=0; i<lista.length; i++) {
                    // Get filename of file or directory
                    String nomearquivo = lista[i];
                    System.out.println("nome arquibvo "+nomearquivo);
                }
            }
            
           
            
            
            // The list of files can also be retrieved as File objects
            File[] arquivos = diretorio.listFiles();
            
             
           
            return arquivos;
        
    }

    public static String FormataDisplay (double Valor)
    {
        
        
        String Resultado = "#####";

        // De 0 a -10
        if ((Valor > -10 ) && (Valor < 0))
            Resultado = "#####0.000";
        
        // De -10 a -100
        if ((Valor > -100) && (Valor <= -10))
            Resultado = "#####0.00";
        
        // De -100 a -1000
        if ((Valor > -1000) && (Valor <=-100))
            Resultado = "#####0.0";

        // De 0 a 10
        if ((Valor >= 0 ) && (Valor < 10))
            Resultado = "#####0.000";
        
        // De 10 a 100
        if ((Valor >= 10) && (Valor < 100))
            Resultado = "#####0.00";
        
        // De 100 a 1000
        if ((Valor >= 100) && (Valor < 1000))
            Resultado = "#####0.0";

        // De 1000 a 10000
        if ((Valor >=1000) && (Valor < 10000))
            Resultado = "####";
        
        return Resultado; 
    }
    
  

   
   

    /**
     * Conversao Celsius para Fh 
     */
    public static double converteCtoF(double Valor)
    {   
        DecimalFormat formatador = new DecimalFormat("#####0.0");
        
        double auxValor = 0;
        //auxValor = Math.round( (9 * Valor / 5) + 32 );    
        auxValor = (9 * Valor / 5) + 32;
        auxValor = Double.parseDouble(formatador.format(auxValor).replace(',', '.'));
        
        return auxValor;
    }   

    /**
     * Conversao Fh para Celsius 
     */ 
    public static double converteFtoC(double Valor)
    {   
        double auxValor = 0;
        auxValor = 5 * (Valor - 32) / 9;
        return auxValor;
    }   
    
    /**
     * Convertendo Celsius para Fh DELTA 
     */
    public static double ConverteDeltaCtoF(double Valor)
    {   
        DecimalFormat formatador = new DecimalFormat("#####0.00");
        
        double auxValor = 0;
        //auxValor = Math.round( (Valor * 1.8) );       
        auxValor = Valor * 1.8;
        
        auxValor = Double.parseDouble(formatador.format(auxValor).replace(',', '.'));
        
        return auxValor;
    }   

    /**
     * Convertendo FH para Celsius DELTA 
     */
    public static float ConverteDeltaFtoC(float Valor)
    {   
        float auxValor = 0;
        auxValor = (Valor / 1.8F);      

        return auxValor;
    }   
        
    /**
     * Capturar a Data/Hora atual do Sistema, no formato desejado <BR>
     * 
     * Exemplos: <BR>
     * 2007-04-10 ==> yyyy-MM-dd <BR>
     * 10/04/2007 ==> dd/MM/yyyy <BR>
     * 10:36:20   ==> hh:mm:ss   <BR>
     * 10:40      ==> hh:mm      <BR>
     * entre outros...
     */
    public static String getDataHoraAtual(String formato) {
        SimpleDateFormat formatador = new SimpleDateFormat(formato);
        
        Date data = new Date();
        
        return formatador.format(data);
    }
    
    public static String formataVariavelAscii(double Valor, String formato, String separadorDecimal, String PrefixoDesejado) {
        String retorno = "";
        String auxValor= "";
        
        DecimalFormat formatadorDecimal = null;
        
        // Tratando caso nao tenha o que formatar
        if (String.valueOf(Valor).length() == 0) {
            for (int i = 0; i < formato.length(); i++) {
                retorno = retorno + PrefixoDesejado;
            }
        } else {
            // Formatando o valor
            formatadorDecimal = new DecimalFormat(formato.replace(',', '.'));
            auxValor = formatadorDecimal.format(Valor);
    
            // Calculando numero de prefixos a ser incluido no valor formatado
            int diferenca = formato.length() - auxValor.length();
            
            // Preenchendo variavel com prefixos
            for (int i = 0; i < diferenca; i++) {
                auxValor = PrefixoDesejado + auxValor;
            }   
            
            // Trocando Separador Decimal se necessario
            if (separadorDecimal.equals(",")) {
                auxValor = auxValor.replace('.', ',');
            }
                        
            retorno = auxValor;
        }       
        return retorno;
    }
    
    static public String LerArquivo(String caminhoArquivo) {
        File arquivoLido = new File(caminhoArquivo);
        //...checks on aFile are elided
        StringBuilder conteudo = new StringBuilder();
        
        try {
          //use buffering, reading one line at a time
          //FileReader always assumes default encoding is OK!
          BufferedReader input =  new BufferedReader(new FileReader(arquivoLido));
          try {
            String line = null; //not declared within while loop
            /*
            * readLine is a bit quirky :
            * it returns the content of a line MINUS the newline.
            * it returns null only for the END of the stream.
            * it returns an empty String if two newlines appear in a row.
            */
            while (( line = input.readLine()) != null){
                conteudo.append(line);
               // conteudo.append(System.getProperty("line.separator"));
            }
          }
          finally {
            input.close();
          }
        }
        catch (IOException ex){
          ex.printStackTrace();
        }
        
        return conteudo.toString();
      }
    
    public static void gravaLinha(String linha, String nomeArquivo, String append) throws IOException {
        FileOutputStream arquivoSaida = null;
        PrintStream escrita = null;
        if(append.equals("true")) {
            arquivoSaida = new FileOutputStream (nomeArquivo,true);
        }
        else {
            arquivoSaida = new FileOutputStream (nomeArquivo);
        }    
        escrita = new  PrintStream ( arquivoSaida );
        
        escrita.println(linha);
        
        escrita.close();
        arquivoSaida.close();
        
    }
    
    public static void gravaArquivo(String linha, String nomeArquivo, String append) throws IOException {
        FileOutputStream arquivoSaida = null;
        PrintStream escrita = null;
        if(append.equals("true")) {
            arquivoSaida = new FileOutputStream (nomeArquivo,true);
        }
        else {
            arquivoSaida = new FileOutputStream (nomeArquivo);
        }    
        escrita = new  PrintStream ( arquivoSaida );
        
        escrita.print(linha);
        
        escrita.close();
        arquivoSaida.close();
        
    }
    
    public static String getHostname() {
        InetAddress iAddress = null;
        String hostName = "";
        try {
            iAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            hostName="rgimenes";
        }
        
         if(iAddress!=null) {
             hostName = iAddress.getHostName();
            // System.out.println("hostName:" +hostName);
         }
        
        return hostName;
    }
    
    
    /**
     * Retorna se o valor double é valido, deve ser usado em classes que calculam formulas.
     * Acontece de colocarem valores muito altos nas constantes das formulas extrapolando o valor para 
     * o infinito,
     */
    public static boolean isValidNumber(double vlr) {
        boolean resul = true;
        if(java.lang.Double.isInfinite(vlr) || java.lang.Double.isNaN(vlr)) {
            resul=false;
        }
        
        try {
            double a = Double.parseDouble(""+vlr);
        } catch (Exception e) {
            resul = false;
        }
        
        if(!resul) {
            System.out.println("isValidNumber filtrou");
        }
        
        return resul;
    }
    
    public static boolean isNegative(double vlr) {
        boolean resul = false;
        if(vlr<0) {
            resul=true;
            System.out.println("isNegative filtrou");
        }
        
        return resul;
    }
    
   
    public static byte[] getBytesFromShort(short value, boolean isNative) {  
        if (isNative)  
            return ByteBuffer.allocate(2).order(ByteOrder.nativeOrder()).putShort(value).array();  
        else  
            return ByteBuffer.allocate(2).putShort(value).array();  
    }  
      
    public static short getShortFromBytes(byte[] array, boolean isNative) {  
        if (isNative)  
            return ByteBuffer.wrap(array, 0, 2).order(ByteOrder.nativeOrder()).getShort();  
        else  
            return ByteBuffer.wrap(array, 0, 2).getShort();  
    }  
      
    public static byte[] getBytesFromInt(int value, boolean isNative) {  
        if (isNative)  
            return ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(value).array();  
        else  
            return ByteBuffer.allocate(4).putInt(value).array();  
    }  
      
    public static int getIntFromBytes(byte[] array, boolean isNative) {  
        if (isNative)  
            return ByteBuffer.wrap(array, 0, 4).order(ByteOrder.nativeOrder()).getInt();  
        else  
            return ByteBuffer.wrap(array, 0, 4).getInt();  
    }  
      
    public static byte[] getBytesFromLong(long value, boolean isNative) {  
        if (isNative)  
            return ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putLong(value).array();  
        else  
            return ByteBuffer.allocate(8).putLong(value).array();  
    }  
      
    public static long getLongFromBytes(byte[] array, boolean isNative) {  
        if (isNative)  
            return ByteBuffer.wrap(array, 0, 8).order(ByteOrder.nativeOrder()).getLong();  
        else  
            return ByteBuffer.wrap(array, 0, 8).getLong();  
    }  
      
    public static byte[] getBytesFromFloat(float value, boolean isNative) {  
        if (isNative)  
            return ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putFloat(value).array();  
        else  
            return ByteBuffer.allocate(4).putFloat(value).array();  
    }    
      
    public static float getFloatFromBytes(byte[] array, boolean isNative) {  
        if (isNative)  
            return ByteBuffer.wrap(array, 0, 4).order(ByteOrder.nativeOrder()).getFloat();  
        else  
            return ByteBuffer.wrap(array, 0, 4).getFloat();  
    }  
      
    public static byte[] getBytesFromDouble(double value, boolean isNative) {  
        if (isNative)  
            return ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putDouble(value).array();  
        else  
            return ByteBuffer.allocate(8).putDouble(value).array();  
    }  
      
    public static double getDoubleFromBytes(byte[] array, boolean isNative) {  
        if (isNative)  
            return ByteBuffer.wrap(array, 0, 8).order(ByteOrder.nativeOrder()).getDouble();  
        else  
            return ByteBuffer.wrap(array, 0, 8).getDouble();  
    }  
    
    public static final byte[] intToByteArray(int value) {
        return new byte[] {
            (byte)(value & 0xff),
            (byte)(value >> 8 & 0xff),
            (byte)(value >> 16 & 0xff),
            (byte)(value >>> 24)
        };
    }
    
 public static String getHex(byte[] raw) {  
        
        final String HEXES = "0123456789ABCDEF";  
  
        if (raw == null) {  
            return null;  
        }  
        final StringBuilder hex = new StringBuilder(2 * raw.length);  
        for (final byte b : raw) {  
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F))).append(" ");  
        }  
        return hex.toString();  
    }  
 
 public static String getHex(int raw) {  
     
     final String HEXES = "0123456789ABCDEF";  

     final StringBuilder hex = new StringBuilder(2);  
         hex.append(HEXES.charAt((raw & 0xF0) >> 4)).append(HEXES.charAt((raw & 0x0F))); 
     return hex.toString();  
 }
 
 //converte uma String hexadecimal para um array de byte
 public static final byte[] toByteArray(final String hexaString) {
     final char[] chars = hexaString.toCharArray();
     final int length = chars.length;
     final byte[] byteArray = new byte[length / 2];
     String hexa = null;
     for (int i = 0, j = 0; i < length; i += 2, j++) {
         hexa = new String(new char[] {chars[i], chars[i + 1]});
         byteArray[j] = toByte(hexa);
     }
     return byteArray;
 }
 //converte uma String hexadecimal para um byte
 public static final byte toByte(final String hexa) {
     return (byte) Short.parseShort(hexa.toUpperCase(), 16);
 }
 public static int contarLinhasArquivos(String filename) throws IOException {
     InputStream is = new BufferedInputStream(new FileInputStream(filename));
     byte[] c = new byte[1024];
     int count = 0;
     int readChars = 0;
     while ((readChars = is.read(c)) != -1) {
         for (int i = 0; i < readChars; ++i) {
             if (c[i] == '\n')
                 ++count;
         }
     }
     return count;
 }
 
 public static int binaryToInteger(String binary) {
     char []cA = binary.toCharArray();
     int result = 0;
     for (int i = cA.length-1;i>=0;i--){
         //111 , length = 3, i = 2, 2^(3-3) + 2^(3-2)
         //                    0           1  
         if(cA[i]=='1') result+=Math.pow(2, cA.length-i-1);
     }
     return result;
 }
 
 public static byte binarioPraByte(String binary) {
     //meubyte |= (1 << posBit);
     char []cA = binary.toCharArray();
     byte result = 0;
     for (int i = cA.length-1;i>=0;i--){
         //111 , length = 3, i = 2, 2^(3-3) + 2^(3-2)
         //                    0           1  
         if(cA[i]=='1') result+=Math.pow(2, cA.length-i-1);
     }
     return result;
 }
 public static String removeNonNumber(String st) {
     return st.replaceAll("[^\\d.]", "");
     
 }


}
