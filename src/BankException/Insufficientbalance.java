package BankException;

public class Insufficientbalance extends Exception {

    private static final long serialVersionUID = 1L;

    public Insufficientbalance(String message) {
        super(message);
    }
}