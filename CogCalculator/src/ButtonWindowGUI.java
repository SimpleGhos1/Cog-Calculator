import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;

public class ButtonWindowGUI {

    private JComboBox<Integer> levelSelector;
    private List<CogInfo> cogs;
    private CogInfo selectedCog;
    private JLabel cogImageLabel;

    private JLabel nameLabel, cogTypeLabel, levelLabel, hpLabel, meritLabel;
    private JPanel attackPanel;

    public ButtonWindowGUI() {
        JFrame frame = new JFrame("Cog Gallery");
        frame.setSize(1081, 620);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setResizable(false);

        // Window icon
        ImageIcon icon = new ImageIcon("src/resources/images/window.png");
        frame.setIconImage(icon.getImage());

        // Background
        File bgFile = new File("src/resources/images/cog_gallery.png");
        ImageIcon bgIcon = new ImageIcon(bgFile.getPath());
        JLabel bgLabel = new JLabel(bgIcon);
        bgLabel.setBounds(0, 0, bgIcon.getIconWidth(), bgIcon.getIconHeight());
        frame.add(bgLabel);

        // Info panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBounds(782, 0, 280, 580);
        infoPanel.setBackground(new Color(200, 200, 200));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Level selector
        levelSelector = new JComboBox<>();
        levelSelector.addActionListener(e -> updateInfo());
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1; gbc.weighty = 0;
        infoPanel.add(levelSelector, gbc);

        // Name panel
        JPanel namePanel = new JPanel();
        namePanel.setBackground(new Color(220, 220, 220));
        nameLabel = new JLabel();
        nameLabel.setHorizontalAlignment(JLabel.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        namePanel.add(nameLabel);
        gbc.gridy = 1; gbc.gridwidth = 2;
        infoPanel.add(namePanel, gbc);

        // Stats grid using nested BoxLayouts
        JPanel statsGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        statsGrid.setOpaque(false);

        levelLabel = addStatBox(statsGrid, "Level");
        hpLabel = addStatBox(statsGrid, "HP");
        cogTypeLabel = addStatBox(statsGrid, "Cog Type");
        meritLabel = addStatBox(statsGrid, "Suit XP");

        gbc.gridy = 2; gbc.gridwidth = 2; gbc.weighty = 0;
        infoPanel.add(statsGrid, gbc);

        // Attacks panel
        attackPanel = new JPanel();
        attackPanel.setLayout(new BoxLayout(attackPanel, BoxLayout.Y_AXIS));
        attackPanel.setBorder(BorderFactory.createTitledBorder("Attacks"));
        JScrollPane attackScroll = new JScrollPane(attackPanel);
        gbc.gridy = 3; gbc.weighty = 0.4;
        infoPanel.add(attackScroll, gbc);

        // Cog image (stays in same spot)
        cogImageLabel = new JLabel();
        cogImageLabel.setHorizontalAlignment(JLabel.CENTER);
        cogImageLabel.setPreferredSize(new Dimension(260, 260));
        gbc.gridy = 4; gbc.weighty = 0.4; gbc.insets = new Insets(10, 0, 0, 0);
        infoPanel.add(cogImageLabel, gbc);

        frame.add(infoPanel);

        // Load cogs
        cogs = CogParser.loadCogs("src/resources/cogs.json");

        // Buttons for cogs
        for (CogInfo cog : cogs) {
            JButton button = new JButton();
            button.setBounds(cog.x, cog.y, cog.width, cog.height);
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorder(BorderFactory.createLineBorder(Color.RED));
            button.addActionListener(e -> {
                selectedCog = cog;
                populateLevelSelector(cog);
                updateInfo();
            });
            bgLabel.add(button);
        }

        frame.setVisible(true);
    }

    // Helper: create a vertical stat box (name on top, value below)
    private JLabel addStatBox(JPanel parent, String title) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel valueLabel = new JLabel();
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        box.add(titleLabel);
        box.add(Box.createVerticalStrut(5)); // small gap
        box.add(valueLabel);

        parent.add(box);
        return valueLabel;
    }

    private void populateLevelSelector(CogInfo cog) {
        levelSelector.removeAllItems();
        for (int lvl : cog.levels) levelSelector.addItem(lvl);
        if (levelSelector.getItemCount() > 0) levelSelector.setSelectedIndex(0);
    }

    private void updateInfo() {
        if (selectedCog == null || levelSelector.getSelectedItem() == null) return;

        int level = (int) levelSelector.getSelectedItem();
        int hp = (level <= 11) ? (level + 1) * (level + 2) : 200;
        int merit = level;

        nameLabel.setText(selectedCog.name);
        levelLabel.setText(String.valueOf(level));
        hpLabel.setText(String.valueOf(hp));
        cogTypeLabel.setText(selectedCog.cogType);

        String meritText = switch (selectedCog.cogType) {
            case "Bossbot" -> "Stock Options: " + merit;
            case "Lawbot" -> "Jury Notices: " + merit;
            case "Cashbot" -> "Cogbucks: " + merit;
            case "Sellbot" -> "Merits: " + merit;
            default -> String.valueOf(merit);
        };
        meritLabel.setText(meritText);

        attackPanel.removeAll();
        for (Map.Entry<String, Map<String, String>> attackEntry : selectedCog.attacks.entrySet()) {
            String attackName = attackEntry.getKey();
            String damage = attackEntry.getValue().getOrDefault(String.valueOf(level), "N/A");
            JLabel attackLabel = new JLabel(attackName + " - " + damage + " dmg");
            attackLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
            attackPanel.add(attackLabel);
        }
        attackPanel.revalidate();
        attackPanel.repaint();

        updateCogImage(selectedCog);
    }

    private void updateCogImage(CogInfo cog) {
        try {
            String path = "src/resources/images/" + cog.image;
            ImageIcon icon = new ImageIcon(path);
            Image scaled = icon.getImage().getScaledInstance(260, 260, Image.SCALE_SMOOTH);
            cogImageLabel.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            cogImageLabel.setIcon(null);
        }
    }
}