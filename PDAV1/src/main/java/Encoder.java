import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Encoder {
    private String format;
    private int width;
    private int height;
    private int maxValue;
    private double[][] yMatrix, uMatrix, vMatrix;
    private List<Block> yuvBlocksList = new ArrayList<>();

    public Encoder(String fileName){
        readFileData(fileName);
    }

    private void readFileData(String fileName) {
        try {
            File f = new File(fileName);
            Scanner fScanner = new Scanner(f);
            this.format = fScanner.nextLine();
            fScanner.nextLine();
            String[] dimensions = fScanner.nextLine().split(" ");
            width=Integer.parseInt(dimensions[0]);
            height=Integer.parseInt(dimensions[1]);
            maxValue = Integer.parseInt(fScanner.nextLine());
            this.yMatrix=new double[height][width];
            this.uMatrix=new double[height][width];
            this.vMatrix=new double[height][width];
            for (int i=0;i<height;i++){
                for (int j=0;j<width;j++){
                    int r = fScanner.nextInt();
                    int g = fScanner.nextInt();
                    int b = fScanner.nextInt();
                    double y = 0.299 * r + 0.587 * g + 0.114 * b;
                    double u = 128 - 0.1687 * r - 0.3312 * g + 0.5 * b;
                    double v = 128 + 0.5 * r - 0.4186 * g - 0.0813 * b;
                    yMatrix[i][j] = y;
                    uMatrix[i][j] = u;
                    vMatrix[i][j] = v;
                }
            }
            fScanner.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void divideMatrices(){
        for (int i=0;i<height;i=i+8){
            for (int j=0;j<width;j=j+8){
                this.storeY(i,j);
                this.storeU(i,j);
                this.storeV(i,j);
            }
        }
        System.out.println(this.yuvBlocksList);
    }

    private void storeU(int i, int j) {
        double[][] matrix8x8 = this.get8x8(i,j,uMatrix);
        double[][] u4x4 = get4x4(matrix8x8);
        this.yuvBlocksList.add(new Block("u",u4x4,i,j));

    }

    private void storeV(int i, int j) {
        double[][] matrix8x8 = this.get8x8(i,j,vMatrix);
        double[][] v4x4 = get4x4(matrix8x8);
        this.yuvBlocksList.add(new Block("v",v4x4,i,j));

    }

    private double[][] get4x4(double[][] matrix8x8) {
        double[][] matrix4x4 = new double[4][4];
        int a,b;
        a=-1;
        for (int i=0;i<8;i=i+2){
            a+=1;
            b=0;
            for (int j=0;j<8;j=j+2){
                double result=this.computeAvg(i,j,matrix8x8);
                matrix4x4[a][b]=result;
                b+=1;

            }
        }
        return matrix4x4;
    }

    private double computeAvg(int i, int j, double[][] matrix8x8) {
        float sum=0;
        for (int a=i;a<i+2;a++){
            for (int b=j;b<j+2;b++){
                sum +=matrix8x8[a][b];
            }
        }
        return sum/4;
    }

    private void storeY(int i, int j) {
        double[][] matrix8x8 = this.get8x8(i,j,yMatrix);
        this.yuvBlocksList.add(new Block("y",matrix8x8,i,j));
    }

    private double[][] get8x8(int i, int j,double[][] matrix) {
        int a=-1;
        int b;
        double[][] values = new double[8][8];
        for (int posI=i;posI<i+8;posI++){
            a+=1;
            b=0;
            for (int posJ=j;posJ<j+8;posJ++){
                values[a][b]=matrix[posI][posJ];
                b+=1;
            }
        }
        return values;
    }


    public String getFormat() {
        return format;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight(){
        return height;
    }

    public List<Block> getYuvBlocksList(){
        return yuvBlocksList;
    }
}
