DROP DATABASE IF EXISTS BankingManagementSystem;

CREATE DATABASE BankingManagementSystem;

USE BankingManagementSystem;

CREATE TABLE bankuser (
    accountno INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    phoneno VARCHAR(15) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    adharno BIGINT NOT NULL UNIQUE,
    address VARCHAR(250) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    accountbalance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    createdat TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE bankuser AUTO_INCREMENT = 1000001;

CREATE TABLE banktransaction (
    transactionid INT AUTO_INCREMENT PRIMARY KEY,
    accountno INT NOT NULL,
    tratype VARCHAR(30) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    receiveraccountno INT,
    description VARCHAR(200),
    transactiondate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (accountno) REFERENCES bankuser(accountno)
);