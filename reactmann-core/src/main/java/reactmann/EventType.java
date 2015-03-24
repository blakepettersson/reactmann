package reactmann;

/**
 * Created by blake on 08/03/15.
 */
public enum EventType {
    INDEX("riemann.index"),
    STREAM("riemann.stream");

    private final String address;

    private EventType(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
