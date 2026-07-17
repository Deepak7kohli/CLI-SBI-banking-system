package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import BLCMethodInterface.BankOperation;
import BankBean.BankTransactionBean;
import BankBean.BankUserBean;
import BankException.Insufficientbalance;
import BankException.InvalidAmount;
import DatabaseDetails.createConnection;

public class BankDAO implements BankOperation {

    @Override
    public int createAccount(BankUserBean user) {
        Connection con = null;

        try {
            con = createConnection.getCon();
            con.setAutoCommit(false);

            String sql = "insert into bankuser "
                    + "(name, age, phoneno, email, adharno, address, username, password, accountbalance) "
                    + "values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, user.getName());
            ps.setInt(2, user.getAge());
            ps.setString(3, user.getPhoneNo());
            ps.setString(4, user.getEmail());
            ps.setLong(5, user.getAdharNo());
            ps.setString(6, user.getAddress());
            ps.setString(7, user.getUsername());
            ps.setString(8, user.getPassword());
            ps.setDouble(9, user.getAccountBalance());

            int result = ps.executeUpdate();

            if (result > 0) {
                ResultSet rs = ps.getGeneratedKeys();

                if (rs.next()) {
                    int accountNo = rs.getInt(1);

                    recordTransaction(con, accountNo, "ACCOUNT_CREATION",
                            user.getAccountBalance(), null,
                            "Account created with initial deposit");

                    con.commit();
                    return accountNo;
                }
            }

            con.rollback();

        } catch (Exception e) {
            rollback(con);

        } finally {
            resetAutoCommit(con);
        }

        return 0;
    }

    @Override
    public BankUserBean login(String username, String password) {
        try {
            Connection con = createConnection.getCon();

            PreparedStatement ps = con.prepareStatement(
                    "select * from bankuser where username = ? and password = ?");

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return getUserFromResultSet(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public double checkBalance(int accountNo) {
        try {
            Connection con = createConnection.getCon();

            PreparedStatement ps = con.prepareStatement(
                    "select accountbalance from bankuser where accountno = ?");

            ps.setInt(1, accountNo);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("accountbalance");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public boolean depositAmount(int accountNo, double amount)
            throws InvalidAmount {

        if (amount <= 0) {
            throw new InvalidAmount("Deposit amount must be greater than zero.");
        }

        Connection con = null;

        try {
            con = createConnection.getCon();
            con.setAutoCommit(false);

            PreparedStatement ps = con.prepareStatement(
                    "update bankuser set accountbalance = accountbalance + ? "
                    + "where accountno = ?");

            ps.setDouble(1, amount);
            ps.setInt(2, accountNo);

            int result = ps.executeUpdate();

            if (result == 0) {
                con.rollback();
                return false;
            }

            recordTransaction(con, accountNo, "DEPOSIT", amount,
                    null, "Money deposited");

            con.commit();
            return true;

        } catch (SQLException e) {
            rollback(con);

        } finally {
            resetAutoCommit(con);
        }

        return false;
    }

    @Override
    public boolean withdrawAmount(int accountNo, double amount)
            throws InvalidAmount, Insufficientbalance {

        if (amount <= 0) {
            throw new InvalidAmount("Withdrawal amount must be greater than zero.");
        }

        Connection con = null;

        try {
            con = createConnection.getCon();
            con.setAutoCommit(false);

            PreparedStatement ps = con.prepareStatement(
                    "update bankuser set accountbalance = accountbalance - ? "
                    + "where accountno = ? and accountbalance >= ?");

            ps.setDouble(1, amount);
            ps.setInt(2, accountNo);
            ps.setDouble(3, amount);

            int result = ps.executeUpdate();

            if (result == 0) {
                con.rollback();
                throw new Insufficientbalance("Insufficient account balance.");
            }

            recordTransaction(con, accountNo, "WITHDRAWAL", amount,
                    null, "Money withdrawn");

            con.commit();
            return true;

        } catch (SQLException e) {
            rollback(con);

        } finally {
            resetAutoCommit(con);
        }

        return false;
    }

    @Override
    public boolean sendMoney(int senderAccountNo, int receiverAccountNo,
            double amount) throws InvalidAmount, Insufficientbalance {

        if (amount <= 0) {
            throw new InvalidAmount("Transfer amount must be greater than zero.");
        }

        if (senderAccountNo == receiverAccountNo) {
            throw new InvalidAmount("Sender and receiver accounts cannot be the same.");
        }

        Connection con = null;

        try {
            con = createConnection.getCon();
            con.setAutoCommit(false);

            if (!accountExists(con, receiverAccountNo)) {
                con.rollback();
                return false;
            }

            PreparedStatement senderPs = con.prepareStatement(
                    "update bankuser set accountbalance = accountbalance - ? "
                    + "where accountno = ? and accountbalance >= ?");

            senderPs.setDouble(1, amount);
            senderPs.setInt(2, senderAccountNo);
            senderPs.setDouble(3, amount);

            int senderResult = senderPs.executeUpdate();

            if (senderResult == 0) {
                con.rollback();
                throw new Insufficientbalance("Insufficient account balance.");
            }

            PreparedStatement receiverPs = con.prepareStatement(
                    "update bankuser set accountbalance = accountbalance + ? "
                    + "where accountno = ?");

            receiverPs.setDouble(1, amount);
            receiverPs.setInt(2, receiverAccountNo);
            receiverPs.executeUpdate();

            recordTransaction(con, senderAccountNo, "TRANSFER_SENT",
                    amount, receiverAccountNo, "Money sent");

            recordTransaction(con, receiverAccountNo, "TRANSFER_RECEIVED",
                    amount, senderAccountNo, "Money received");

            con.commit();
            return true;

        } catch (SQLException e) {
            rollback(con);

        } finally {
            resetAutoCommit(con);
        }

        return false;
    }

    @Override
    public List<BankTransactionBean> getTransactionHistory(int accountNo) {
        List<BankTransactionBean> transactions = new ArrayList<>();

        try {
            Connection con = createConnection.getCon();

            PreparedStatement ps = con.prepareStatement(
                    "select * from banktransaction where accountno = ? "
                    + "order by transactiondate, transactionid");

            ps.setInt(1, accountNo);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                BankTransactionBean transaction = new BankTransactionBean();

                transaction.setTransactionId(rs.getInt("transactionid"));
                transaction.setAccountNo(rs.getInt("accountno"));
                transaction.setTraType(rs.getString("tratype"));
                transaction.setAmount(rs.getDouble("amount"));

                int receiverAccountNo = rs.getInt("receiveraccountno");
                if (!rs.wasNull()) {
                    transaction.setReceiverAccountNo(receiverAccountNo);
                }

                transaction.setDescription(rs.getString("description"));
                transaction.setTransactionDate(
                        rs.getTimestamp("transactiondate"));

                transactions.add(transaction);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return transactions;
    }

    @Override
    public boolean changePassword(int accountNo, String currentPassword,
            String newPassword) {

        try {
            Connection con = createConnection.getCon();

            PreparedStatement ps = con.prepareStatement(
                    "update bankuser set password = ? "
                    + "where accountno = ? and password = ?");

            ps.setString(1, newPassword);
            ps.setInt(2, accountNo);
            ps.setString(3, currentPassword);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public BankUserBean viewProfile(int accountNo) {
        try {
            Connection con = createConnection.getCon();

            PreparedStatement ps = con.prepareStatement(
                    "select * from bankuser where accountno = ?");

            ps.setInt(1, accountNo);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return getUserFromResultSet(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean accountExists(Connection con, int accountNo)
            throws SQLException {

        PreparedStatement ps = con.prepareStatement(
                "select accountno from bankuser where accountno = ?");

        ps.setInt(1, accountNo);

        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    private void recordTransaction(Connection con, int accountNo,
            String transactionType, double amount,
            Integer receiverAccountNo, String description)
            throws SQLException {

        PreparedStatement ps = con.prepareStatement(
                "insert into banktransaction "
                + "(accountno, tratype, amount, receiveraccountno, description) "
                + "values (?, ?, ?, ?, ?)");

        ps.setInt(1, accountNo);
        ps.setString(2, transactionType);
        ps.setDouble(3, amount);

        if (receiverAccountNo == null) {
            ps.setNull(4, java.sql.Types.INTEGER);
        } else {
            ps.setInt(4, receiverAccountNo);
        }

        ps.setString(5, description);
        ps.executeUpdate();
    }

    private BankUserBean getUserFromResultSet(ResultSet rs)
            throws SQLException {

        BankUserBean user = new BankUserBean();

        user.setAccountNo(rs.getInt("accountno"));
        user.setName(rs.getString("name"));
        user.setAge(rs.getInt("age"));
        user.setPhoneNo(rs.getString("phoneno"));
        user.setEmail(rs.getString("email"));
        user.setAdharNo(rs.getLong("adharno"));
        user.setAddress(rs.getString("address"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setAccountBalance(rs.getDouble("accountbalance"));

        return user;
    }

    private void rollback(Connection con) {
        try {
            if (con != null) {
                con.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetAutoCommit(Connection con) {
        try {
            if (con != null) {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}