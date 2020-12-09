public class Main {

    public static void main(String[] args) {

        Encoder encoder = new Encoder("nt-P3.ppm");
        encoder.divideMatrices();

        encoder.FDCTandQuantization();

        Decoder decoder = new Decoder(encoder.getFormat(), encoder.getWidth(), encoder.getHeight(),
                encoder.getMaxValue(), encoder.getYuvBlocksList());

        decoder.inverseDCTandDeQuantization();
        decoder.decode("name2.ppm");

    }
}