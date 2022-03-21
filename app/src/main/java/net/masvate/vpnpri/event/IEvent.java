package net.masvate.vpnpri.event;

public class IEvent {

    public final Object[] message;

    public IEvent(Object... message) {
        this.message = message;
    }

    public Object[] getMessage() {
        return message;
    }
}
