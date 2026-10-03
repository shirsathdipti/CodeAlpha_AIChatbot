import javax.swing.*;
import java.awt.*;

/**
 * Swing chat window for JavaBot.
 * All the thinking is done by AIChatbot.reply(); this class only handles the window.
 */
public class AIChatbotGUI extends JFrame {

    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton("Send");

    public AIChatbotGUI() {
        super("JavaBot - AI Chatbot");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(520, 600);
        setLocationRelativeTo(null);          // centre on screen
        setLayout(new BorderLayout());

        // ----- title bar at the top -----
        JLabel title = new JLabel("JavaBot - Ask me about Java, OOP, AI or CodeAlpha", SwingConstants.CENTER);
        title.setOpaque(true);
        title.setBackground(new Color(30, 60, 114));
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        add(title, BorderLayout.NORTH);

        // ----- chat history in the middle -----
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatArea.setMargin(new Insets(10, 10, 10, 10));
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // ----- input row at the bottom -----
        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // clicking Send or pressing Enter does the same thing
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        addMessage("JavaBot", "Hi! I'm JavaBot. Type 'help' to see what I can do, or 'bye' to quit.");
    }

    // adds one line to the chat and scrolls to the bottom
    private void addMessage(String who, String text) {
        chatArea.append(who + ": " + text + "\n\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        addMessage("You", text);
        inputField.setText("");

        if (AIChatbot.isGoodbye(text)) {
            addMessage("JavaBot", "Goodbye!");
            inputField.setEnabled(false);
            sendButton.setEnabled(false);
            Timer closeTimer = new Timer(1200, e -> dispose());   // close window after 1.2 seconds
            closeTimer.setRepeats(false);
            closeTimer.start();
            return;
        }

        addMessage("JavaBot", AIChatbot.reply(text));    // the bot's brain from Step 3
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AIChatbotGUI window = new AIChatbotGUI();
            window.setVisible(true);
            window.inputField.requestFocusInWindow();
        });
    }
}