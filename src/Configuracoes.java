
public class Configuracoes {
    String tipo;
    int qtdReg;
    boolean ordemBytes;
    int enderecoInicial;
    
    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    public int getQtdReg() {
        return qtdReg;
    }
    public void setQtdReg(int qtdReg) {
        this.qtdReg = qtdReg;
    }
    public boolean isOrdemBytes() {
        return ordemBytes;
    }
    public void setOrdemBytes(boolean ordemBytes) {
        this.ordemBytes = ordemBytes;
    }
    public int getEnderecoInicial() {
        return enderecoInicial;
    }
    public void setEnderecoInicial(int enderecoInicial) {
        this.enderecoInicial = enderecoInicial;
    }
    public Configuracoes(String tipo, int qtdReg, boolean ordemBytes,
            int enderecoInicial) {
        super();
        this.tipo = tipo;
        this.qtdReg = qtdReg;
        this.ordemBytes = ordemBytes;
        this.enderecoInicial = enderecoInicial;
    }
}
