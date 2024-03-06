import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import com.kursx.parser.fb2.*;

/**Using inverted dictionary*/
public class Dictionary {

    // TreeMap to store the inverted index with zones
    TreeMap<String, TreeMap<String, TreeSet<Integer>>> dictionary;

    // Variables to store statistics about the collection and dictionary
    private int collectionWordCount;
    private int dictionaryWordCount;
    private int collectionSize;
    private double dictionarySize;

    private String collectionLocation;

    private String dictionaryLocation;


    /**File references array*/
    File[] textFiles;

    Dictionary(String collPath, String dictPath) throws Exception {

        // Initialize data structures
        dictionary = new TreeMap<>();
        collectionWordCount = 0;
        dictionaryWordCount = 0;
        collectionSize = 0;
        dictionarySize = 0;
        collectionLocation = collPath;
        dictionaryLocation = dictPath;

        //files folder path
        File folder = new File(collectionLocation);

        //Get files from folder
        File[] allFiles = folder.listFiles();

        //Check if files are in the folder!!!
        if (allFiles != null) {
            textFiles = new File[allFiles.length];

            //copy all files to textFiles arr
            System.arraycopy(allFiles, 0, textFiles, 0, allFiles.length);

            buildDictionary();

        } else {
            //No files are found in the folder
            System.out.println("No files found in the specified folder.");
        }
    }

    /**Build the inverted index dictionary*/
    public void buildDictionary() throws Exception {
        for (int i = 0; i < textFiles.length; i++) {
            collectionSize += textFiles[i].length();
            FictionBook fictionBook = new FictionBook(textFiles[i]);

            // Process author names
            for (Person person : fictionBook.getAuthors()) {
                String author = person.getFullName();
                StringTokenizer stringTokenizer = new StringTokenizer(author);
                while (stringTokenizer.hasMoreTokens()) {
                    collectionWordCount++;
                    String s = stringTokenizer.nextToken().toLowerCase();
                    TreeMap<String, TreeSet<Integer>> wordMap = dictionary.get(s);
                    if (wordMap == null) {
                        wordMap = new TreeMap<>();
                    }
                    TreeSet<Integer> setOfIndices = wordMap.get("author");
                    if (setOfIndices == null) {
                        setOfIndices = new TreeSet<>();
                    }
                    setOfIndices.add(i);
                    wordMap.put("author", setOfIndices);
                    dictionary.put(s, wordMap);
                }
            }

            //Process title
            String title = fictionBook.getTitle();
            StringTokenizer stringTokenizer = new StringTokenizer(title);
            while (stringTokenizer.hasMoreTokens()) {
                collectionWordCount++;
                String s = stringTokenizer.nextToken().toLowerCase();
                TreeMap<String, TreeSet<Integer>> wordMap = dictionary.get(s);
                if (wordMap == null) {
                    wordMap = new TreeMap<>();
                }
                TreeSet<Integer> setOfIndices = wordMap.get("title");
                if (setOfIndices == null) {
                    setOfIndices = new TreeSet<>();
                }
                setOfIndices.add(i);
                wordMap.put("title", setOfIndices);
                dictionary.put(s, wordMap);
            }

            // Process body text
            for (Section section : fictionBook.getBody().getSections()) {
                for (Element element : section.getElements()) {
                    String text = element.getText();
                    stringTokenizer = new StringTokenizer(text, ".,:;()[]{}<>_- —=+“”'`\"/|!?$^&*@#%0123456789");
                    while (stringTokenizer.hasMoreTokens()) {
                        collectionWordCount++;
                        String s = stringTokenizer.nextToken().toLowerCase();
                        TreeMap<String, TreeSet<Integer>> wordMap = dictionary.get(s);
                        if (wordMap == null) {
                            wordMap = new TreeMap<>();
                        }
                        TreeSet<Integer> setOfIndices = wordMap.get("body");
                        if (setOfIndices == null) {
                            setOfIndices = new TreeSet<>();
                        }
                        setOfIndices.add(i);
                        wordMap.put("body", setOfIndices);
                        dictionary.put(s, wordMap);
                    }
                }
            }

        }
        dictionaryWordCount = dictionary.size();

        // Write dictionary information to a file
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(dictionaryLocation)));
        printWriter.write("|||||| Zoned Dictionary ||||||\n");
        for (String word : dictionary.keySet()) {
            printWriter.println(word);
            for (String group : dictionary.get(word).keySet()) {
                printWriter.println("   " + group + "      " + dictionary.get(word).get(group));
            }
        }
        printWriter.close();

        //Update size of the dictionary file
        File output = new File(dictionaryLocation);
        long fileSizeBytes = output.length(); // File size in bytes

        //Convert bytes to kilobytes
        dictionarySize = (double) fileSizeBytes / 1024;
    }

    // Method to perform a query-based search using zone ranking
    public void search(String title, String author, String body) {
        String searchTitle = title.toLowerCase();
        String searchAuthor = author.toLowerCase();
        String searchBody = body.toLowerCase();

        ArrayList<Integer> authorDocs = new ArrayList<>();
        ArrayList<Integer> titleDocs = new ArrayList<>();
        ArrayList<Integer> bodyDocs = new ArrayList<>();

        try {
            if (searchTitle.equals("")) {
                for (int i = 0; i < textFiles.length; i++) {
                    titleDocs.add(i);
                }
            }
            else {
                for (Integer docID : dictionary.get(searchTitle).get("title")) {
                    titleDocs.add(docID);
                }
            }

            if (searchAuthor.equals("")) {
                for (int i = 0; i < textFiles.length; i++) {
                    authorDocs.add(i);
                }
            }
            else {
                for (Integer docID : dictionary.get(searchAuthor).get("author")) {
                    authorDocs.add(docID);
                }
            }
            if (searchBody.equals("")) {
                for (int i = 0; i < textFiles.length; i++) {
                    bodyDocs.add(i);
                }
            }
            else {
                for (Integer docID : dictionary.get(searchBody).get("body")) {
                    bodyDocs.add(docID);
                }
            }
        } catch(NullPointerException e){
            System.out.println("No files found in the specified folder.");
        }


        // TreeMap to store the score for each document
        TreeMap<Integer, BigDecimal> map = new TreeMap<>();

        // Assign weights and calculate scores for each document
        for (Integer docID : titleDocs) {
            map.putIfAbsent(docID, BigDecimal.valueOf(0.0));
            map.put(docID, map.get(docID).add(BigDecimal.valueOf(0.6)));
        }

        for (Integer docID : authorDocs) {
            map.putIfAbsent(docID, BigDecimal.valueOf(0.0));
            map.put(docID, map.get(docID).add(BigDecimal.valueOf(0.3)));
        }

        for (Integer docID : bodyDocs) {
            map.putIfAbsent(docID, BigDecimal.valueOf(0.0));
            map.put(docID, map.get(docID).add(BigDecimal.valueOf(0.1)));
        }

        //Array of predefined values to determine the relevance order
        BigDecimal[] values = {BigDecimal.valueOf(1.0), BigDecimal.valueOf(0.9), BigDecimal.valueOf(0.7),
                BigDecimal.valueOf(0.6), BigDecimal.valueOf(0.4), BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.1)};

        //Print the documents in order of relevance for each value
        for (BigDecimal value : values)
            for (Integer i : map.keySet())
                if (map.get(i).equals(value))
                    System.out.println(i + " " + map.get(i));

        System.out.println();
    }

    public String getDictPath(){
        return dictionaryLocation;
    }
    public String getCollPath(){
        return collectionLocation;
    }
    public int getCollectionWordCount(){
        return collectionWordCount;
    }
    public int getDictionaryWordCount(){
        return dictionaryWordCount;
    }
    public int getCollectionSize(){
        return collectionSize;
    }
    public double getDictionarySize() {
        return dictionarySize;
    }

    public void printStats() throws IOException {
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter("src/results/stats.txt")));
        printWriter.write("Collection size: " + collectionSize/1024 + " kb;\n");
        printWriter.write("Dictionary size: " + dictionarySize + " kb;\n");
        printWriter.write("Words in collection: " + collectionWordCount + ";\n");
        printWriter.write("Words in dictionary: " + dictionaryWordCount + ";\n");

        printWriter.close();
    }

    /**Open .txt files*/
    public void openTXT(String filePath) throws IOException {
        File file = new File(filePath);

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            System.out.println("\nPulling up the file...");

            if (file.exists()) {
                desktop.open(file);
            } else {
                System.out.println("File not found: " + filePath + "; Please restart the program.");
            }
        } else {
            System.out.println("Desktop is not supported.");
        }
    }
}