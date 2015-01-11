package reactmann.subscribers;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import rx.Observable;
import rx.Subscriber;

public class BufferAction implements Observable.OnSubscribe<Buffer> {
    private int size = -1;
    private RecordParser recordParser;
    private final Observable<Buffer> observable;

    public BufferAction(Observable<Buffer> observable) {
        this.observable = observable;
    }

    @Override
    public void call(Subscriber<? super Buffer> subscriber) {
        observable.subscribe(buffer -> {
            if(recordParser == null) {
                recordParser = RecordParser.newFixed(1, b -> {
                    subscriber.onNext(b);
                    size = -1;
                });
            }

            if(size == -1) {
                size = buffer.getInt(0);
                recordParser.fixedSizeMode(size + 4);
            }

            recordParser.handle(buffer);
        });
    }
}
