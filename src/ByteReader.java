import java.io.*;
import java.util.*;

public class ByteReader {
    private File file;
    private List<Byte> bytesToWrite;
    private List<Byte> toReplaceList;
    private List<Byte> replacementList;

    // Konstruktor otrzymujący plik oraz tekst z bajtami
    public ByteReader(File file, String foundBytes, String replacedBytes) throws Exception {
        this.file = file;
        this.bytesToWrite = new ArrayList<>();

        StringTokenizer tokenizerFinder = new StringTokenizer(foundBytes, " ");
        this.toReplaceList = new ArrayList<>();

        StringTokenizer tokenizerReplacement = new StringTokenizer(replacedBytes, " ");
        this.replacementList = new ArrayList<>();

        while (tokenizerFinder.hasMoreElements())
            toReplaceList.add((byte) Integer.parseInt(tokenizerFinder.nextToken(), 16));

        if (toReplaceList.size() == 0)
            throw new Exception("Brak bajtów do zamiany.");

        while (tokenizerReplacement.hasMoreElements())
            replacementList.add((byte) Integer.parseInt(tokenizerReplacement.nextToken(), 16));
    }

    // Konstruktor przyjmujący plik oraz kolekcje bajtów
    public ByteReader(File file, List<Byte> toReplaceList, List<Byte> replacementList) throws Exception {
        this.file = file;
        this.bytesToWrite = new ArrayList<>();

        this.toReplaceList = toReplaceList;
        this.replacementList = replacementList;
    }

    // Funkcja do zamiany bajtów
    public boolean byteChanger() throws Exception {
        boolean findSequence = true;
        int indexWhereMatch = 0;
        int indexWhereFailed = 0;

        try {
            // Otworzenie strumienia wejścia
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            byte[] bytes = inputStream.readAllBytes();

            // Sprawdzenie czy plik nie jest pusty
            if (bytes.length == 0)
                throw new Exception("Plik jest pusty.");

            // Sprawdzenie czy w pliku znajduje się sekwecja bajtów do znalezienia oraz ich natychmiastowa zamiana
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] == toReplaceList.get(0)) {
                    indexWhereMatch = i;
                    for (int j = 1; j < toReplaceList.size(); j++) {
                        i++;
                        if (i == file.length() || bytes[i] != toReplaceList.get(j)) {
                            findSequence = false;
                            indexWhereFailed = i - 1;
                            break;
                        }
                    }
                    if (findSequence) {
                        bytesToWrite.addAll(replacementList);
                        i = indexWhereMatch + toReplaceList.size() - 1;
                    } else {
                        for (int j = indexWhereMatch; j < indexWhereFailed + 1; j++)
                            bytesToWrite.add(bytes[j]);

                        i = indexWhereFailed;
                        findSequence = true;
                    }
                } else {
                    bytesToWrite.add(bytes[i]);
                }
            }

            boolean checkIfDifferent = false;
            inputStream.close();

            // Sprawdzenie czy zaszła w ogóle jakaś zmiana bajtów
            if (bytes.length == bytesToWrite.size()) {
                for (int i = 0; i < bytes.length; i++) {
                    if (bytes[i] != bytesToWrite.get(i)) {
                        checkIfDifferent = true;
                        break;
                    }
                }
            } else
                checkIfDifferent = true;

            // Jeśli zaszła zmiana bajtów zapisz nową sekwencję bajtów
            if (checkIfDifferent) {
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                for (Byte b : bytesToWrite)
                    outputStream.write(b);

                outputStream.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Pokaż sekwencję bajtów do znaleziania i do zmiany
    public void showPreparation() {
        System.out.print("Do znalezienia: ");

        for (Byte b : toReplaceList) {
            System.out.printf("%02X ", b);
        }

        System.out.println();
        System.out.println("----------------");

        System.out.print("Do zamiany: ");

        for (Byte b : replacementList) {
            System.out.printf("%02X ", b);
        }

        System.out.println();
        System.out.println("----------------");
    }

    // Pokaż sekwencję bajtów pliku
    public void showBytes() {
        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            byte[] bytes = inputStream.readAllBytes();

            for (int i = 0; i < bytes.length; i++) {
                System.out.printf("%02X ", bytes[i]);
                if ((i + 1) % 40 == 0)
                    System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
