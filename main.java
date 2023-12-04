
import java.io.IOException;
public class main {

    public static void main(String[] args) throws IOException {
//        try {
        // Compression parameters
        int vectorHeight = 2;
        int vectorWidth = 2;
        int codeBlockSize = 64;

        // Input image path
        String imagePath = "R.jpg";

        // Compress the image
        boolean compressionResult = Compression.Compress(vectorHeight, vectorWidth, codeBlockSize, imagePath);
        if (compressionResult) {
            System.out.println("Compression successful.");

//                // Decompress the compressed file
//                boolean decompressionResult = Decompression.Decompress(Compression.getCompressedPath(imagePath));
//                if (decompressionResult) {
//                    System.out.println("Decompression successful.");
//                } else {
//                    System.out.println("Decompression failed.");
//                }
//            } else {
//                System.out.println("Compression failed.");
//            }
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
        }
    }
}
