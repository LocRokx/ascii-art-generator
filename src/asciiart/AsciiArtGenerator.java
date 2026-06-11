package asciiart;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * ASCII Art Generator — Swing desktop app.
 *
 * Select a JPG/JPEG/PNG image, convert it to ASCII art, and save the result
 * as a .txt file (opened automatically in the system text editor when
 * supported). View the output in a monospace font, e.g. Courier New at a
 * small size.
 */
public class AsciiArtGenerator {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back to the default look and feel.
        }
        SwingUtilities.invokeLater(MainFrame::new);
    }
}

class MainFrame implements ActionListener {

    private final JFrame frame = new JFrame();
    private final JLabel imagePreview = new JLabel();
    private JButton selectButton;
    private JButton convertButton;
    private JButton resetButton;
    private JButton exitButton;

    private File sourceFile;

    MainFrame() {
        frame.setTitle("ASCII Art Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 850);
        frame.getContentPane().setBackground(new Color(14, 88, 201));
        buildLabels();
        buildButtons();
        frame.setResizable(false);
        frame.setLayout(null);
        frame.setVisible(true);
    }

    private void buildLabels() {
        JLabel title = new JLabel("ASCII ART GENERATOR");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 0, 850, 50);
        title.setHorizontalAlignment(JLabel.CENTER);

        JLabel credits = new JLabel("Created By : LOCHAN S R");
        credits.setHorizontalAlignment(JLabel.CENTER);
        credits.setForeground(Color.WHITE);
        credits.setBounds(0, 50, 850, 20);

        JLabel note = new JLabel("NOTE : View the output in a monospace font (e.g. Courier New, size 8)");
        note.setBounds(175, 650, 500, 20);
        note.setHorizontalAlignment(JLabel.CENTER);
        note.setForeground(Color.WHITE);

        imagePreview.setBounds(175, 150, 500, 500);
        imagePreview.setOpaque(true);
        imagePreview.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        frame.add(title);
        frame.add(credits);
        frame.add(note);
        frame.add(imagePreview);
    }

    private void buildButtons() {
        selectButton = makeButton("SELECT IMAGE", 50);
        convertButton = makeButton("CONVERT", 250);
        resetButton = makeButton("RESET", 450);
        exitButton = makeButton("EXIT", 650);

        convertButton.setEnabled(false);
        resetButton.setEnabled(false);
    }

    private JButton makeButton(String text, int x) {
        JButton button = new JButton(text);
        button.setBounds(x, 700, 150, 60);
        button.addActionListener(this);
        frame.add(button);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == exitButton) {
            int choice = JOptionPane.showConfirmDialog(frame,
                    "Are You Sure You Want to Exit ?", "EXIT", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        } else if (event.getSource() == selectButton) {
            selectImage();
        } else if (event.getSource() == convertButton) {
            convertImage();
        } else if (event.getSource() == resetButton) {
            reset();
        }
    }

    private void selectImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Images (.jpg, .jpeg, .png)", "jpg", "jpeg", "png"));
        chooser.setDialogTitle("Select Image");

        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        sourceFile = chooser.getSelectedFile();

        ImageIcon sourceIcon = new ImageIcon(sourceFile.getAbsolutePath());
        Image scaled = sourceIcon.getImage().getScaledInstance(
                imagePreview.getWidth(), imagePreview.getHeight(), Image.SCALE_SMOOTH);
        imagePreview.setIcon(new ImageIcon(scaled));

        convertButton.setEnabled(true);
        resetButton.setEnabled(true);
        JOptionPane.showMessageDialog(frame, "Image Imported Successfully",
                "MESSAGE", JOptionPane.INFORMATION_MESSAGE);
    }

    private void convertImage() {
        BufferedImage image;
        try {
            image = ImageIO.read(sourceFile);
        } catch (IOException e) {
            image = null;
        }
        if (image == null) {
            JOptionPane.showMessageDialog(frame,
                    sourceFile + " is not a valid image.", "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text file (.txt)", "txt"));
        chooser.setDialogTitle("Select path to save file");
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String outputPath = chooser.getSelectedFile().getAbsolutePath();
        if (!outputPath.toLowerCase().endsWith(".txt")) {
            outputPath += ".txt";
        }

        String ascii = new AsciiConverter().convert(image);
        try {
            Files.writeString(Path.of(outputPath), ascii);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame,
                    "Could not write file: " + e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }

        convertButton.setEnabled(false);
        openInEditor(new File(outputPath));
    }

    private void openInEditor(File file) {
        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(frame, "Saved to " + file.getAbsolutePath(),
                    "MESSAGE", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Saved to " + file.getAbsolutePath(),
                    "MESSAGE", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void reset() {
        sourceFile = null;
        imagePreview.setIcon(null);
        convertButton.setEnabled(false);
        resetButton.setEnabled(false);
        JOptionPane.showMessageDialog(frame, "Process Reset Successfully",
                "MESSAGE", JOptionPane.INFORMATION_MESSAGE);
    }
}
