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
    double[][] quantizationMatrix=new double[][]{
            {6 ,  4,   4 ,  6 ,  10 , 16 , 20 , 24},
            {5  , 5 ,  6 ,  8 ,  10 , 23 , 24 , 22},
            {6  , 5  , 6 ,  10 , 16 , 23 , 28 , 22},
            {6  , 7 ,  9 ,  12 , 20 , 35 , 32 , 25},
            {7  , 9 ,  15 , 22 , 27 , 44 , 41 , 31},
            {10,  14 , 22 , 26  ,32,  42  ,45,  37},
            {20,  26  ,31  ,35  ,41 , 48 , 48 , 40},
            {29,  37 , 38  ,39 , 45  ,40,  41  ,40}
    };

    public Encoder(String fileName) {
        readFileData(fileName);
    }

    private void readFileData(String fileName) {
        try {
            File f = new File(fileName);
            Scanner fScanner = new Scanner(f);
            this.format = fScanner.nextLine();
            fScanner.nextLine();
            String[] dimensions = fScanner.nextLine().split(" ");
            width = Integer.parseInt(dimensions[0]);
            height = Integer.parseInt(dimensions[1]);
            maxValue = Integer.parseInt(fScanner.nextLine());
            this.yMatrix = new double[height][width];
            this.uMatrix = new double[height][width];
            this.vMatrix = new double[height][width];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
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

    public void divideMatrices() {
        for (int i = 0; i < height; i = i + 8) {
            for (int j = 0; j < width; j = j + 8) {
                this.storeY(i, j);
                this.storeU(i, j);
                this.storeV(i, j);
            }
        }
        //System.out.println(this.yuvBlocksList);
    }

    private void storeU(int i, int j) {
        double[][] matrix8x8 = this.get8x8(i, j, uMatrix);
        double[][] u4x4 = get4x4(matrix8x8);
        this.yuvBlocksList.add(new Block("u", u4x4, i, j));

    }

    private void storeV(int i, int j) {
        double[][] matrix8x8 = this.get8x8(i, j, vMatrix);
        double[][] v4x4 = get4x4(matrix8x8);
        this.yuvBlocksList.add(new Block("v", v4x4, i, j));

    }

    private double[][] get4x4(double[][] matrix8x8) {
        double[][] matrix4x4 = new double[4][4];
        int a, b;
        a = -1;
        for (int i = 0; i < 8; i = i + 2) {
            a += 1;
            b = 0;
            for (int j = 0; j < 8; j = j + 2) {
                double result = this.computeAvg(i, j, matrix8x8);
                matrix4x4[a][b] = result;
                b += 1;

            }
        }
        return matrix4x4;
    }

    private double computeAvg(int i, int j, double[][] matrix8x8) {
        float sum = 0;
        for (int a = i; a < i + 2; a++) {
            for (int b = j; b < j + 2; b++) {
                sum += matrix8x8[a][b];
            }
        }
        return sum / 4;
    }

    private void storeY(int i, int j) {
        double[][] matrix8x8 = this.get8x8(i, j, yMatrix);
        this.yuvBlocksList.add(new Block("y", matrix8x8, i, j));
    }

    private double[][] get8x8(int i, int j, double[][] matrix) {
        int a = -1;
        int b;
        double[][] values = new double[8][8];
        for (int posI = i; posI < i + 8; posI++) {
            a += 1;
            b = 0;
            for (int posJ = j; posJ < j + 8; posJ++) {
                values[a][b] = matrix[posI][posJ];
                b += 1;
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

    public int getHeight() {
        return height;
    }

    public List<Block> getYuvBlocksList() {
        return yuvBlocksList;
    }

    public void FDCTandQuantization() {


        for (Block block : yuvBlocksList) {
            if (!block.getType().equals("y")) {
                block.setMatrix(reverseSubsampling(block.getMatrix()));
            }

            double[][] mat = block.getMatrix();
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    mat[i][j] -= 128;
                }
            }
            block.setMatrix(mat);

            block.setMatrix(FDCT(block));

            block.setMatrix(quantization(block));


        }
    }

    private double[][] quantization(Block block) {
        double [][] mat=block.getMatrix();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                mat[i][j] = mat[i][j]/quantizationMatrix[i][j];
            }
        }
        return mat;
    }

    private double[][] FDCT(Block block) {
        double[][] DCT = new double[8][8];
        for (int u = 0; u < 8; u++) {
            for (int v = 0; v < 8; v++) {

                if (u == 0 && v == 0) {
                    DCT[u][v] = (float) 1 / 4 * 1 / Math.sqrt(2) * 1 / Math.sqrt(2);
                } else if (u > 0 && v > 0) {
                    DCT[u][v] = (float) 1 / 4;

                } else {
                    DCT[u][v] = (float) 1 / 4 * 1 / Math.sqrt(2);

                }
                double sum = 0;
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        sum += block.getMatrix()[x][y] * Math.cos(((2 * x + 1) * u * Math.PI) / 16) *
                                Math.cos(((2 * y + 1) * v * Math.PI) / 16);
                    }
                }
                DCT[u][v] *= sum;
            }
        }
        return DCT;
    }

    private double[][] reverseSubsampling(double[][] matrix) {
        double[][] reversed = new double[8][8];
        int posI = 0;
        for (int i = 0; i < 4; i++) {
            int posJ = 0;
            for (int j = 0; j < 4; j++) {
                reversed[posI][posJ] = matrix[i][j];
                reversed[posI + 1][posJ] = matrix[i][j];
                reversed[posI][posJ + 1] = matrix[i][j];
                reversed[posI + 1][posJ + 1] = matrix[i][j];
                posJ += 2;
            }
            posI += 2;
        }
        return reversed;
    }
}
