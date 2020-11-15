package sample;

class RetryAttemptsExhaustedException extends RuntimeException {
    public RetryAttemptsExhaustedException(String message) {
        super(message);
    }
}