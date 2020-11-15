package sample;

import java.util.UUID;

class Payment {
    UUID id;
    Long accountId;
    Long amount;

    public Payment(UUID id, Long accountId, Long amount) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
    }
}