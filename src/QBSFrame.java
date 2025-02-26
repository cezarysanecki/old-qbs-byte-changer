import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class QBSFrame extends JFrame {
    private static final int WIDTH = 385;
    private static final int HEIGHT = 300;
    private static int WIDTH_SCREEN = Toolkit.getDefaultToolkit().getScreenSize().width;
    private static int HEIGHT_SCREEN = Toolkit.getDefaultToolkit().getScreenSize().height;

    private JTextField directory;
    private JTextField fileExtension;
    private JFileChooser fileChooser;
    private LinkedList<File> files;
    private String mainPath;
    private List<Byte> toReplaceList;
    private List<Byte> replacementList;
    private Pattern patternTyped;
    private Pattern patternExtension;

    public QBSFrame() {
        // Podstatawowe informacje o ramce
        setTitle("ByteChanger");
        setBounds((WIDTH_SCREEN - WIDTH) / 2, (HEIGHT_SCREEN - HEIGHT) / 2, WIDTH, HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Utworzenie kolekcji
        files = new LinkedList<>();
        toReplaceList = new ArrayList<>();
        replacementList = new ArrayList<>();

        // Wzór wyrażenia regularnego
        patternTyped = Pattern.compile("[0-9a-fA-F][0-9a-fA-F]");
        patternExtension = Pattern.compile("[0-9a-zA-Z]+");

        // Przeglądarka plików
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.addActionListener(e -> {
            if(e.getActionCommand().equals("ApproveSelection")) {
                mainPath = ((JFileChooser)e.getSource()).getSelectedFile().getPath();
                directory.setText(mainPath);
            }
        });

        // Podpisy do pól w GUI
        JLabel directoryLabel = new JLabel("Ścieżka katalogu: ");
        JLabel fileExtensionLabel = new JLabel("Rozszerzenie pliku (bez kropki np. \"jpg\"): ");
        JLabel firstGroupOfBytesLabel = new JLabel("Szukane bajty (format - '[0-9a-fA-F][0-9a-fA-F]* '): ");
        JLabel secondGroupOfBytesLabel = new JLabel("Bajty do zamiany (format - '[0-9a-fA-F][0-9a-fA-F]* '): ");

        // Podstawowe elementy GUI
        JButton chooser = new JButton("Wybierz");
        chooser.addActionListener(e -> fileChooser.showOpenDialog(rootPane));
        directory = new JTextField(25);
        directory.setEditable(false);
        fileExtension = new JTextField(25);
        JTextField firstGroupOfBytes = new JTextField(25);
        JTextField secondGroupOfBytes = new JTextField(25);

        // Button od wykonywania zadania
        JButton searcher = new JButton("Zamień");
        searcher.addActionListener(e -> {

            // Sprawdzenie czy argumenty są puste
            if((firstGroupOfBytes.getText().equals("") && secondGroupOfBytes.getText().equals("")) ||
                    firstGroupOfBytes.getText().equals("") || fileExtension.getText().equals("") ||
                    directory.getText().equals(""))
                JOptionPane.showMessageDialog(rootPane, "Nie podano wymaganych argumentów.",
                        "Brak argumentów", JOptionPane.WARNING_MESSAGE);

            // Sprawdzenie czy nie podano za dużo spacji
            else if(checkIfALotOfSpaces(firstGroupOfBytes.getText()) ||
                    checkIfALotOfSpaces(secondGroupOfBytes.getText()))
                JOptionPane.showMessageDialog(rootPane, "Zbyt dużo spacji w argumencie.",
                        "Za dużo spacji", JOptionPane.WARNING_MESSAGE);

            // Sprawdzenie czy podane bajty nie są takie same
            else if(firstGroupOfBytes.getText().equals(secondGroupOfBytes.getText()))
                JOptionPane.showMessageDialog(rootPane, "Podano dwa takie same argumenty.",
                        "Dwa takie same argumenty", JOptionPane.WARNING_MESSAGE);

            // Czy arguemtny nie kończą się i nie zaczynąją spacją oraz czy pasują do wyrażenia regularnego
            else if(firstGroupOfBytes.getText().endsWith(" ") || secondGroupOfBytes.getText().endsWith(" ") ||
                    firstGroupOfBytes.getText().startsWith(" ") || secondGroupOfBytes.getText().startsWith(" ") ||
                    checkIfGoodTyped(firstGroupOfBytes, secondGroupOfBytes, fileExtension)) {
                JOptionPane.showMessageDialog(rootPane, "Podano złe argumenty\n(sprawdź spacje " +
                                ", znaki bądź podane bajty)","Złe argumenty", JOptionPane.WARNING_MESSAGE);
            }

            // Jeśli wyżej wymienione warunki są nieprawdziwe dochodzi do zamiany bajtów
            else {

                // Zamiana podanych argumentów z bajtami na kolekcje
                try {
                    changeToLists(firstGroupOfBytes, secondGroupOfBytes);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                /* Czyszczenie listy plików i rozpoczęcie przeszukiwania folderu w celu znalezienia podkatalogów i
                plików o wymaganym rozszerzeniu */
                files.clear();
                searchFileExtension(files, mainPath, fileExtension.getText());

                // Sprawdzenie czy nowa lista plików posiada pliki, jeśli tak to je wyświetla
                if(files.size() == 0)
                    JOptionPane.showMessageDialog(rootPane, "Brak plików określonego rozszerzenia.",
                            "Nie znaleziono plików danego rozszerzenia", JOptionPane.WARNING_MESSAGE);
                else {
                    for (File f : files)
                        System.out.println(f.getPath());
                    System.out.println("----------");

                    // Dla każdego pliku z listy plików następuje zamiana bajtów jeśli zostaną znalezione
                    for (File f : files) {
                        try {
                            // Bajty pliku przed zamianą
                            ByteReader byteReader = new ByteReader(f, toReplaceList, replacementList);
                            System.out.print(f.getPath() + " - przed zmianą: ");
                            byteReader.showBytes();
                            System.out.println();

                            // Jeżeli są pliki do zamiany to są zamieniane a jeśli nie to zmiana nic nie wniosła
                            if(byteReader.byteChanger()) {
                                System.out.print(f.getPath() + " - po zmianie: ");
                                byteReader.showBytes();
                                System.out.println();
                            }
                            else
                                System.out.println("Zmiana nic nie wniosła");

                            System.out.println("----------");
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }

                    JOptionPane.showMessageDialog(rootPane, "Zmiana powiodła się.",
                            "Zrobione", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // Dodanie elementów do GUI
        JPanel mainPanel = new JPanel();
        mainPanel.add(directoryLabel);
        mainPanel.add(directory);
        mainPanel.add(chooser);
        mainPanel.add(fileExtensionLabel);
        mainPanel.add(fileExtension);
        mainPanel.add(firstGroupOfBytesLabel);
        mainPanel.add(firstGroupOfBytes);
        mainPanel.add(secondGroupOfBytesLabel);
        mainPanel.add(secondGroupOfBytes);

        mainPanel.add(searcher);

        this.add(mainPanel);
    }

    // Zamiana podanych bajtów na listy
    private void changeToLists(JTextField first, JTextField second) throws Exception {
        StringTokenizer tokenizerFinder = new StringTokenizer(first.getText(), " ");
        this.toReplaceList = new ArrayList<>();

        StringTokenizer tokenizerReplacement = new StringTokenizer(second.getText(), " ");
        this.replacementList = new ArrayList<>();

        while (tokenizerFinder.hasMoreElements())
            toReplaceList.add((byte) Integer.parseInt(tokenizerFinder.nextToken(), 16));

        if(toReplaceList.size() == 0)
            throw new Exception("Brak bajtów do zamiany.");

        while (tokenizerReplacement.hasMoreElements())
            replacementList.add((byte) Integer.parseInt(tokenizerReplacement.nextToken(), 16));
    }

    // Sprawdza czy podane argumenty pasują do wzorca regularnego
    private boolean checkIfGoodTyped(JTextField first, JTextField second, JTextField fileExtension) {
        StringTokenizer tokenizerFinder = new StringTokenizer(first.getText(), " ");
        this.toReplaceList = new ArrayList<>();

        StringTokenizer tokenizerReplacement = new StringTokenizer(second.getText(), " ");
        this.replacementList = new ArrayList<>();

        if(!patternExtension.matcher(fileExtension.getText()).matches())
            return true;

        while (tokenizerFinder.hasMoreElements())
            if(!patternTyped.matcher(tokenizerFinder.nextToken()).matches())
                return true;

        while (tokenizerReplacement.hasMoreElements())
            if(!patternTyped.matcher(tokenizerReplacement.nextToken()).matches())
                return true;

        return false;
    }

    // Sprawdza czy nie ma za dużo spacji
    private boolean checkIfALotOfSpaces(String test) {
        boolean repeated = false;
        for (char c : test.toCharArray()) {
            if(c == ' ' && repeated)
                return true;

            repeated = c == ' ';
        }

        return false;
    }

    // Poszukuje danego rozszerzenia pliku w katalogach
    private void searchFileExtension(LinkedList<File> linkedList, String mainPath, String fileExtension) {
        File file = new File(mainPath);
        File[] paths = file.listFiles();

        if(paths != null) {
            for (File f : paths) {
                if(f != null && !f.isHidden()) {
                    if (f.isDirectory()) {
                        if (!f.isHidden())
                            searchFileExtension(linkedList, f.getPath(), fileExtension);
                    } else if (f.isFile() && f.getPath().endsWith("." + fileExtension))
                        linkedList.add(f);
                }
            }
        }
        else
            System.out.println("Błąd dostępu do pliku " + file.getPath());
    }

    // Uruchomienie
    public static void main(String[] args) {
        QBSFrame qbsFrame = new QBSFrame();
        qbsFrame.setVisible(true);
    }
}
