package lt.codeacademy.vartotojas;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static lt.codeacademy.vartotojas.Ivestis.*;

public class Main {
    static Scanner in = new Scanner(System.in);
    private static MongoClient client;

    public static void connectToDB() {
        Logger.getLogger("org.mongodb.driver")
                .setLevel(Level.WARNING);
        client = new MongoClient();
    }

    public static void main(String[] args) {
        int pasirinkimas;
        connectToDB();

        menu:
        while (true) {
            System.out.print("""
                                        
                    ┌──────────────────────────────────┐
                    │               MENIU              │
                    ├──────────────────────────────────┤
                    │ 1 - Ivesti vartotoja             │
                    │ 2 - Pakeisti esama vartotoja     │
                    │ 3 - Trinti vartotoja             │
                    │ 4 - Atspausdinti vartotojus      │
                    │ 5 - Baigti programa              │
                    └──────────────────────────────────┘
                      Jusu pasirinkimas:\s""");

            try {
                pasirinkimas = in.nextInt();
            } catch (InputMismatchException e) {
                pasirinkimas = -1;
            }

            in.nextLine();
            switch (pasirinkimas) {
                case 1 -> ivestiVartotoja();
                case 2 -> modifikuotiVartotoja();
                case 3 -> trintiVartotoja();
                case 4 -> spausdintiVartotojus(true);
                case 5 -> {
                    break menu;
                }
                default -> System.out.println("Blogas pasirinkimas!");
            }
        }
        in.close();
        System.out.println("Programa baigia darba!");
    }

    private static void ivestiVartotoja() {
        String vardas = vardoIvestis();
        String slaptazodis = slaptazodzioIvestis();
        String email = emailIvestis();
        String lytis = lytiesIvestis();
        LocalDate gimimoData = gimimoDatosIvestis();

        MongoCollection<Document> collection = client.getDatabase("mano")
                .getCollection("vartotojai");

        Document doc = new Document()
                .append("vardas", vardas)
                .append("slaptazodis", slaptazodis)
                .append("email", email)
                .append("lytis", lytis)
                .append("gimimo_data", gimimoData)
                .append("registracijos_data", LocalDateTime.now());

        collection.insertOne(doc);

        System.out.println("Vartotojas sukurtas.");
    }

    private static void modifikuotiVartotoja() {
        spausdintiVartotojus(false); // Galima nieko neisvesti jei daug vartotoju.
        System.out.print("Kuri vartotoja norite keisti: ");
        int keiciamasId = in.nextInt();
        in.nextLine();

        try {
            Connection conn = null; // Added so code compiles!
            Statement stat = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stat.executeQuery("SELECT * FROM vartotojai WHERE id = " + keiciamasId);
            cond:
            if (rs.next()) {
                System.out.print("""
                        1 - vardas
                        2 - slaptazodis
                        3 - email
                        4 - lytis
                        Kuri lauka norite keisti:\s""");
                String pasirinkimas = in.nextLine();

                switch (pasirinkimas) {
                    case "1" -> rs.updateString("vardas", Ivestis.vardoIvestis());
                    case "2" -> rs.updateString("slaptazodis", Ivestis.slaptazodzioIvestis());
                    case "3" -> rs.updateString("email", Ivestis.emailIvestis());
                    case "4" -> rs.updateString("lytis", Ivestis.lytiesIvestis());
                    default -> {
                        System.out.println("Blogas pasirinkimas!");
                        break cond;
                    }
                }
                rs.updateRow();
                System.out.println("Vartotojas pakoreguotas.");
            } else {
                System.out.println("indeksas " + keiciamasId + " nerastas");
            }
        } catch (SQLException e) {
            System.err.println("Ivyko duomenu bazes klaida!");
        }

    }

    private static void trintiVartotoja() {
        spausdintiVartotojus(false); // Galima nieko neisvesti jei daug vartotoju.
        System.out.print("Kuri vartotoja norite istrinti: ");

        MongoCollection<Document> collection = client.getDatabase("mano").getCollection("vartotojai");
        ArrayList<ObjectId> vartIds = new ArrayList<>();

        for (Document doc : collection.find()) {
            vartIds.add(doc.getObjectId("_id"));
        }

        try {
            int trinamasId = in.nextInt();
            if (trinamasId < 1) {
                System.out.println("id privalo buti ne maziau uz 1!");
                return;
            }
            if (trinamasId <= vartIds.size()) {
                collection.deleteOne(Filters.eq("_id", vartIds.get(trinamasId - 1)));
                System.out.println(vartIds.get(trinamasId - 1));
                System.out.println("Vartotojas istrintas");
            } else {
                System.out.println("Vartotojas tokiu id nerastas!");
            }
        } catch (InputMismatchException e) {
            System.err.println("Blogai nurodytas id!");
        } finally {
            in.nextLine();
        }
    }

    private static void spausdintiVartotojus(boolean menu) {
        //TODO Prideti tikrinima ar yra vartotoju duomenu bazeje.

        String pasirinkimas = "1";

        if (menu) {
            System.out.print("""
                    Kur norite isvesti vartotojus
                    1 - I ekrana
                    2 - I faila
                    3 - I ekrana ir i faila
                    Jusu pasirinkimas:\s""");
            pasirinkimas = in.nextLine();
        }


        if (!pasirinkimas.equals("1") && !pasirinkimas.equals("2") && !pasirinkimas.equals("3")) {
            System.out.println("Blogas pasirinkimas!");
            return;
        }

        StringBuilder sb = new StringBuilder();

        MongoCollection<Document> collection = client.getDatabase("mano")
                .getCollection("vartotojai");
        int i = 1;
        for (Document doc : collection.find()) {
            String id = doc.getObjectId("_id").toHexString();
            String vardas = doc.getString("vardas");
            String slaptazodis = doc.getString("slaptazodis");
            String email = doc.getString("email");
            String lytis = doc.getString("lytis");

            String gimimoData = LocalDate.ofInstant(doc.getDate("gimimo_data").toInstant(), ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ISO_DATE);

            String registracijosData = LocalDateTime.ofInstant(doc.getDate("registracijos_data").toInstant(), ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            sb.append(String.format("%3d | %s | %s | %s | %s | %s | %s\n",
                    i, vardas, slaptazodis, email, lytis,
                    gimimoData, registracijosData
            ));
            i++;
        }

        String text = sb.toString();

        switch (pasirinkimas) {
            case "1" -> System.out.print(text);
            case "2" -> issaugotiIFaila(text);
            case "3" -> {
                System.out.print(text);
                issaugotiIFaila(text);
            }
        }
    }

    private static void issaugotiIFaila(String text) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss");
        String filename = "vartotojai_" + LocalDateTime.now().format(dtf) + ".txt";
        File file = new File(filename);

        try {
            Files.writeString(file.toPath(), text, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            System.out.println("Failas issaugotas vardu: " + filename);
        } catch (IOException e) {
            System.out.println("Failo issaugoti nepavyko!");
        }
    }
}