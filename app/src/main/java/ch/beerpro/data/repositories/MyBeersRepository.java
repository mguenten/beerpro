package ch.beerpro.data.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import ch.beerpro.domain.models.*;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

import static androidx.lifecycle.Transformations.map;
import static ch.beerpro.domain.utils.LiveDataExtensions.combineLatest;

public class MyBeersRepository {

    private static List<MyBeer> getMyBeersFirst(Triple<List<Wish>, List<Rating>, HashMap<String, Beer>> input) {
        List<Wish> wishlist = input.getLeft();
        List<Rating> ratings = input.getMiddle();
        HashMap<String, Beer> beers = input.getRight();

        ArrayList<MyBeer> result = new ArrayList<>();
        Set<String> beersAlreadyOnTheList = new HashSet<>();
        for (Wish wish : wishlist) {
            String beerId = wish.getBeerId();
            result.add(new MyBeerFromWishlist(wish, beers.get(beerId)));
            beersAlreadyOnTheList.add(beerId);
        }

        for (Rating rating : ratings) {
            String beerId = rating.getBeerId();
            if (beersAlreadyOnTheList.contains(beerId)) {
                // if the beer is already on the wish list, don't add it again
            } else {
                result.add(new MyBeerFromRating(rating, beers.get(beerId)));
                // we also don't want to see a rated beer twice
                beersAlreadyOnTheList.add(beerId);
            }
        }
        Collections.sort(result, (r1, r2) -> r2.getDate().compareTo(r1.getDate()));
        return result;
    }

    private static List<MyBeer> getMyBeersSecond(Triple<List<MyBeer>, List<FridgeItem>, HashMap<String, Beer>> input) {
        List<MyBeer> myBeers = input.getLeft();
        List<FridgeItem> fridge = input.getMiddle();
        HashMap<String, Beer> beers = input.getRight();

        ArrayList<MyBeer> result = new ArrayList<>();
        Set<String> beersAlreadyOnTheList = new HashSet<>();
        for (MyBeer myBeer : myBeers) {
            String beerId = myBeer.getBeerId();
            result.add(myBeer);
            beersAlreadyOnTheList.add(beerId);
        }
        for (FridgeItem fridgeItem : fridge) {
            String beerId = fridgeItem.getBeerId();
            if (beersAlreadyOnTheList.contains(beerId)) {
                // if the beer is already on the list, don't add it again
            } else {
                result.add(new MyBeerFromFridge(fridgeItem, beers.get(beerId)));
                // we also don't want to see a rated beer twice
                beersAlreadyOnTheList.add(beerId);
            }
        }
        Collections.sort(result, (r1, r2) -> r2.getDate().compareTo(r1.getDate()));
        return result;
    }


    public LiveData<List<MyBeer>> getMyBeers(LiveData<List<Beer>> allBeers, LiveData<List<Wish>> myWishlist,
                                             LiveData<List<Rating>> myRatings, LiveData<List<FridgeItem>> myFridge) {
        LiveData<List<MyBeer>> myBeers = map(combineLatest(myWishlist, myRatings, map(allBeers, Entity::entitiesById)),
                MyBeersRepository::getMyBeersFirst);

        return map(combineLatest(myBeers, myFridge, map(allBeers, Entity::entitiesById)),
                MyBeersRepository::getMyBeersSecond);
    }

}
