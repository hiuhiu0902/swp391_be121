package fu.se.myplatform.enums;

public enum TransactionMethod {
    VNPAY("VNPay"),
    MOMO("MoMo");

    private final String method;

    TransactionMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return method;
    }
}
