package android.support.v7.widget;

public class PublicChildHelper extends ChildHelper {

    public PublicChildHelper(PublicCallback callback) {
        super(callback);
    }

    public interface PublicCallback extends Callback {

    }

}
