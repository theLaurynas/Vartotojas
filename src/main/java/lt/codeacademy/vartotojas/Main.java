package lt.codeacademy.vartotojas;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.java.Log;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.codec.digest.DigestUtils;
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
import java.util.Map;
import java.util.Scanner;
import java.util.logging.*;

@Log
public class Main {
    static final DateTimeFormatter ISO_LOCAL_DATE_TIME_NO_MILIS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final SessionFactory factory;
    static Scanner in = new Scanner(System.in);
    static JedisPool jedisPool = new JedisPool();
    static Jedis jedis = jedisPool.getResource();

    static Map<String, String> locale;

    static {
        Logger hibernate_log = Logger.getLogger("org.hibernate");
        hibernate_log.setLevel(Level.WARNING);
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
        hibernate_log.getParent().getHandlers()[0].setFormatter(formatter);

        log.getParent().getHandlers()[0].setFormatter(formatter);
        log.setLevel(Level.WARNING);
        try {
            Handler fileHandler = new FileHandler("Vartotojas.LOG", true);
            fileHandler.setFormatter(formatter);
            log.addHandler(fileHandler);
        } catch (IOException e) {
            log.severe("Nepavyko prideti FileHander i loggeri!");
        }
    }

    public static void main(String[] args) throws Exception {

        ArgumentParser parser = ArgumentParsers.newFor("Vartotojas").build();
        parser.defaultHelp(true).description("Vartotoju ir postu programa");
        parser.addArgument("-v", "--verbose").action(Arguments.storeTrue())
                .dest("debug").help("Print debug info.");
        parser.addArgument("-l", "--lang")
                .dest("lang").help("Sets meniu language");
        Namespace ns = parser.parseArgs(args);
        boolean debug = ns.getBoolean("debug");

        String lang = ns.getString("lang");

        if (debug) {
            log.setLevel(Level.ALL);
            log.getParent().getHandlers()[0].setLevel(Level.ALL);
        }


        try (Session session = factory.openSession()) {
            int userCount = session.createQuery("select count(*) FROM User", int.class).getFirstResult();
            jedis.set("vartotojai_size", String.valueOf(userCount));
            log.info("vartotojai irasyti i cache");
        }

        var locale_lt = Map.of(
                "user.created", "Vartotojas sukurtas"
        );

        var locale_en = Map.of(
                "user.created", "User created"
        );

        if (lang.equals("lt"))
            locale = locale_lt;
        else if (lang.equals("en"))
            locale = locale_en;


        int pasirinkimas;

        String menu_lt = """
                                    
                ┌───────────────────────────────────┐
                │               MENIU               │
                ├───────────────────────────────────┤
                │ 1 - Ivesti vartotoja              │
                │ 2 - Pakeisti esama vartotoja      │
                │ 3 - Trinti vartotoja              │
                │ 4 - Atspausdinti vartotojus       │
                │ 5 - Atspausdinti viena vartotoja  │
                │ 6 - Sukurti nauja posta           │
                │ 7 - Atspausdinti vartotojo postus │
                │ 8 - Baigti programa               │
                └───────────────────────────────────┘
                  Jusu pasirinkimas:\s""";

        String menu_en = """
                                    
                ┌───────────────────────────────────┐
                │               MENU                │
                ├───────────────────────────────────┤
                │ 1 - Insert user                   │
                │ 2 - Update user                   │
                │ 3 - Delete user                   │
                │ 4 - Print all users               │
                │ 5 - Print one user                │
                │ 6 - Create new post               │
                │ 7 - Print all users posts         │
                │ 8 - Exit program                  │
                └───────────────────────────────────┘
                  Jusu pasirinkimas:\s""";


        menu:
        while (true) {
            if (lang.equals("en"))
                System.out.print(menu_en);

            else if (lang.equals("lt"))
                System.out.print(menu_lt);
            else
                System.exit(1);

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
                case 6 -> sukurtiPosta();
                case 7 -> spausdintiPostus();
                case 8 -> {
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

    private static void spausdintiPostus() {
        spausdintiVartotojus(false); // Galima nieko neisvesti jei daug vartotoju.

        System.out.print("Kurio vartotojo postus norite matyti: ");
        int id;
        try {
            id = in.nextInt();
        } catch (InputMismatchException e) {
            log.warning("Blogai nurodytas id!");
            return;
        } finally {
            in.nextLine();
        }

        try (Session session = factory.openSession()) {
            User user = session.find(User.class, id);
            if (user == null) {
                log.warning("Vartotojas nerastas");
                return;
            }

            List<Post> posts = user.getPosts();
            if (!posts.isEmpty()) {
                posts.stream().map(post -> "\t- " + post.getPavadinimas() + " | " + post.getTekstas())
                        .forEach(System.out::println);
            } else {
                log.warning("Vartotojas neturi nei vieno posto!");
            }
        }
    }

    private static void sukurtiPosta() {
        System.out.print("Iveskite email: ");
        String email = in.nextLine();
        System.out.print("Iveskite password: ");
        String password = DigestUtils.sha1Hex(in.nextLine());

        try (Session session = factory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> root = cq.from(User.class);
            cq.select(root)
                    .where(cb.and(
                            cb.equal(root.get("email"), email),
                            cb.equal(root.get("slaptazodis"), password)
                    ));
            List<User> users = session.createQuery(cq).getResultList();
            if (!users.isEmpty()) {
                User user = users.get(0);
                System.out.print("Iveskite posto pavadinima: ");
                String pavadinimas = in.nextLine();
                System.out.print("Iveskite posto teksta: ");
                String tekstas = in.nextLine();

                Post post = new Post(pavadinimas, tekstas, user);

                user.getPosts().add(post);

                Transaction tx = session.beginTransaction();
                session.merge(user);
                tx.commit();
                log.info("Postas sukurtas");
            } else {
                log.warning("Neteisingas email ir/arba slaptazodis!");
            }
        }
    }

    private static void ivestiVartotoja() {
        /*String vardas = vardoIvestis();
        String slaptazodis = slaptazodzioIvestis();
        String email = emailIvestis();
        String lytis = lytiesIvestis();
        LocalDate gimimoData = gimimoDatosIvestis();*/

        String vardas = "Jonas";
        String slaptazodis = "54e8d2e15d3caa89aa3f82c8c0428ad5742f056c";
        String email = "jonas@gmail.com";
        String lytis = "vyras";
        LocalDate gimimoData = LocalDate.of(2000, 1, 1);

        User user = new User(vardas, slaptazodis, email, lytis, gimimoData, LocalDateTime.now());

        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(user);
            tx.commit();
        }

        int id = user.getId();

        jedis.incr("vartotojai_size");

        log.info(locale.get("user.created"));
        jedis.del("vartotojai");
    }

    private static void modifikuotiVartotoja() {
        spausdintiVartotojus(false); // Galima nieko neisvesti jei daug vartotoju.
        System.out.print("Kuri vartotoja norite keisti: ");
        int keiciamasId;
        try {
            keiciamasId = in.nextInt();
        } catch (InputMismatchException e) {
            log.warning("Blogai nurodytas id!");
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
            log.warning("Blogai nurodytas id!");
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
            log.warning("Blogai nurodytas id!");
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