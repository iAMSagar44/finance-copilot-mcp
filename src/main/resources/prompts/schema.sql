-- Table: financial_transaction
CREATE TABLE IF NOT EXISTS financial_transaction(
    amount double precision NOT NULL,
    "date" date NOT NULL,
    id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    category varchar(255) NOT NULL,
    transaction_detail varchar(255) NOT NULL,
    transaction_type varchar(255) NOT NULL DEFAULT 'DEBIT'::character varying,
    PRIMARY KEY(id)
);