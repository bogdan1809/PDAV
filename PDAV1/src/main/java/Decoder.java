import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Decoder {
    private String format;
    private int width, height, maxValue;
    private double[][] yMatrix, uMatrix, vMatrix;
    private List<Block> yuvBlocksList = new ArrayList<>();

    public Decoder(String format, int width, int height, int maxValue, List<Block> yuvBlocksList) {
        this.yuvBlocksList = yuvBlocksList;
        this.format = format;
        this.width = width;
        this.height = height;
        this.maxValue = maxValue;
        this.yMatrix = new double[height][width];
        this.uMatrix = new double[height][width];
        this.vMatrix = new double[height][width];
    }

    private void writeFile(String fileName) {
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write(this.format);
            fileWriter.write("\n");
            fileWriter.write(this.width + " " + this.height);
            fileWriter.write("\n");
            fileWriter.write(Integer.toString(maxValue));
            fileWriter.write("\n");

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int r = (int) Math.round(yMatrix[i][j] + 1.402 * (vMatrix[i][j] - 128));
                    int g = (int) Math.round(yMatrix[i][j] - 0.344 * (uMatrix[i][j] - 128) - 0.714 * (vMatrix[i][j] - 128));
                    int b = (int) Math.round(yMatrix[i][j] + 1.772 * (uMatrix[i][j] - 128));

                    if (r < 0) {
                        r = 0;
                    } else if (r > 255) {
                        r = 255;
                    }
                    if (g < 0) {
                        g = 0;
                    } else if (g > 255) {
                        g = 255;
                    }
                    if (b < 0) {
                        b = 0;
                    } else if (b > 255) {
                        b = 255;
                    }

                    fileWriter.write(r+"");
                    fileWriter.write("\n");

                    fileWriter.write(g+"");
                    fileWriter.write("\n");

                    fileWriter.write(b+"");
                    fileWriter.write("\n");
                }


            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decode(String fileName) {
        for (Block block : yuvBlocksList) {
            String type = block.getType();
            if (type.equals("y")) {
                this.decodeMatrixY(block);
               }
            else if (type.equals("u")){
                this.decodeMatrixUV(block,"u");
            }
            else{
                this.decodeMatrixUV(block,"v");
            }
        }

        writeFile(fileName);
    }

    private void decodeMatrixUV(Block block, String type) {


        int posI=block.getPosI();
        for (int i=0;i<4;i++){
            int posJ=block.getPosJ();
            for (int j=0;j<4;j++){
                if (type.equals("u")){
                    uMatrix[posI][posJ]=block.getMatrix()[i][j];
                    uMatrix[posI+1][posJ]=block.getMatrix()[i][j];
                    uMatrix[posI][posJ+1]=block.getMatrix()[i][j];
                    uMatrix[posI+1][posJ+1]=block.getMatrix()[i][j];
                }
                else if (type.equals("v")){
                    vMatrix[posI][posJ]=block.getMatrix()[i][j];
                    vMatrix[posI+1][posJ]=block.getMatrix()[i][j];
                    vMatrix[posI][posJ+1]=block.getMatrix()[i][j];
                    vMatrix[posI+1][posJ+1]=block.getMatrix()[i][j];
                }
                posJ+=2;
            }
            posI+=2;
        }
    }

    private void decodeMatrixY(Block block) {
        int posI=block.getPosI();
        for(int i=0;i<8;i++){
            int posJ=block.getPosJ();
            for (int j=0;j<8;j++){
                this.yMatrix[posI][posJ] = block.getMatrix()[i][j];
                posJ+=1;
            }
            posI+=1;
    }
}
}
