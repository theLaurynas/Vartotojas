package lt.codeacademy.vartotojas;

import lombok.extern.java.Log;
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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.*;

@Log
public class Main {
    static final DateTimeFormatter ISO_LOCAL_DATE_TIME_NO_MILIS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final SessionFactory factory;
    static Scanner in = new Scanner(System.in);
    static JedisPool jedisPool = new JedisPool();
    static Jedis jedis = jedisPool.getResource();

    static {
        Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
        factory = new Configuration().configure().buildSessionFactory();
        Formatter formatter = new Formatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("[%s %-7s]: %s\n",
                        LocalTime.ofInstant(record.getInstant(), ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                        record.getLevel(),
                        record.getMessage());
            }
        };
        log.getParent().getHandlers()[0].setFormatter(formatter);
        try {
            Handler fileHandler = new FileHandler("Vartotojas.LOG", true);
            fileHandler.setFormatter(formatter);
            log.addHandler(fileHandler);
        } catch (IOException e) {
            log.severe("Nepavyko prideti FileHander i loggeri!");
        }
    }

    public static void main(String[] args) {

        try (Session session = factory.openSession()) {
            int userCount = session.createQuery("select count(*) FROM User", int.class).getFirstResult();
            jedis.set("vartotojai_size", String.valueOf(userCount));
            log.info("vartotojai irasyti i cache");
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
                case 2 -> modifikuotiVartotoja();
                case 3 -> trintiVartotoja();
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
        in.close();
        log.info("Programa baigia darba!");
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

        log.info("Vartotojas sukurtas.");
        jedis.del("vartotojai");
    }

    private static void modifikuotiVartotoja() {
        spausdintiVartotojus(false); // Galima nieko neisvesti jei daug vartotoju.
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

        try (Session session = factory.openSession()) {
            User user = session.find(User.class, keiciamasId);

            if (user != null) {
                System.out.print("""
                        1 - vardas
                        2 - slaptazodis
                        3 - email
                        4 - lytis
                        Kuri lauka norite keisti:\s""");
                String pasirinkimas = in.nextLine();

                switch (pasirinkimas) {
                    case "1" -> user.setVardas(Ivestis.vardoIvestis());
                    case "2" -> user.setSlaptazodis(Ivestis.slaptazodzioIvestis());
                    case "3" -> user.setEmail(Ivestis.emailIvestis());
                    case "4" -> user.setLytis(Ivestis.lytiesIvestis());
                    default -> {
                        log.warning("Blogas pasirinkimas!");
                        return;
                    }
                }
                Transaction tx = session.beginTransaction();
                session.merge(user);
                tx.commit();
                log.info("Vartotojas pakoreguotas.");
                jedis.del("vartotojai");
                jedis.del(String.valueOf(keiciamasId));
            }
        }
    }

    private static void trintiVartotoja() {
        spausdintiVartotojus(false); // Galima nieko neisvesti jei daug vartotoju.

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

        log.info("Vartotojas istrintas");
        String idString = String.valueOf(id);
        jedis.decr("vartotojai_size");
        jedis.del("vartotojai");
        jedis.del(idString);
    }

    private static void spausdintiVartotojus(boolean menu) {
        long kiekis = Integer.parseInt(jedis.get("vartotojai_size"));
        if (kiekis == 0) {
            log.warning("Vartotoju nera!");
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
                log.warning("Blogas pasirinkimas!");
                return;
            }
        }

        String text;
        if (jedis.exists("vartotojai")) {
            text = jedis.get("vartotojai");
            log.info("cache hit!");
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
            log.warning("Vartotoju nera!");
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
                log.warning("Tokio vartotojo nera!");
            }
        }
    }

    private static void issaugotiIFaila(String text) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss");
        String filename = "vartotojai_" + LocalDateTime.now().format(dtf) + ".txt";
        File file = new File(filename);

        try {
            Files.writeString(file.toPath(), text, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            log.info("Failas issaugotas vardu: " + filename);
        } catch (IOException e) {
            log.severe("Failo issaugoti nepavyko!");
        }
    }
}