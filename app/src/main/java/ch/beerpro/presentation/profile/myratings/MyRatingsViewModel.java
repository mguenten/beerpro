package ch.beerpro.presentation.profile.myratings;

import android.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ch.beerpro.data.repositories.CurrentUser;
import ch.beerpro.data.repositories.FridgeRepository;
import ch.beerpro.data.repositories.RatingsRepository;
import ch.beerpro.data.repositories.WishlistRepository;
import ch.beerpro.domain.models.FridgeItem;
import ch.beerpro.domain.models.Rating;
import ch.beerpro.domain.models.Wish;

import java.util.List;

public class MyRatingsViewModel extends ViewModel implements CurrentUser {

    private static final String TAG = "MyBeersViewModel";

    private final LiveData<List<Rating>> myRatings;
    private final LiveData<List<Wish>> myWishlist;
    private final LiveData<List<FridgeItem>> myFridge;
    private final WishlistRepository wishlistRepository;
    private final FridgeRepository fridgeRepository;
    private final RatingsRepository ratingsRepository;
    private final MutableLiveData<String> currentUserId;

    public MyRatingsViewModel() {
        fridgeRepository = new FridgeRepository();
        wishlistRepository = new WishlistRepository();
        ratingsRepository = new RatingsRepository();
        currentUserId = new MutableLiveData<>();
        myFridge = fridgeRepository.getMyFridge(currentUserId);
        myWishlist = wishlistRepository.getMyWishlist(currentUserId);
        myRatings = ratingsRepository.getMyRatings(currentUserId);

        currentUserId.setValue(getCurrentUser().getUid());
    }

    public LiveData<List<Rating>> getMyRatings() {
        return myRatings;
    }

    public void toggleItemInWishlist(String beerId) {
        wishlistRepository.toggleUserWishlistItem(getCurrentUser().getUid(), beerId);
    }

    public LiveData<List<Pair<Rating, Wish>>> getMyRatingsWithWishes() {
        return ratingsRepository.getMyRatingsWithWishes(currentUserId, getMyWishlist());
    }

    public LiveData<List<Wish>> getMyWishlist() {
        return myWishlist;
    }


    public LiveData<List<FridgeItem>> getMyFridge() {
        return myFridge;
    }
}