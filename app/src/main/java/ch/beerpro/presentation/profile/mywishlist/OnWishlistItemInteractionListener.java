package ch.beerpro.presentation.profile.mywishlist;

import android.widget.ImageView;
import ch.beerpro.domain.models.Beer;

public interface OnWishlistItemInteractionListener {

    void onMoreClickedListener(ImageView photo, Beer beer);

    void onWishClickedListener(Beer beer);

    void onFridgeItemClickedListener(Beer beer);
}
