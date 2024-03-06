import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;

import com.kursx.parser.fb2.*;

public class Dictionary {

    // TreeMap to store the inverted index with zones
    TreeMap<String, TreeMap<String, TreeSet<Integer>>> dictionary;

    // Variables to store statistics about the collection and dictionary
    private int wordsInCollection;
    private int wordsInDictionary;
    private int sizeCollection;
    private long sizeDictionary;

    /**File references array*/
    File[] textFiles;

    Dictionary() throws Exception {

        // Initialize data structures
        dictionary = new TreeMap<>();
        wordsInCollection = 0;
        wordsInDictionary = 0;
        sizeCollection = 0;
        sizeDictionary = 0;

        //files folder path
        File folder = new File("src/res");

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
            sizeCollection += textFiles[i].length();
            FictionBook fictionBook = new FictionBook(textFiles[i]);

            // Process author names
            for (Person person : fictionBook.getAuthors()) {
                String author = person.getFullName();
                StringTokenizer stringTokenizer = new StringTokenizer(author);
                while (stringTokenizer.hasMoreTokens()) {
                    wordsInCollection++;
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

            // Process title
            String title = fictionBook.getTitle();
            StringTokenizer stringTokenizer = new StringTokenizer(title);
            while (stringTokenizer.hasMoreTokens()) {
                wordsInCollection++;
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
                        wordsInCollection++;
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
        wordsInDictionary = dictionary.size();

        // Write dictionary information to a file
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter("src/results/dictionary.txt")));
        printWriter.write("COLLECTION SIZE: " + sizeCollection + " bytes \n");
        printWriter.write("WORDS IN COLLECTION: " + wordsInCollection + "\n");
        printWriter.write("WORDS IN DICTIONARY: " + wordsInDictionary + "\n");
        printWriter.write("\n \n");
        for (String word : dictionary.keySet()) {
            printWriter.println(word);
            for (String group : dictionary.get(word).keySet()) {
                printWriter.println("   " + group + "      " + dictionary.get(word).get(group));
            }
        }
        printWriter.close();

        // Update size of the dictionary file
        File output = new File("dictionary.txt");
        sizeDictionary = output.length();

        // Append dictionary size information to the same file
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("src/results/dictionary.txt", true)));
        out.println("\nDICTIONARY SIZE: " + sizeDictionary + " bytes \n");
        out.flush();
        out.close();
    }

    // Method to perform a query-based search using zone ranking
    public void search(String title, String author, String body) {
        ArrayList<Integer> authorDocs = new ArrayList<>();
        ArrayList<Integer> titleDocs = new ArrayList<>();
        ArrayList<Integer> bodyDocs = new ArrayList<>();

        try {

            // Process title query
            if (title.equals("")) {
                for (int i = 0; i < textFiles.length; i++) {
                    titleDocs.add(i);
                }
            } else {
                titleDocs.addAll(dictionary.get(title).get("title"));
            }

            // Process author query
            if (author.equals("")) {
                for (int i = 0; i < textFiles.length; i++) {
                    authorDocs.add(i);
                }
            } else {
                authorDocs.addAll(dictionary.get(author).get("author"));
            }

            // Process body query
            if (body.equals("")) {
                for (int i = 0; i < textFiles.length; i++) {
                    bodyDocs.add(i);
                }
            } else {
                bodyDocs.addAll(dictionary.get(body).get("body"));
            }

        } catch (NullPointerException e) {
            System.out.println("No documents for the specified search have been found.");
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

        // Array of predefined values to determine the relevance order
        BigDecimal[] values = {BigDecimal.valueOf(1.0), BigDecimal.valueOf(0.9), BigDecimal.valueOf(0.7),
                BigDecimal.valueOf(0.6), BigDecimal.valueOf(0.4), BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.1)};

        // Print the documents in order of relevance for each value
        for (BigDecimal value : values)
            for (Integer i : map.keySet())
                if (map.get(i).equals(value))
                    System.out.println(i + " " + map.get(i));

        System.out.println();
    }
}