package lt.codeacademy.vartotojas;

import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Ivestis {
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static Scanner in = Main.in;

    public static String vardoIvestis() {
        String vardas;
        do {
            System.out.print("Iveskite varda: ");
            vardas = in.nextLine();
        } while (!isNameValid(vardas));
        return vardas;
    }

    public static String slaptazodzioIvestis() {
        String slaptazodis;
        String slaptazodis2;

        do {
            System.out.print("Iveskite slaptazodi: ");
            slaptazodis = in.nextLine();

            System.out.print("Iveskite slaptazodi(dar karta): ");
            slaptazodis2 = in.nextLine();
        } while (!isPassValid(slaptazodis, slaptazodis2));
        slaptazodis = DigestUtils.sha1Hex(slaptazodis);
        return slaptazodis;
    }

    public static String emailIvestis() {
        String email;
        do {
            System.out.print("Iveskite email: ");
            email = in.nextLine();
        } while (!isEmailValid(email));
        return email;
    }

    public static String lytiesIvestis() {
        System.out.print("Iveskite lyti: ");
        String lytis = in.nextLine().toUpperCase();
        return lytis.equals("VYRAS") || lytis.equals("MOTERIS") ? lytis : "NEZINOMA";
    }

    public static LocalDate gimimoDatosIvestis() {
        String gimimoDataString;

        do {
            System.out.print("Iveskite gimimo data(yyyy-MM-dd): ");
            gimimoDataString = in.next();
        } while (!isDateOfBirthValid(gimimoDataString));

        return LocalDate.parse(gimimoDataString, DATE_FORMATTER);
    }

    private static boolean isNameValid(String vardas) {

        if (vardas.length() < 3) {
            System.out.println("Vartotojo vardas per trumpas!");
            return false;
        }

        if (vardas.length() > 15) {
            System.out.println("Vartotojo vardas per ilgas!");
            return false;
        }

        if (vardas.matches(".*[^A-Za-z].*")) {
            System.out.println("Vardas turi netinkamų simbolių!");
            return false;
        }

        if (!vardas.matches("[A-Z][a-z]+")) {
            System.out.println("Vardas privalo prasidėti didžiąją raide ir toliau būti mažosiomis");
            return false;
        }

        return true;
    }

    private static boolean isPassValid(String slaptazodis, String slaptazodis2) {

        if (!slaptazodis.equals(slaptazodis2)) {
            System.out.println("Slaptazodziai nesutampa!");
            return false;
        }

        if (slaptazodis.length() < 5) {
            System.out.println("Slaptažodis per trumpas!");
            return false;
        }

        if (slaptazodis.matches(".*\\W.*")) {
            System.out.println("Slaptažodis turi neleidžiamų simbolių");
            return false;
        }

        if (!slaptazodis.matches(".*[a-z].*") || !slaptazodis.matches(".*[A-Z].*") || !slaptazodis.matches(".*\\d.*")) {
            System.out.println("Slaptažodis privalo turėti bent vieną didžiają raidę, mažąją raidę ir skaičių");
            return false;
        }

        return true;
    }

    private static boolean isEmailValid(String email) {

        if (email.contains(" ")) {
            System.out.println("El. pastas negali turetu tarpu!");
            return false;
        }

        if (!email.matches("[a-z\\d_.]+@[a-z\\d]+\\.[a-z]{2,10}")) {
            System.out.println("Neteisingas email formatas!");
            return false;
        }

        return true;
    }

    private static boolean isDateOfBirthValid(String dateOfBirthString) {
        LocalDate gimimoData;
        try {
            gimimoData = LocalDate.parse(dateOfBirthString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("Blogas datos formatas!");
            return false;
        }
        if (gimimoData.isAfter(LocalDate.now().minusYears(18))) {
            System.out.println("Tau nera 18 metu!");
            return false;
        }

        return true;
    }
}
