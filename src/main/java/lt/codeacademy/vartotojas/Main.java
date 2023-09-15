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
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.List;
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

        try (Session session = factory.openSession()) {
            int userCount = session.createQuery("select count(*) FROM User", int.class).getFirstResult();
            jedis.set("vartotojai_size", String.valueOf(userCount));
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
                case 1 -> ivestiVartotoja();
                case 2 -> modifikuotiVartotoja(db);
                case 3 -> trintiVartotoja(db);
                case 4 -> spausdintiVartotojus(true);
                case 5 -> spausdintiVartotoja();
                case 6 -> {
                    break menu;
                }
                default -> System.out.println("Blogas pasirinkimas!");
            }
        }

        jedis.close();
        jedisPool.close();
        factory.close();
        client.close();
        in.close();
        System.out.println("Programa baigia darba!");
    }

    private static void ivestiVartotoja() {
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

        int id = user.getId();

        jedis.incr("vartotojai_size");

        System.out.println("Vartotojas sukurtas.");
        jedis.del("vartotojai");
    }

    private static void modifikuotiVartotoja(MongoDatabase db) {
        spausdintiVartotojus(false); // Galima nieko neisvesti jei daug vartotoju.
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
        spausdintiVartotojus(false); // Galima nieko neisvesti jei daug vartotoju.
        MongoCollection<Document> collection = db.getCollection("vartotojai");

        System.out.print("Kuri vartotoja norite istrinti: ");
        int id;
        try {
            id = in.nextInt();
        } catch (InputMismatchException e) {
            System.err.println("Blogai nurodytas id!");
            return;
        } finally {
            in.nextLine();
        }

        try (Session session = factory.openSession()) {
            User user = session.find(User.class, id);
            if (user != null) {
                Transaction tx = session.beginTransaction();
                session.remove(user);
                tx.commit();
            }
        }

        System.out.println("Vartotojas istrintas");
        String idString = String.valueOf(id);
        jedis.decr("vartotojai_size");
        jedis.del("vartotojai");
        jedis.del(idString);
    }

    private static void spausdintiVartotojus(boolean menu) {
        long kiekis = Integer.parseInt(jedis.get("vartotojai_size"));
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

            List<User> users;
            try (Session session = factory.openSession()) {
                users = session.createQuery("from User", User.class).getResultList();
            }

            for (User user : users) {
                sb.append(user.toSimpleString()).append("\n");
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

    private static void spausdintiVartotoja() {
        long kiekis = Integer.parseInt(jedis.get("vartotojai_size"));
        if (kiekis == 0) {
            System.out.println("Vartotoju nera!");
            return;
        }

        System.out.print("Kuri vartotoja norite spausdinti: ");
        int id = 0;
        try {
            id = in.nextInt();
        } catch (InputMismatchException e) {
            System.err.println("Blogai nurodytas id!");
        } finally {
            in.nextLine();
        }

        String idString = String.valueOf(id);
        if (jedis.exists(idString)) {
            System.out.println(jedis.get(idString));
        } else {
            User user;
            try (Session session = factory.openSession()) {
                user = session.find(User.class, id);
            }
            if (user != null) {
                String text = user.toSimpleString();
                System.out.println(text);
                jedis.set(idString, text);
            } else {
                System.out.println("Tokio vartotojo nera!");
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