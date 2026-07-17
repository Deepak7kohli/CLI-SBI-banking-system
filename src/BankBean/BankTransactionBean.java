package BankBean;

import java.io.Serializable;
import java.sql.Timestamp;

public class BankTransactionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer transactionId;
    private Integer accountNo;
    private String traType;
    private Double amount;
    private Integer receiverAccountNo;
    private String description;
    private Timestamp transactionDate;

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(Integer accountNo) {
        this.accountNo = accountNo;
    }

    public String getTraType() {
        return traType;
    }

    public void setTraType(String traType) {
        this.traType = traType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getReceiverAccountNo() {
        return receiverAccountNo;
    }

    public void setReceiverAccountNo(Integer receiverAccountNo) {
        this.receiverAccountNo = receiverAccountNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Timestamp transactionDate) {
        this.transactionDate = transactionDate;
    }
}