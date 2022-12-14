
public class Configuracoes {
    @Override
	public String toString() {
		return "Config [type=" + tipo + ", qtyReg=" + qtdReg + ", byteOrder=" + ordemBytes
				+ ", initAddress=" + enderecoInicial + "]";
	}
	String tipo;
    int qtdReg;
    String ordemBytes;
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
    public String isOrdemBytes() {
        return ordemBytes;
    }
    public void setOrdemBytes(String ordemBytes) {
        this.ordemBytes = ordemBytes;
    }
    public int getEnderecoInicial() {
        return enderecoInicial;
    }
    public void setEnderecoInicial(int enderecoInicial) {
        this.enderecoInicial = enderecoInicial;
    }
    public Configuracoes(String tipo, int qtdReg, String ordemBytes,
            int enderecoInicial) {
        super();
        this.tipo = tipo;
        this.qtdReg = qtdReg;
        this.ordemBytes = ordemBytes;
        this.enderecoInicial = enderecoInicial;
    }
    public String getOrdemBytes() {
        return ordemBytes;
    }
}
