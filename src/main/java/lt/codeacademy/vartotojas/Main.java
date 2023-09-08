package lt.codeacademy.vartotojas;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static lt.codeacademy.vartotojas.Ivestis.*;

public class Main {
    // 2011-12-03T10:15:30
    static final DateTimeFormatter ISO_LOCAL_DATE_TIME_NO_MILIS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        Logger.getLogger("org.mongodb.driver")
                .setLevel(Level.WARNING);

        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("mano");

        int pasirinkimas;

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
                case 1 -> ivestiVartotoja(db);
                case 2 -> modifikuotiVartotoja(db);
                case 3 -> trintiVartotoja(db);
                case 4 -> spausdintiVartotojus(db, true);
                case 5 -> {
                    break menu;
                }
                default -> System.out.println("Blogas pasirinkimas!");
            }
        }

        client.close();
        in.close();
        System.out.println("Programa baigia darba!");
    }

    private static void ivestiVartotoja(MongoDatabase db) {
        String vardas = vardoIvestis();
        String slaptazodis = slaptazodzioIvestis();
        String email = emailIvestis();
        String lytis = lytiesIvestis();
        LocalDate gimimoData = gimimoDatosIvestis();

        MongoCollection<Document> collection = db.getCollection("vartotojai");

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

    private static void modifikuotiVartotoja(MongoDatabase db) {
        spausdintiVartotojus(db, false); // Galima nieko neisvesti jei daug vartotoju.
        MongoCollection<Document> collection = db.getCollection("vartotojai");
        System.out.print("Kuri vartotoja norite keisti: ");
        int keiciamasId;
        try {
            keiciamasId = in.nextInt();
        } catch (InputMismatchException e) {
            System.err.println("Blogai nurodytas id!");
            return;
        } finally {
            in.nextLine();
        }

        findId(collection, keiciamasId).map(objectId -> Filters.eq("_id", objectId)).ifPresent(filter -> {
            System.out.print("""
                    1 - vardas
                    2 - slaptazodis
                    3 - email
                    4 - lytis
                    Kuri lauka norite keisti:\s""");
            String pasirinkimas = in.nextLine();

            switch (pasirinkimas) {
                case "1" -> collection.updateOne(filter, Updates.set("vardas", Ivestis.vardoIvestis()));
                case "2" -> collection.updateOne(filter, Updates.set("slaptazodis", Ivestis.slaptazodzioIvestis()));
                case "3" -> collection.updateOne(filter, Updates.set("email", Ivestis.emailIvestis()));
                case "4" -> collection.updateOne(filter, Updates.set("lytis", Ivestis.lytiesIvestis()));
                default -> {
                    System.out.println("Blogas pasirinkimas!");
                    return;
                }
            }
            System.out.println("Vartotojas pakoreguotas.");
        });

    }

    public static Optional<ObjectId> findId(MongoCollection<Document> collection, int id) {
        if (id < 1) {
            System.out.println("id privalo buti ne maziau uz 1!");
            return Optional.empty();
        }

        ArrayList<ObjectId> vartIds = new ArrayList<>();

        for (Document doc : collection.find())
            vartIds.add(doc.getObjectId("_id"));

        if (id > vartIds.size()) {
            System.out.println("Vartotojas tokiu id nerastas!");
            return Optional.empty();
        }

        return Optional.of(vartIds.get(id - 1));
    }

    private static void trintiVartotoja(MongoDatabase db) {
        spausdintiVartotojus(db, false); // Galima nieko neisvesti jei daug vartotoju.
        MongoCollection<Document> collection = db.getCollection("vartotojai");

        System.out.print("Kuri vartotoja norite istrinti: ");
        int trinamasId;
        try {
            trinamasId = in.nextInt();
        } catch (InputMismatchException e) {
            System.err.println("Blogai nurodytas id!");
            return;
        } finally {
            in.nextLine();
        }

        findId(collection, trinamasId).ifPresent(objectId -> {
            collection.deleteOne(Filters.eq("_id", objectId));
            System.out.println("Vartotojas istrintas");
        });
    }

    private static void spausdintiVartotojus(MongoDatabase db, boolean menu) {
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

        MongoCollection<Document> collection = db.getCollection("vartotojai");
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
                    .format(ISO_LOCAL_DATE_TIME_NO_MILIS);

            sb.append(String.format("%03d | %s | ****** | %s | %s | %s | %s\n",
                    i, vardas, email, lytis,
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