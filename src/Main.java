import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        String collectionPath = "src/res";
        String dictionaryLocation = "src/results/dictionary.txt";


        Dictionary dictionary = new Dictionary(collectionPath, dictionaryLocation);


        Scanner in = new Scanner(System.in);
        System.out.println("""
                0 - Open dictionary.txt;
                1 - Print statistics in console;
                2 - Search dictionary.txt
                -1 - Exit.
                """);

        System.out.println("\nYour input here: ");
        int i = in.nextInt();


        while(i != -1) {
            switch(i) {
                case 0:
                    dictionary.openTXT(dictionary.getDictPath());
                    break;
                case 1:
                    dictionary.printStats();
                    dictionary.openTXT("src/results/stats.txt");
                    break;
                case 2:
                    System.out.println("Input title: ");
                    in.nextLine();
                    String title = in.nextLine();
                    System.out.println("Input author: ");
                    String author = in.nextLine();
                    System.out.println("Input body: ");
                    String body = in.nextLine().toLowerCase();

                    dictionary.search(title, author, body);
                    break;
                default:
                    System.out.println("Incorrect format! Try again.");
        }

            System.out.println("""
                0 - Open dictionary.txt;
                1 - Print statistics in console;
                2 - Search dictionary.txt
                -1 - Exit.
                """);
        System.out.println("\nYour input here: ");
        i = in.nextInt();
    }
    }
}