public class Block {
    private String type;
    private double[][] matrix;
    private int posI,posJ;

    public Block(String type,double[][] matrix,int posI,int posJ){
        this.type=type;
        this.matrix=matrix;
        this.posI=posI;
        this.posJ=posJ;

    }

    public double[][] getMatrix() {
        return matrix;
    }

    public int getPosI() {
        return posI;
    }

    public int getPosJ() {
        return posJ;
    }

    public String getType() {
        return type;
    }

    public void setMatrix(double[][] matrix) {
        this.matrix = matrix;
    }

    public void setPosI(int posI) {
        this.posI = posI;
    }

    public void setPosJ(int posJ) {
        this.posJ = posJ;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder value= new StringBuilder();
        for(int i=0;i<matrix.length;i++){
            for(int j=0;j<matrix.length;j++){
                value.append(matrix[i][j]);
                value.append(" ");
            }
            value.append("\n");
        }

        return "Block{" +
                "values=" + value+
                ", type='" + type + '\'' +
                ", position=" + posI +" " +posJ+
                '}';
    }
}
