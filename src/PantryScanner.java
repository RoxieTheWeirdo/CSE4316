import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PantryScanner {
    private static boolean showText = true;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pantry Scanning");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 700);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // ==== TOP: Webcam Panel ====
        Webcam webcam = Webcam.getDefault();
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        WebcamPanel webcamPanel = new WebcamPanel(webcam) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (showText) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                    g2.setColor(new Color(255, 255, 255, 180));
                    g2.drawString("Scanning Pantry...", 100, 150);
                    g2.dispose();
                }
            }
        };

        webcamPanel.setPreferredSize(new Dimension(400, 300));
        webcamPanel.setFPSDisplayed(false);

        Timer timer = new Timer(500, _ -> {
            showText = !showText;
            webcamPanel.repaint();
        });
        timer.start();

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        topPanel.add(webcamPanel);
        topPanel.setBackground(new Color(178, 102, 255));
        frame.add(topPanel, BorderLayout.NORTH);

        // ==== CENTER PANEL ====
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
        centerPanel.setBackground(new Color(178, 102, 255));

        // ==== Bubble Panel ====
        JPanel bubblePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 30, 30);

                // White bubble
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 30, 30);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        bubblePanel.setLayout(new BoxLayout(bubblePanel, BoxLayout.Y_AXIS));
        bubblePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bubblePanel.setOpaque(false);
        bubblePanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Meal label
        JLabel mealLabel = new JLabel("Suggested meal: Tuna Salad");
        mealLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        mealLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mealLabel.setPreferredSize(new Dimension(300, 40));
        mealLabel.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                super.paint(g, c);
                int x = 0;
                int y = c.getHeight() - 2;
                g.setColor(c.getForeground());
                g.drawLine(x, y, c.getWidth(), y);
            }
        });
        bubblePanel.add(mealLabel);
        bubblePanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Ingredients label
        JLabel ingredients = new JLabel("Ingredients:");
        ingredients.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        ingredients.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubblePanel.add(ingredients);
        bubblePanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Ingredient list
        List<String> items = List.of("Tuna", "Low-fat mayo", "Pickles", "Raisins");
        for (String item : items) {
            JLabel iLabel = new JLabel("           • " + item);
            iLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            iLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 5));
            iLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubblePanel.add(iLabel);
        }
        centerPanel.add(bubblePanel);

        // ==== Pill-shaped Button ====
        JButton button = new OvalButton("New Suggestion", 400, 80);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(_ -> {
            // No operation, just clickable
        });

        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(button);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        frame.add(centerPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    // ==== Oval Button Class ====
    static class OvalButton extends JButton {
        private final Dimension size;

        public OvalButton(String label, int width, int height) {
            super(label);
            this.size = new Dimension(width, height);
            setOpaque(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 24));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Dimension getPreferredSize() {
            return size;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isPressed()) {
                g2.setColor(Color.GRAY);
            } else {
                g2.setColor(Color.RED);
            }

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight()); // pill shape
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        public boolean contains(int x, int y) {
            Shape shape = new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            return shape.contains(x, y);
        }
    }
}