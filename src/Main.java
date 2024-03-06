import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Dictionary dictionary;

        try {
            dictionary = new Dictionary();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return;
        }

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

                case 1:

                case 2:

                System.out.println("Search");
                System.out.println("\nTITLE: ");
                String title = in.nextLine();
                System.out.println("\nAUTHOR: ");
                String author = in.nextLine();
                System.out.println("\nBODY: ");
                String body = in.nextLine();
                dictionary.search(title, author, body);
            }
           //exit condition

        }
    }
}