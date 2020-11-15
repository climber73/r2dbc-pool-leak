CREATE TABLE IF NOT EXISTS accounts (
    id              bigint      PRIMARY KEY,
    balance         bigint      NOT NULL
);

CREATE TABLE IF NOT EXISTS payments (
    id              UUID        PRIMARY KEY,
    account_id      bigint      NOT NULL,
    amount          bigint      NOT NULL CHECK (amount >= 0)
);

CREATE INDEX payments_account_id_idx ON payments (account_id);

ALTER TABLE payments ADD FOREIGN KEY (account_id) REFERENCES accounts;

INSERT INTO accounts VALUES (1, 1000000000);