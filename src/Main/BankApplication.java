package Main;

import java.util.List;
import java.util.Scanner;

import BankBean.BankTransactionBean;
import BankBean.BankUserBean;
import BankException.Insufficientbalance;
import BankException.InvalidAmount;
import DAO.BankDAO;

public class BankApplication {

    private static final Scanner scanner = new Scanner(System.in);
    private static final BankDAO dao = new BankDAO();

    public static void main(String[] args) {

        boolean running = true;

        while (running) {
            System.out.println("\n==================================");
            System.out.println("       WELCOME TO SBI BANK");
            System.out.println("==================================");
            System.out.println("1. Existing User");
            System.out.println("2. New User");
            System.out.println("3. Exit");

            int choice = readInt("Enter your choice: ");

            switch (choice) {
            case 1:
                loginUser();
                break;

            case 2:
                createNewAccount();
                break;

            case 3:
                running = false;
                System.out.println("Thank you for using SBI Bank.");
                break;

            default:
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void createNewAccount() {
        System.out.println("\n------ CREATE ACCOUNT ------");

        BankUserBean user = new BankUserBean();

        user.setName(readNonEmpty("Full Name: "));
        user.setAge(readAge());
        user.setPhoneNo(readPhoneNumber());
        user.setEmail(readEmail());
        user.setAdharNo(readAadhaarNumber());
        user.setAddress(readNonEmpty("Address: "));
        user.setUsername(readNonEmpty("Username: "));
        user.setPassword(readPassword("Password: "));
        user.setAccountBalance(readPositiveAmount("Initial Deposit: "));

        int accountNo = dao.createAccount(user);

        if (accountNo > 0) {
            System.out.println("\nAccount Created Successfully!");
            System.out.println("Your Account Number: " + accountNo);
        } else {
            System.out.println("\nAccount creation failed.");
            System.out.println("Username, email, or Aadhaar number may already exist.");
        }
    }

    private static void loginUser() {
        System.out.println("\n------ LOGIN ------");

        String username = readNonEmpty("Username: ");
        String password = readNonEmpty("Password: ");

        BankUserBean user = dao.login(username, password);

        if (user == null) {
            System.out.println("Invalid username or password.");
            return;
        }

        System.out.println("\nLogin successful. Welcome, " + user.getName() + "!");
        bankMenu(user);
    }

    private static void bankMenu(BankUserBean user) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n==========================");
            System.out.println("      SBI BANK MAIN MENU");
            System.out.println("==========================");
            System.out.println("1. View Balance");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Send Money");
            System.out.println("5. Transaction History");
            System.out.println("6. Change Password");
            System.out.println("7. View Profile");
            System.out.println("8. Logout");

            int choice = readInt("Enter your choice: ");

            switch (choice) {
            case 1:
                viewBalance(user);
                break;

            case 2:
                depositMoney(user);
                break;

            case 3:
                withdrawMoney(user);
                break;

            case 4:
                sendMoney(user);
                break;

            case 5:
                showTransactionHistory(user);
                break;

            case 6:
                changePassword(user);
                break;

            case 7:
                viewProfile(user);
                break;

            case 8:
                loggedIn = false;
                System.out.println("Logged out successfully.");
                break;

            default:
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewBalance(BankUserBean user) {
        double balance = dao.checkBalance(user.getAccountNo());

        if (balance >= 0) {
            System.out.printf("Current Balance: Rs. %.2f%n", balance);
        } else {
            System.out.println("Unable to fetch balance.");
        }
    }

    private static void depositMoney(BankUserBean user) {
        double amount = readPositiveAmount("Enter deposit amount: ");

        try {
            boolean result = dao.depositAmount(user.getAccountNo(), amount);

            if (result) {
                System.out.println("Money deposited successfully.");
            } else {
                System.out.println("Deposit failed.");
            }

        } catch (InvalidAmount e) {
            System.out.println(e.getMessage());
        }
    }

    private static void withdrawMoney(BankUserBean user) {
        double amount = readPositiveAmount("Enter withdrawal amount: ");

        try {
            boolean result = dao.withdrawAmount(user.getAccountNo(), amount);

            if (result) {
                System.out.println("Money withdrawn successfully.");
            }

        } catch (InvalidAmount | Insufficientbalance e) {
            System.out.println(e.getMessage());
        }
    }

    private static void sendMoney(BankUserBean user) {
        int receiverAccountNo = readInt("Receiver Account Number: ");

        BankUserBean receiver = dao.viewProfile(receiverAccountNo);

        if (receiver == null) {
            System.out.println("Receiver account does not exist.");
            return;
        }

        double amount = readPositiveAmount("Enter transfer amount: ");

        try {
            boolean result = dao.sendMoney(
                    user.getAccountNo(), receiverAccountNo, amount);

            if (result) {
                System.out.println("Money transferred successfully.");
            } else {
                System.out.println("Transfer failed.");
            }

        } catch (InvalidAmount | Insufficientbalance e) {
            System.out.println(e.getMessage());
        }
    }

    private static void showTransactionHistory(BankUserBean user) {
        List<BankTransactionBean> transactions =
                dao.getTransactionHistory(user.getAccountNo());

        System.out.println("\n------ TRANSACTION HISTORY ------");

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        for (BankTransactionBean transaction : transactions) {
            System.out.println("Date: " + transaction.getTransactionDate());
            System.out.println("Type: " + transaction.getTraType());
            System.out.printf("Amount: Rs. %.2f%n", transaction.getAmount());

            if (transaction.getReceiverAccountNo() != null) {
                System.out.println("Related Account: "
                        + transaction.getReceiverAccountNo());
            }

            System.out.println("Details: " + transaction.getDescription());
            System.out.println("----------------------------------");
        }
    }

    private static void changePassword(BankUserBean user) {
        String currentPassword = readNonEmpty("Current Password: ");
        String newPassword = readPassword("New Password: ");
        String confirmPassword = readNonEmpty("Confirm New Password: ");

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("New password and confirmation do not match.");
            return;
        }

        boolean result = dao.changePassword(
                user.getAccountNo(), currentPassword, newPassword);

        if (result) {
            user.setPassword(newPassword);
            System.out.println("Password changed successfully.");
        } else {
            System.out.println("Current password is incorrect.");
        }
    }

    private static void viewProfile(BankUserBean user) {
        BankUserBean profile = dao.viewProfile(user.getAccountNo());

        if (profile == null) {
            System.out.println("Profile not found.");
            return;
        }

        System.out.println("\n------ PROFILE ------");
        System.out.println("Name: " + profile.getName());
        System.out.println("Account Number: " + profile.getAccountNo());
        System.out.println("Mobile: " + profile.getPhoneNo());
        System.out.println("Email: " + profile.getEmail());
        System.out.println("Aadhaar Number: " + profile.getAdharNo());
        System.out.println("Address: " + profile.getAddress());
        System.out.printf("Current Balance: Rs. %.2f%n",
                profile.getAccountBalance());
    }

    private static String readNonEmpty(String message) {
        while (true) {
            System.out.print(message);
            String value = scanner.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("This field cannot be empty.");
        }
    }

    private static int readInt(String message) {
        while (true) {
            try {
                System.out.print(message);
                return Integer.parseInt(scanner.nextLine().trim());

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static int readAge() {
        while (true) {
            int age = readInt("Age: ");

            if (age >= 18 && age <= 120) {
                return age;
            }

            System.out.println("Age must be between 18 and 120.");
        }
    }

    private static String readPhoneNumber() {
        while (true) {
            String phoneNumber = readNonEmpty("Mobile Number: ");

            if (phoneNumber.matches("\\d{10,15}")) {
                return phoneNumber;
            }

            System.out.println("Enter a valid mobile number.");
        }
    }

    private static String readEmail() {
        while (true) {
            String email = readNonEmpty("Email: ");

            if (email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                return email;
            }

            System.out.println("Enter a valid email address.");
        }
    }

    private static Long readAadhaarNumber() {
        while (true) {
            String aadhaar = readNonEmpty("Aadhaar Number: ");

            if (aadhaar.matches("\\d{12}")) {
                return Long.parseLong(aadhaar);
            }

            System.out.println("Aadhaar number must contain 12 digits.");
        }
    }

    private static double readPositiveAmount(String message) {
        while (true) {
            try {
                System.out.print(message);
                double amount = Double.parseDouble(scanner.nextLine().trim());

                if (amount > 0) {
                    return amount;
                }

                System.out.println("Amount must be greater than zero.");

            } catch (NumberFormatException e) {
                System.out.println("Enter a valid amount.");
            }
        }
    }

    private static String readPassword(String message) {
        while (true) {
            String password = readNonEmpty(message);

            if (password.length() >= 4) {
                return password;
            }

            System.out.println("Password must contain at least 4 characters.");
        }
    }
}