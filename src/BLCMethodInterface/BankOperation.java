package BLCMethodInterface;

import java.util.List;

import BankBean.BankTransactionBean;
import BankBean.BankUserBean;
import BankException.Insufficientbalance;
import BankException.InvalidAmount;

public interface BankOperation {

    int createAccount(BankUserBean user);

    BankUserBean login(String username, String password);

    double checkBalance(int accountNo);

    boolean depositAmount(int accountNo, double amount)
            throws InvalidAmount;

    boolean withdrawAmount(int accountNo, double amount)
            throws InvalidAmount, Insufficientbalance;

    boolean sendMoney(int senderAccountNo, int receiverAccountNo, double amount)
            throws InvalidAmount, Insufficientbalance;

    List<BankTransactionBean> getTransactionHistory(int accountNo);

    boolean changePassword(int accountNo, String currentPassword, String newPassword);

    BankUserBean viewProfile(int accountNo);
}