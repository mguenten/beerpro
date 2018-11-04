package ch.beerpro.presentation.profile.mybeers;

import android.widget.ImageView;
import ch.beerpro.domain.models.Beer;

public interface OnMyBeerItemInteractionListener {

    void onMoreClickedListener(ImageView photo, Beer item);

    void onWishClickedListener(Beer item);

    void onFridgeItemClickedListener(Beer item);
}
