
import java.io.*;
import java.util.Vector;

public class Compression {

    static Vector<Integer> quantizedIndices;

    static boolean Compress(int vectorHeight, int vectorWidth, int codeBlockSize, String Path) throws IOException {
        int[][] image = Image.readImage(Path);

        int originalHeight = Image.height;
        int originalWidth = Image.width;

        //ensuring that they are multiples of vectorHeight and vectorWidth respectively.
        int scaledHeight = originalHeight % vectorHeight == 0 ? originalHeight : ((originalHeight / vectorHeight) + 1) * vectorHeight;
        int scaledWidth = originalWidth % vectorWidth == 0 ? originalWidth : ((originalWidth / vectorWidth) + 1) * vectorWidth;

        int[][] scaledImage = new int[scaledHeight][scaledWidth];
        for (int i = 0; i < scaledHeight; i++) {
            int x = i >= originalHeight ? originalHeight - 1 : i;
            for (int j = 0; j < scaledWidth; j++) {
                int y = j >= originalWidth ? originalWidth - 1 : j;
                scaledImage[i][j] = image[x][y];
            }
        }
     //to store the image vectors.
        Vector<Vector<Integer>> Vectors = new Vector<>();
        for (int i = 0; i < scaledHeight; i += vectorHeight) {
            for (int j = 0; j < scaledWidth; j += vectorWidth) {
                Vectors.add(new Vector<>());
                for (int x = i; x < i + vectorHeight; x++) {
                    for (int y = j; y < j + vectorWidth; y++) {
                        Vectors.lastElement().add(scaledImage[x][y]);
                    }
                }
            }
        }

        Vector<Vector<Integer>> Quantized = new Vector<>();
        Quantize(codeBlockSize, Vectors, Quantized);
        //corresponds to the index of the nearest quantized vector for the respective input vector in the Vectors vector.
        quantizedIndices = optimize(Vectors, Quantized);

        writeCompressedFile(Path, originalWidth, originalHeight, quantizedIndices, Quantized);

        return true;
    }



    static Vector<Integer> optimize(Vector<Vector<Integer>> Vectors, Vector<Vector<Integer>> Quantized) {
        Vector<Integer> QuantizedIndices = new Vector<>();

        for (Vector<Integer> vector : Vectors) {
            int smallestDistance = EuclidDistance(vector, Quantized.get(0));
            int smallestIndex = 0;

            for (int i = 1; i < Quantized.size(); i++) {
                int tempDistance = EuclidDistance(vector, Quantized.get(i));
                if (tempDistance < smallestDistance) {
                    smallestDistance = tempDistance;
                    smallestIndex = i;
                }
            }

            QuantizedIndices.add(smallestIndex);
        }
        return QuantizedIndices;
    }

    static String getCompressedPath(String path) {
        return path.substring(0, path.lastIndexOf('.')) + ".txt";
    }
    private static int EuclidDistance(Vector<Integer> x, Vector<Integer> y)
        {
        return EuclidDistance(x, y, 0);
    }
    private static int EuclidDistance(Vector<Integer> x, Vector<Integer> y, int incrementFactor) {
        int distance = 0;
        for (int i = 0; i < x.size(); i++)
            distance += Math.pow(x.get(i) - y.get(i) + incrementFactor, 2);
        return (int) Math.sqrt(distance);
    }
    //codebook
    private static void Quantize(int Level, Vector<Vector<Integer>> Vectors, Vector<Vector<Integer>> Quantized) {
        //checks if the current level of quantization is 1 or if there are no vectors to quantize.
        // If either condition is true, the method proceeds to quantize the vectors.
        if (Level == 1 || Vectors.size() == 0) {
            if (Vectors.size() > 0)
                Quantized.add(vectorAverage(Vectors));
            return;
        }

        Vector<Vector<Integer>> leftVectors = new Vector<>();
        Vector<Vector<Integer>> rightVectors = new Vector<>();

        Vector<Integer> avgvec = vectorAverage(Vectors);

        for (Vector<Integer> vec : Vectors) {
            int Dist1 = EuclidDistance(vec, avgvec, 1);
            int Dist2 = EuclidDistance(vec, avgvec, -1);

            if (Dist1 >= Dist2)
                leftVectors.add(vec);
            else

                rightVectors.add(vec);
        }
        //It then recursively calls itself for the left and right vectors with a halved quantization level.
        Quantize(Level / 2, leftVectors, Quantized);
        Quantize(Level / 2, rightVectors, Quantized);
    }

    private static Vector<Integer> vectorAverage(Vector<Vector<Integer>> Vectors) {
        int[] sum = new int[Vectors.get(0).size()];

        for (Vector<Integer> vector : Vectors)
            for (int i = 0; i < vector.size(); i++)
                sum[i] += vector.get(i);

        Vector<Integer> returnVector = new Vector<>();
        for (int i = 0; i < sum.length; i++)
            returnVector.add(sum[i] / Vectors.size());

        return returnVector;
    }

    static void writeCompressedFile(String filePath, int originalWidth, int originalHeight, Vector<Integer> vectorsIndices, Vector<Vector<Integer>> Quantized)
            throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(getCompressedPath(filePath));

        /* codeBookSize_CodeBook_originalHeight_originalWidth_padding_Indices */
        // IndicesSize = scaledHeight/2 * scaledWidth/2

        // Write CodeBookSize
        int codeBookSize = 16;
        fileOutputStream.write(codeBookSize);

        // Write CodeBook
        String codeBook = convertCodeBookToStream(codeBookSize, Quantized);
        for (int i = 0; i < codeBook.length(); i++){
            fileOutputStream.write(codeBook.charAt(i));
        }

        // Write original height in 2 bytes
        int firstHByteValue = Math.min(originalHeight, 255);
        int secondHByteValue = (originalHeight > 255) ? originalHeight - 255 : 0;
        fileOutputStream.write(firstHByteValue);
        fileOutputStream.write(secondHByteValue);

        // Write original height in 2 bytes
        int firstWByteValue = Math.min(originalWidth, 255);
        int secondWByteValue = (originalWidth > 255) ? originalWidth - 255 : 0;
        fileOutputStream.write(firstWByteValue);
        fileOutputStream.write(secondWByteValue);

        // Calculating Padding
        StringBuilder compressedImage = new StringBuilder(convertToStream(codeBookSize));
        int padding = 8 - (compressedImage.length() % 8);
        if (padding != 8) {
            compressedImage.append("0".repeat(padding));
        }

        // Writing Padding Size
        fileOutputStream.write(padding);

        // Writing QuantizedIndices
        for (int i = 0; i < compressedImage.length(); i += 8) {
            String streamByte = compressedImage.substring(i, i + 8);
            short value = Short.parseShort(streamByte, 2);
            fileOutputStream.write(value);
        }

        fileOutputStream.close();

    }

    // Converting the quantizedIndices vector into a stream of bits.
    static String convertToStream(int codeBookSize){
        StringBuilder compressedImage = new StringBuilder();
        // The no of bits for every index = log2(codeBookSize)
        int bitsNo = (int) (Math.log(codeBookSize) / Math.log(2));
        for (Integer quantizedIndex : quantizedIndices) {
            String bits = String.format("%" + bitsNo + "s", Integer.toBinaryString(quantizedIndex & 0xFF)).replace(' ', '0');
            compressedImage.append(bits);
        }
        return String.valueOf(compressedImage);
    }

    static String convertCodeBookToStream(int codeBookSize, Vector<Vector<Integer>> quantized){
        // codeBook is a vector of vectors (each vector has 4 values its max value = 255)
        StringBuilder codeBookStream = new StringBuilder();
        for (int i = 0; i < codeBookSize; i++){
            for (int j = 0; j < 4; j++){
                codeBookStream.append(Character.toChars(quantized.get(i).get(j)));
            }
        }
        return String.valueOf(codeBookStream);
    }
}
