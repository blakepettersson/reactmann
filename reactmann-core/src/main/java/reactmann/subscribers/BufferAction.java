package reactmann.subscribers;

import io.vertx.core.buffer.Buffer;
import rx.Observable;
import rx.Subscriber;

public class BufferAction implements Observable.OnSubscribe<Buffer> {
    private RiemannParser recordParser;
    private final Observable<Buffer> observable;

    public BufferAction(Observable<Buffer> observable) {
        this.observable = observable;
    }

    @Override
    public void call(Subscriber<? super Buffer> subscriber) {
        observable.subscribe(buffer -> {
            if(recordParser == null) {
                recordParser = RiemannParser.newFixed(b -> {
                    subscriber.onNext(b);
                });
            }

            recordParser.handle(buffer);
        });
    }
}
