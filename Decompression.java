import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

public class Decompression {
    static String InputFile;
    static int codeBookSize;
    static int originalHeight;
    static int originalWidth;
    static int padding;
    static Vector<Vector<Integer>> Quantized = new Vector<>();
    static Vector<Integer> quantizedIndices = new Vector<>();

    static void readInputFile(String inputFile){
        InputFile = inputFile;
        try (FileInputStream fileInputStream = new FileInputStream(inputFile)){
            /* codeBookSize_CodeBook_originalHeight_originalWidth_padding_Indices */

            // Read Code Book Size
            codeBookSize = fileInputStream.read();
            
            // Read Code Book
            readCodeBook(fileInputStream);
            
            // Read Height
            readHeight(fileInputStream);
            
            // Read Width
            readWidth(fileInputStream);

            // Read quantizedIndices and Padding
            readIndices(fileInputStream);

        } catch (IOException e){
            System.out.println("An Error Occurred while reading the file.");
        }
    }

    private static void readCodeBook(FileInputStream file) throws IOException {
        for (int i = 0; i < codeBookSize; i++){
            Quantized.add(new Vector<>());
            for (int j = 0; j < 4; j++) {
                Quantized.lastElement().add(file.read());
            }
        }
    }
    
    private static void readHeight(FileInputStream file) throws IOException {
        int firstByte = file.read();
        int secondByte = file.read();
        originalHeight = firstByte + secondByte;
    }

    private static void readWidth(FileInputStream file) throws IOException {
        int firstByte = file.read();
        int secondByte = file.read();
        originalWidth = firstByte + secondByte;
    }
    
    private static void readIndices(FileInputStream file) throws IOException {
        int bitsNo = (int) (Math.log(codeBookSize) / Math.log(2));
        StringBuilder indicesBytes = new StringBuilder();
        int bytesRead;
        byte[] buffer = new byte[1024];

        padding = file.read();
        while ((bytesRead = file.read(buffer)) != -1) {
            for (int i = 0; i < bytesRead; i++) {
                // Convert each byte to its binary representation and append to the compressed text
                String bits = String.format("%8s", Integer.toBinaryString(buffer[i] & 0xFF)).replace(' ', '0');
                indicesBytes.append(bits);
            }
        }

        // Remove the padding bits from the end of the compressed text
        if (padding != 8){
            indicesBytes.setLength(indicesBytes.length() - padding);
        }

        for (int i = 0; i < indicesBytes.length(); i += bitsNo) {
            String streamByte = indicesBytes.substring(i, i + bitsNo);
            quantizedIndices.add(Integer.parseInt(streamByte, 2));
        }
    }

    static boolean decompress(String inputFile){
        readInputFile(inputFile);
        int vectorHeight = 2, vectorWidth = 2;
        int scaledHeight = originalHeight % vectorHeight == 0 ? originalHeight : ((originalHeight / vectorHeight) + 1) * vectorHeight;
        int scaledWidth = originalWidth % vectorWidth == 0 ? originalWidth : ((originalWidth / vectorWidth) + 1) * vectorWidth;

        int[][] newImg = new int[scaledHeight][scaledWidth];

        for (int i = 0; i < quantizedIndices.size(); i++) {
            int x = i / (scaledWidth / vectorWidth);
            int y = i % (scaledWidth / vectorWidth);
            x *= vectorHeight;
            y *= vectorWidth;
            int v = 0;
            for (int j = x; j < x + vectorHeight; j++) {
                for (int k = y; k < y + vectorWidth; k++) {
                    newImg[j][k] = Quantized.get(quantizedIndices.get(i)).get(v++);
                }
            }
        }
        Image.writeImage(newImg, originalWidth, originalHeight, compressedPath(InputFile));
        return true;
    }

    static String compressedPath(String path) {
        return path.substring(0, path.lastIndexOf('.')) + "_Compressed.jpg";
    }

}
