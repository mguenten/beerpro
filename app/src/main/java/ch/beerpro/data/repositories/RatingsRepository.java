package ch.beerpro.data.repositories;

import android.util.Log;
import android.util.Pair;
import androidx.lifecycle.LiveData;
import ch.beerpro.domain.models.FridgeItem;
import ch.beerpro.domain.models.Rating;
import ch.beerpro.domain.models.Wish;
import ch.beerpro.domain.utils.FirestoreQueryLiveDataArray;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static androidx.lifecycle.Transformations.map;
import static androidx.lifecycle.Transformations.switchMap;
import static ch.beerpro.domain.utils.LiveDataExtensions.combineLatest;

public class RatingsRepository {

    private final FirestoreQueryLiveDataArray<Rating> allRatings = new FirestoreQueryLiveDataArray<>(
            FirebaseFirestore.getInstance().collection(Rating.COLLECTION)
                    .orderBy(Rating.FIELD_CREATION_DATE, Query.Direction.DESCENDING), Rating.class);

    public static LiveData<List<Rating>> getRatingsByUser(String userId) {
        return new FirestoreQueryLiveDataArray<>(FirebaseFirestore.getInstance().collection(Rating.COLLECTION)
                .orderBy(Rating.FIELD_CREATION_DATE, Query.Direction.DESCENDING)
                .whereEqualTo(Rating.FIELD_USER_ID, userId), Rating.class);
    }

    public static LiveData<List<Rating>> getRatingsByBeer(String beerId) {
        return new FirestoreQueryLiveDataArray<>(FirebaseFirestore.getInstance().collection(Rating.COLLECTION)
                .orderBy(Rating.FIELD_CREATION_DATE, Query.Direction.DESCENDING)
                .whereEqualTo(Rating.FIELD_BEER_ID, beerId), Rating.class);
    }

    public LiveData<List<Pair<Rating, Wish>>> getAllRatingsWithWishes(LiveData<List<Wish>> myWishlist) {
        return map(combineLatest(getAllRatings(), map(myWishlist, entries -> {
            HashMap<String, Wish> byId = new HashMap<>();
            for (Wish entry : entries) {
                byId.put(entry.getBeerId(), entry);
            }
            return byId;
        })), input -> {
            List<Rating> ratings = input.first;
            HashMap<String, Wish> wishesByItem = input.second;

            ArrayList<Pair<Rating, Wish>> result = new ArrayList<>();
            for (Rating rating : ratings) {
                Wish wish = wishesByItem.get(rating.getBeerId());
                result.add(Pair.create(rating, wish));
            }
            return result;
        });
    }

    public LiveData<List<Pair<Rating, FridgeItem>>> getAllRatingsWithFridge(LiveData<List<FridgeItem>> myFridge) {
        return map(combineLatest(getAllRatings(), map(myFridge, entries -> {
            HashMap<String, FridgeItem> byId = new HashMap<>();
            for (FridgeItem entry : entries) {
                byId.put(entry.getBeerId(), entry);
            }
            return byId;
        })), input -> {
            List<Rating> ratings = input.first;
            HashMap<String, FridgeItem> fridgeByItem = input.second;

            ArrayList<Pair<Rating, FridgeItem>> result = new ArrayList<>();
            for (Rating rating : ratings) {
                FridgeItem fridgeItem = fridgeByItem.get(rating.getBeerId());
                result.add(Pair.create(rating, fridgeItem));
            }
            return result;
        });
    }

    public LiveData<List<Rating>> getAllRatings() {
        return allRatings;
    }

    public LiveData<List<Rating>> getRatingsForBeer(LiveData<String> beerId) {
        return switchMap(beerId, RatingsRepository::getRatingsByBeer);
    }

    public LiveData<List<Pair<Rating, Wish>>> getMyRatingsWithWishes(LiveData<String> currentUserId,
                                                                     LiveData<List<Wish>> myWishlist) {
        return map(combineLatest(getMyRatings(currentUserId), myWishlist), input -> {
            List<Rating> ratings = input.first;

            // Optimization: also do this in a transformation
            List<Wish> wishes = input.second == null ? Collections.emptyList() : input.second;
            HashMap<String, Wish> wishesByItem = new HashMap<>();
            for (Wish wish : wishes) {
                wishesByItem.put(wish.getBeerId(), wish);
            }

            ArrayList<Pair<Rating, Wish>> result = new ArrayList<>();
            for (Rating rating : ratings) {
                Wish wish = wishesByItem.get(rating.getBeerId());
                result.add(Pair.create(rating, wish));
            }
            return result;
        });
    }

    public LiveData<List<Pair<Rating, FridgeItem>>> getMyRatingsWithFridge(LiveData<String> currentUserId,
                                                                     LiveData<List<FridgeItem>> myFridge) {
        return map(combineLatest(getMyRatings(currentUserId), myFridge), input -> {
            List<Rating> ratings = input.first;

            // Optimization: also do this in a transformation
            List<FridgeItem> fridge = input.second == null ? Collections.emptyList() : input.second;
            HashMap<String, FridgeItem> fridgeByItem = new HashMap<>();
            for (FridgeItem fridgeItem : fridge) {
                fridgeByItem.put(fridgeItem.getBeerId(), fridgeItem);
            }

            ArrayList<Pair<Rating, FridgeItem>> result = new ArrayList<>();
            for (Rating rating : ratings) {
                FridgeItem fridgeItem = fridgeByItem.get(rating.getBeerId());
                result.add(Pair.create(rating, fridgeItem));
            }
            return result;
        });
    }

    public LiveData<List<Rating>> getMyRatings(LiveData<String> currentUserId) {
        return switchMap(currentUserId, RatingsRepository::getRatingsByUser);
    }
}
