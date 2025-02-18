package Markdown;

import javax.swing.*;
import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import java.io.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class markdownNote {
    private JFrame frame;
    private JTextArea textArea;
    private JPanel panel;
    private JButton exportTxtButton;
    private JButton exportPdfButton;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new markdownNote().createUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void createUI() {
        frame = new JFrame("Markdown Note Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Create a split layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Text Area for input
        textArea = new JTextArea();
        textArea.setText("Write your markdown here...");
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                renderMarkdown();
            }
        });

        // Panel for rendered HTML output
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Buttons for exporting
        exportTxtButton = new JButton("Export as .txt");
        exportTxtButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToFile("txt");
            }
        });

        exportPdfButton = new JButton("Export as .pdf");
        exportPdfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToFile("pdf");
            }
        });

        JPanel topPanel = new JPanel();
        topPanel.add(exportTxtButton);
        topPanel.add(exportPdfButton);

        frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        splitPane.setLeftComponent(new JScrollPane(textArea));
        splitPane.setRightComponent(new JScrollPane(panel));

        frame.getContentPane().add(splitPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    public void renderMarkdown() {
        String markdownText = textArea.getText();

        // Parse markdown using CommonMark
        Parser parser = Parser.builder().build();
        org.commonmark.node.Node document = parser.parse(markdownText);

        // Convert markdown to HTML
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String htmlContent = renderer.render(document);

        // Display HTML in the panel (You can also use JEditorPane)
        panel.removeAll();
        JEditorPane editorPane = new JEditorPane("text/html", htmlContent);
        editorPane.setEditable(false);
        panel.add(new JScrollPane(editorPane), BorderLayout.CENTER);

        panel.revalidate();
        panel.repaint();
    }

    private void exportToFile(String format) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as " + format);
        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (format.equals("txt")) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave + ".txt"))) {
                    writer.write(textArea.getText());
                    JOptionPane.showMessageDialog(frame, "File saved successfully!");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(frame, "Error saving file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (format.equals("pdf")) {
                // Parse markdown using CommonMark
                Parser parser = Parser.builder().build();
                org.commonmark.node.Node document = parser.parse(textArea.getText());

                // Convert markdown to HTML
                HtmlRenderer renderer = HtmlRenderer.builder().build();
                String htmlContent = renderer.render(document);

                // Convert HTML to PDF using XMLWorkerHelper
                Document pdfDocument = new Document();
                try {
                    PdfWriter writer = PdfWriter.getInstance(pdfDocument, new FileOutputStream(fileToSave + ".pdf"));
                    pdfDocument.open();
                    XMLWorkerHelper.getInstance().parseXHtml(writer, pdfDocument, new StringReader(htmlContent));
                    pdfDocument.close();
                    JOptionPane.showMessageDialog(frame, "PDF saved successfully!");
                } catch (DocumentException | IOException e) {
                    JOptionPane.showMessageDialog(frame, "Error saving PDF.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
