package lt.codeacademy.vartotojas;

public class Vartotojas {
    private static int idCounter = 0; // Ivestu vartotoju kiekis

    public static void setIdCounter(int idCounter) {
        Vartotojas.idCounter = idCounter;
    }

    public static int getAndIncrIdCounter() {
        return idCounter++;
    }

}
