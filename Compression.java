
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

        write_compressedFile(Path, originalWidth, originalHeight, scaledWidth, scaledHeight,
                vectorWidth, vectorHeight, quantizedIndices, Quantized);
        writeCompressedFile(Path, originalWidth, originalHeight, scaledWidth, scaledHeight,
                vectorWidth, vectorHeight, quantizedIndices, Quantized);
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
        return path.substring(0, path.lastIndexOf('.')) + ".VQ";
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
    static void write_compressedFile(String compressedFilePath, int width, int height, int scaledWidth,
                                      int scaledHeight, int vectorWidth, int vectorHeight,
                                      Vector<Integer> QIndices, Vector<Vector<Integer>> Quantized)
            throws IOException {
        int[][] newImg = new int[scaledHeight][scaledWidth];

        for (int i = 0; i < QIndices.size(); i++) {
            int x = i / (scaledWidth / vectorWidth);
            int y = i % (scaledWidth / vectorWidth);
            x *= vectorHeight;
            y *= vectorWidth;
            int v = 0;
            for (int j = x; j < x + vectorHeight; j++) {
                for (int k = y; k < y + vectorWidth; k++) {
                    newImg[j][k] = Quantized.get(QIndices.get(i)).get(v++);
                }
            }
        }

        Image.writeImage(newImg, width, height, compressedPath(compressedFilePath));
    }
    static void writeCompressedFile(String filePath, int originalWidth, int originalHeight, int scaledWidth,
                                    int scaledHeight, int vectorWidth, int vectorHeight, Vector<Integer> vectorsIndices, Vector<Vector<Integer>> Quantized)
            throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(getCompressedPath(filePath));
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

        objectOutputStream.writeObject(originalWidth);
        objectOutputStream.writeObject(originalHeight);
        objectOutputStream.writeObject(scaledWidth);
        objectOutputStream.writeObject(scaledHeight);
        objectOutputStream.writeObject(vectorWidth);
        objectOutputStream.writeObject(vectorHeight);
        objectOutputStream.writeObject(vectorsIndices);
        objectOutputStream.writeObject(Quantized);
        objectOutputStream.close();

    }
    static String compressedPath(String path) {
        return path.substring(0, path.lastIndexOf('.')) + "_Compressed.jpg";
    }
}
