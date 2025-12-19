package gui;

import bridge.DroolsBridge;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.List;

public class MainFrame extends JFrame {

    private final DroolsBridge bridge;
    private String selectedAnswer = null;

    public MainFrame() {
        this.bridge = new DroolsBridge();
        setTitle("System rekomendacji gier");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        render();
        setVisible(true);
    }

    private void render() {
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());

        
        Object recommendation = bridge.getRecommendation();
        if (recommendation != null) {
            try {
                Method getTitle = recommendation.getClass().getMethod("getTitle");
                String title = (String) getTitle.invoke(recommendation);

                JLabel result = new JLabel(
                        "<html><h2>Rekomendowana gra:</h2><h1>" + title + "</h1></html>",
                        SwingConstants.CENTER
                );
                add(result, BorderLayout.CENTER);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            revalidate();
            repaint();
            return;
        }


        Object guiState = bridge.getGUIState();
        if (guiState == null) {
            repaint();
            return;
        }

        try {
            Method getText = guiState.getClass().getMethod("getText");
            Method getOptions = guiState.getClass().getMethod("getOptions");

            String text = (String) getText.invoke(guiState);
            List<?> options = (List<?>) getOptions.invoke(guiState);

            JLabel label = new JLabel(text);
            add(label, BorderLayout.NORTH);

            JPanel center = new JPanel();
            ButtonGroup group = new ButtonGroup();
            selectedAnswer = null;

            for (Object opt : options) {
                String value = opt.toString();
                JRadioButton btn = new JRadioButton(value);
                btn.addActionListener(e -> selectedAnswer = value);
                group.add(btn);
                center.add(btn);
            }

            add(center, BorderLayout.CENTER);

            JButton nextButton = new JButton("DALEJ");
            nextButton.addActionListener(e -> {
                if (selectedAnswer != null) {
                    bridge.answer(selectedAnswer);
                    render();
                } else {
                    JOptionPane.showMessageDialog(this, "Wybierz odpowiedź");
                }
            });

            add(nextButton, BorderLayout.SOUTH);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
