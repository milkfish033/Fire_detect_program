import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class FireWatchClient extends JFrame {
    private JLabel videoLabel;

    public FireWatchClient() {
        setTitle("FireWatch Monitoring System");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        videoLabel = new JLabel();
        add(videoLabel);
        setVisible(true);

        // Start the video stream simulation (for real RTSP, integrate FFmpeg or GStreamer)
        new Timer(1000, e -> {
            try {
                BufferedImage image = ImageIO.read(new URL("http://localhost:5000/stream"));
                videoLabel.setIcon(new ImageIcon(image));

                // Call detection
                boolean fireDetected = callDetectionAPI(image);
                if (fireDetected) {
                    sendAlertEmail();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private boolean callDetectionAPI(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

        URL url = new URL("http://localhost:5000/predict");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String json = "{\"image\":\"" + base64Image + "\"}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        try (InputStream is = conn.getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            return line.contains("fire": true);
        }
    }

    private void sendAlertEmail() {
        // Set email credentials
        final String from = "your-email@gmail.com";
        final String password = "your-password";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("alert@recipient.com"));
            message.setSubject("Fire Detected!");
            message.setText("A fire has been detected. Immediate attention required.");

            Transport.send(message);
            System.out.println("Alert email sent.");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FireWatchClient::new);
    }
}
