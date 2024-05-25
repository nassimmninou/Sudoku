package com.example;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

public class SudokuApp extends JFrame {
    private static final int SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    private JTextField[][] cells = new JTextField[SIZE][SIZE];
    private Color userInputColor = new Color(255, 223, 186);  // Light orange
    private Color solverInputColor = new Color(255, 255, 186);  // Light yellow
    private Color subgridColor1 = new Color(186, 255, 201);  // Light green
    private Color subgridColor2 = new Color(186, 228, 255);  // Light blue
    private Color buttonColor = new Color(186, 186, 255);  // Light purple
    private Color backgroundColor = new Color(240, 240, 240);  // Light grey

    public SudokuApp() {
        setTitle("Sudoku Solver");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Résolution de Sudoku", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel subtitleLabel = new JLabel("Écris les chiffres initiaux et cliquez sur Résoudre", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 20));
        subtitleLabel.setBorder(new EmptyBorder(0, 10, 20, 10));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(backgroundColor);
        topPanel.add(titleLabel);
        topPanel.add(subtitleLabel);

        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE, 10, 10)) {
            @Override
            public Insets getInsets() {
                return new Insets(10, 10, 10, 10);
            }
        };
        gridPanel.setBackground(backgroundColor);
        gridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                cells[row][col] = new JTextField();
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                cells[row][col].setFont(new Font("Arial", Font.BOLD, 20));
                cells[row][col].setBorder(new RoundedBorder(10));
                cells[row][col].setOpaque(false);
                cells[row][col].setBackground(new Color(0, 0, 0, 0));
                cells[row][col].setForeground(Color.BLACK);
                cells[row][col].setMargin(new Insets(5, 5, 5, 5));
                cells[row][col].setUI(new RoundedTextFieldUI(10));
                cells[row][col].addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusLost(java.awt.event.FocusEvent evt) {
                        JTextField source = (JTextField) evt.getSource();
                        if (!source.getText().isEmpty()) {
                            source.setBackground(userInputColor);
                        }
                    }
                });
                if ((row / SUBGRID_SIZE + col / SUBGRID_SIZE) % 2 == 0) {
                    cells[row][col].setBackground(subgridColor1);
                } else {
                    cells[row][col].setBackground(subgridColor2);
                }
                gridPanel.add(cells[row][col]);
            }
        }

        JButton solveButton = new JButton("Résoudre");
        styleButton(solveButton);

        JButton resetButton = new JButton("Réinitialiser");
        styleButton(resetButton);

        solveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                solveSudoku();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetGrid();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(solveButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(resetButton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.setBackground(backgroundColor);

        add(topPanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setBackground(buttonColor);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setFocusPainted(false);
        button.setBorder(new RoundedBorder(10));
        button.setPreferredSize(new Dimension(150, 50));
    }

    private void solveSudoku() {
        int[][] sudoku = new int[SIZE][SIZE];

        // Read values from the UI grid
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                String text = cells[row][col].getText();
                if (!text.isEmpty()) {
                    sudoku[row][col] = Integer.parseInt(text);
                } else {
                    sudoku[row][col] = 0;
                }
            }
        }

        // Check if the grid is valid before solving
        if (!isValidSudoku(sudoku)) {
            JOptionPane.showMessageDialog(this, "The grid is invalid. Please check your input.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Solve the Sudoku puzzle
        SudokuSolver solver = new SudokuSolver();
        if (solver.solve(sudoku)) {
            new Thread(() -> animateSolution(sudoku)).start();
        } else {
            JOptionPane.showMessageDialog(this, "Unable to solve the Sudoku with this configuration.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void animateSolution(int[][] solvedGrid) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                int finalRow = row;
                int finalCol = col;
                SwingUtilities.invokeLater(() -> {
                    if (cells[finalRow][finalCol].getBackground() != userInputColor) {
                        cells[finalRow][finalCol].setText(String.valueOf(solvedGrid[finalRow][finalCol]));
                        cells[finalRow][finalCol].setBackground(solverInputColor);
                    }
                });
                try {
                    Thread.sleep(50);  // Adjust the speed of the animation
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void resetGrid() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                cells[row][col].setText("");
                cells[row][col].setBackground((row / SUBGRID_SIZE + col / SUBGRID_SIZE) % 2 == 0 ? subgridColor1 : subgridColor2);
            }
        }
    }

    private boolean isValidSudoku(int[][] board) {
        for (int i = 0; i < SIZE; i++) {
            Set<Integer> rowSet = new HashSet<>();
            Set<Integer> colSet = new HashSet<>();
            for (int j = 0; j < SIZE; j++) {
                if ((board[i][j] != 0 && !rowSet.add(board[i][j])) || (board[j][i] != 0 && !colSet.add(board[j][i]))) {
                    return false;
                }
            }
        }

        for (int i = 0; i < SIZE; i += SUBGRID_SIZE) {
            for (int j = 0; j < SIZE; j += SUBGRID_SIZE) {
                Set<Integer> subgridSet = new HashSet<>();
                for (int k = i; k < i + SUBGRID_SIZE; k++) {
                    for (int l = j; l < j + SUBGRID_SIZE; l++) {
                        if (board[k][l] != 0 && !subgridSet.add(board[k][l])) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SudokuApp().setVisible(true));
    }
}

class RoundedBorder implements Border {
    private int radius;

    RoundedBorder(int radius) {
        this.radius = radius;
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(this.radius + 1, this.radius + 1, this.radius + 2, this.radius);
    }

    public boolean isBorderOpaque() {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.setColor(c.getBackground().darker());
        g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        g.setColor(new Color(0, 0, 0, 50));
        g.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);
    }
}

class RoundedTextFieldUI extends javax.swing.plaf.basic.BasicTextFieldUI {
    private int radius;

    RoundedTextFieldUI(int radius) {
        this.radius = radius;
    }

    @Override
    protected void paintSafely(Graphics g) {
        JTextComponent component = getComponent();
        if (component.isOpaque()) {
            g.setColor(component.getBackground());
            g.fillRoundRect(0, 0, component.getWidth(), component.getHeight(), radius, radius);
        }
        super.paintSafely(g);
    }

    @Override
    protected void paintBackground(Graphics g) {
        // Do nothing, handled in paintSafely
    }
}
