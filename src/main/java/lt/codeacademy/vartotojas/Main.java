package lt.codeacademy.vartotojas;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    static final DateTimeFormatter ISO_LOCAL_DATE_TIME_NO_MILIS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final SessionFactory factory;
    static Scanner in = new Scanner(System.in);
    static JedisPool jedisPool = new JedisPool();
    static Jedis jedis = jedisPool.getResource();

    static {
        Logger.getLogger("org.mongodb.driver")
                .setLevel(Level.WARNING);
        Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
        factory = new Configuration().configure().buildSessionFactory();
    }

    public static void main(String[] args) {

        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("mano");

        if (!jedis.exists("vartotojai_ids")) {
            for (Document doc : db.getCollection("vartotojai").find()) {
                jedis.rpush("vartotojai_ids", doc.getObjectId("_id").toHexString());
            }
        }

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
                    │ 5 - Atspausdinti viena vartotoja │
                    │ 6 - Baigti programa              │
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
                case 5 -> spausdintiVartotoja(db);
                case 6 -> {
                    break menu;
                }
                default -> System.out.println("Blogas pasirinkimas!");
            }
        }

        factory.close();
        client.close();
        in.close();
        System.out.println("Programa baigia darba!");
    }

    private static void ivestiVartotoja(MongoDatabase db) {
        int id = 0;
        /*String vardas = vardoIvestis();
        String slaptazodis = slaptazodzioIvestis();
        String email = emailIvestis();
        String lytis = lytiesIvestis();
        LocalDate gimimoData = gimimoDatosIvestis();*/

        String vardas = "Jonas";
        String slaptazodis = "2183bc4a0b6a9cf387c1302da9bb0ac8a55ed540";
        String email = "jonas@gmail.com";
        String lytis = "vyras";
        LocalDate gimimoData = LocalDate.of(2000, 1, 1);

        User user = new User(0, vardas, slaptazodis, email, lytis, gimimoData, LocalDateTime.now());

        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(user);
            tx.commit();
        }

        id = user.getId();
        System.out.println("Naujo elemento id: " + id);

        jedis.rpush("vartotojai_ids", String.valueOf(id));

        System.out.println("Vartotojas sukurtas.");
        jedis.del("vartotojai");
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

        findId(keiciamasId).ifPresent(objectId -> {
            Bson filter = Filters.eq("_id", objectId);
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
            jedis.del("vartotojai");
            jedis.del(objectId.toHexString());
        });

    }

    public static Optional<ObjectId> findId(int id) {
        if (id < 1) {
            System.out.println("id privalo buti ne maziau uz 1!");
            return Optional.empty();
        }

        if (id > jedis.llen("vartotojai_ids")) {
            System.out.println("Vartotojas tokiu id nerastas!");
            return Optional.empty();
        }

        return Optional.of(new ObjectId(jedis.lindex("vartotojai_ids", id - 1)));
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

        findId(trinamasId).ifPresent(objectId -> {
            collection.deleteOne(Filters.eq("_id", objectId));
            System.out.println("Vartotojas istrintas");
            String id = objectId.toHexString();
            jedis.lrem("vartotojai_ids", 1, id);
            jedis.del("vartotojai");
            jedis.del(id);
        });
    }

    private static void spausdintiVartotojus(MongoDatabase db, boolean menu) {
        long kiekis = jedis.llen("vartotojai_ids");
        if (kiekis == 0) {
            System.out.println("Vartotoju nera!");
            return;
        }

        String pasirinkimas = "1";

        if (menu) {
            System.out.print("""
                    Kur norite isvesti vartotojus
                    1 - I ekrana
                    2 - I faila
                    3 - I ekrana ir i faila
                    Jusu pasirinkimas:\s""");
            pasirinkimas = in.nextLine();

            if (!pasirinkimas.equals("1") && !pasirinkimas.equals("2") && !pasirinkimas.equals("3")) {
                System.out.println("Blogas pasirinkimas!");
                return;
            }
        }

        String text;
        if (jedis.exists("vartotojai")) {
            text = jedis.get("vartotojai");
            System.out.println("cache hit!");
        } else {
            StringBuilder sb = new StringBuilder();

            MongoCollection<Document> collection = db.getCollection("vartotojai");
            int i = 1;
            for (Document doc : collection.find()) {
                sb.append(docToString(i, doc)).append("\n");
                i++;
            }

            text = sb.toString();
            jedis.set("vartotojai", text);
        }

        switch (pasirinkimas) {
            case "1" -> System.out.print(text);
            case "2" -> issaugotiIFaila(text);
            case "3" -> {
                System.out.print(text);
                issaugotiIFaila(text);
            }
        }
    }

    private static String docToString(int i, Document doc) {
        String id = doc.getObjectId("_id").toHexString();
        String vardas = doc.getString("vardas");
        String slaptazodis = doc.getString("slaptazodis");
        String email = doc.getString("email");
        String lytis = doc.getString("lytis");

        String gimimoData = LocalDate.ofInstant(doc.getDate("gimimo_data").toInstant(), ZoneId.of("UTC"))
                .format(DateTimeFormatter.ISO_DATE);

        String registracijosData = LocalDateTime.ofInstant(doc.getDate("registracijos_data").toInstant(), ZoneId.of("UTC"))
                .format(ISO_LOCAL_DATE_TIME_NO_MILIS);

        return String.format("%03d | %s | ****** | %s | %s | %s | %s",
                i, vardas, email, lytis,
                gimimoData, registracijosData
        );
    }

    private static void spausdintiVartotoja(MongoDatabase db) {
        long kiekis = jedis.llen("vartotojai_ids");
        if (kiekis == 0) {
            System.out.println("Vartotoju nera!");
            return;
        }

        System.out.printf("Kuri vartotoja norite istrinti(1-%d): ", kiekis);
        int idInt = 0;
        try {
            idInt = in.nextInt();
        } catch (InputMismatchException e) {
            System.err.println("Blogai nurodytas id!");
        } finally {
            in.nextLine();
        }

        findId(idInt).ifPresent(id -> {
            String idString = id.toHexString();
            if (jedis.exists(idString)) {
                System.out.println(jedis.get(idString));
            } else {
                MongoCollection<Document> collection = db.getCollection("vartotojai");
                Document doc = collection.find(Filters.eq("_id", id)).first();
                String text = docToString(1, doc);
                System.out.println(text);
                jedis.set(idString, text);
            }
        });
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