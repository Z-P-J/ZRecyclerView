package androidx.recyclerview.widget;

import androidx.recyclerview.widget.ChildHelper;

public class PublicChildHelper extends ChildHelper {

    public PublicChildHelper(PublicCallback callback) {
        super(callback);
    }

    public interface PublicCallback extends Callback {

    }

}
