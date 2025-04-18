-- Table: credit_card_payments. This table contains transactions specific to monthly Credit card payments only.
CREATE TABLE IF NOT EXISTS credit_card_payments(
    id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    "date" date NOT NULL,
    amount double precision NOT NULL,
    transaction_detail varchar(255) NOT NULL,
    PRIMARY KEY(id)
);

-- Table: financial_transaction. This table contains both debit and credit transactions from a user's bank account.
CREATE TABLE IF NOT EXISTS financial_transaction(
    amount double precision NOT NULL,
    "date" date NOT NULL,
    id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    category varchar(255) NOT NULL,
    transaction_detail varchar(255) NOT NULL,
    transaction_type varchar(255) NOT NULL DEFAULT 'DEBIT'::character varying,
    PRIMARY KEY(id)
);