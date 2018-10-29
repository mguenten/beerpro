package ch.beerpro.data.repositories;

import android.util.Pair;
import androidx.lifecycle.LiveData;
import ch.beerpro.domain.models.Beer;
import ch.beerpro.domain.models.Entity;
import ch.beerpro.domain.models.FridgeItem;
import ch.beerpro.domain.utils.FirestoreQueryLiveData;
import ch.beerpro.domain.utils.FirestoreQueryLiveDataArray;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static androidx.lifecycle.Transformations.map;
import static androidx.lifecycle.Transformations.switchMap;
import static ch.beerpro.domain.utils.LiveDataExtensions.combineLatest;

public class FridgeRepository {

    private static LiveData<List<FridgeItem>> getFridgeByUser(String userId) {
        return new FirestoreQueryLiveDataArray<>(FirebaseFirestore.getInstance().collection(FridgeItem.COLLECTION)
                .orderBy(FridgeItem.FIELD_ADDED_AT, Query.Direction.DESCENDING).whereEqualTo(FridgeItem.FIELD_USER_ID, userId),
                FridgeItem.class);
    }

    private static LiveData<FridgeItem> getUserFridgeFor(Pair<String, Beer> input) {
        String userId = input.first;
        Beer beer = input.second;
        DocumentReference document = FirebaseFirestore.getInstance().collection(FridgeItem.COLLECTION)
                .document(FridgeItem.generateId(userId, beer.getId()));
        return new FirestoreQueryLiveData<>(document, FridgeItem.class);
    }

    public Task<Void> addBottleInUserFridge(String userId, String itemId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String fridgeItemId = FridgeItem.generateId(userId, itemId);

        DocumentReference fridgeItemEntryQuery = db.collection(FridgeItem.COLLECTION).document(fridgeItemId);

        return fridgeItemEntryQuery.get().continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                FridgeItem fridgeItem = task.getResult().toObject(FridgeItem.class);
                fridgeItem.increaseCount();
                return fridgeItemEntryQuery.set(fridgeItem);
            } else {
                throw task.getException();
            }
        });
    }

    public Task<Void> removeBottleFromUserFridge(String userId, String itemId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String fridgeItemId = FridgeItem.generateId(userId, itemId);

        DocumentReference fridgeItemEntryQuery = db.collection(FridgeItem.COLLECTION).document(fridgeItemId);

        return fridgeItemEntryQuery.get().continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                FridgeItem fridgeItem = task.getResult().toObject(FridgeItem.class);
                int count = fridgeItem.getCount();
                if (count == 1) {
                    return fridgeItemEntryQuery.delete();
                }
                fridgeItem.decreaseCount();
                return fridgeItemEntryQuery.set(fridgeItem);
            } else {
                throw task.getException();
            }
        });
    }

    public Task<Void> toggleUserFridgeItem(String userId, String itemId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String fridgeItemId = FridgeItem.generateId(userId, itemId);

        DocumentReference fridgeItemEntryQuery = db.collection(FridgeItem.COLLECTION).document(fridgeItemId);

        return fridgeItemEntryQuery.get().continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                return fridgeItemEntryQuery.delete();
            } else if (task.isSuccessful()) {
                return fridgeItemEntryQuery.set(new FridgeItem(userId, itemId, new Date()));
            } else {
                throw task.getException();
            }
        });
    }

    public LiveData<List<Pair<FridgeItem, Beer>>> getMyFridgeWithBeers(LiveData<String> currentUserId,
                                                                   LiveData<List<Beer>> allBeers) {
        return map(combineLatest(getMyFridge(currentUserId), map(allBeers, Entity::entitiesById)), input -> {
            List<FridgeItem> fridge = input.first;
            HashMap<String, Beer> beersById = input.second;

            ArrayList<Pair<FridgeItem, Beer>> result = new ArrayList<>();
            for (FridgeItem fridgeItem : fridge) {
                Beer beer = beersById.get(fridgeItem.getBeerId());
                result.add(Pair.create(fridgeItem, beer));
            }
            return result;
        });
    }

    public LiveData<List<FridgeItem>> getMyFridge(LiveData<String> currentUserId) {
        return switchMap(currentUserId, FridgeRepository::getFridgeByUser);
    }


    public LiveData<FridgeItem> getMyFridgeItemForBeer(LiveData<String> currentUserId, LiveData<Beer> beer) {


        return switchMap(combineLatest(currentUserId, beer), FridgeRepository::getUserFridgeFor);
    }
}
