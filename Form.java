import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Form extends JFrame {
    private JTextField inputFilePathField;
    private JTextField outputFilePathField;
    private JButton compressButton;
    private JButton decompressButton;

    public Form() {
        initializeComponents();
        createLayout();
        pack();
        setSize(400, 400);
        setTitle("Vector Quantization Compression-Decompression Program");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeComponents() {
        inputFilePathField = new JTextField(10);
        outputFilePathField = new JTextField(10);

        compressButton = new JButton("Compress");
        decompressButton = new JButton("Decompress");

        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    compress();
                } catch (IOException ex) {
                    showMessage("Something Wrong Happened.");
                }
                inputFilePathField.setText("");
                outputFilePathField.setText("");
            }
        });
        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                decompress();
                inputFilePathField.setText("");
                outputFilePathField.setText("");
            }
        });
    }

    private void createLayout() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 10, 10, 10);

        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(new JLabel("Input File Path: "), constraints);

        constraints.gridx = 1;
        panel.add(inputFilePathField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(new JLabel("Output File Path: "), constraints);

        constraints.gridx = 1;
        panel.add(outputFilePathField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(compressButton, constraints);

        constraints.gridx = 1;
        panel.add(decompressButton, constraints);

        setContentPane(panel);
    }

    private void compress() throws IOException {
        // Compression parameters
        int vectorHeight = 2;
        int vectorWidth = 2;
        int codeBlockSize = 16;

        // Input image path
        String imagePath = "R.jpg";

        boolean compressionResult = Compression.Compress(vectorHeight, vectorWidth, codeBlockSize, imagePath);
        if (compressionResult) {
            showMessage("Compression successful.");
        } else {
            showMessage("An error occurred.");
        }
    }

    private void decompress() {
        String imagePath = "R.jpg";

        boolean decompressionResult = Decompression.decompress(Compression.getCompressedPath(imagePath));
        if (decompressionResult) {
            showMessage("Decompression successful.");
        } else {
            showMessage("Decompression failed.");
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
