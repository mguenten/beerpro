package ch.beerpro.presentation.explore.search;

import android.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ch.beerpro.data.repositories.*;
import ch.beerpro.domain.models.Beer;
import ch.beerpro.domain.models.Search;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.lifecycle.Transformations.map;
import static androidx.lifecycle.Transformations.switchMap;
import static ch.beerpro.domain.utils.LiveDataExtensions.zip;

public class SearchViewModel extends ViewModel implements CurrentUser {

    private static final String TAG = "SearchViewModel";
    private final MutableLiveData<String> searchTerm = new MutableLiveData<>();
    private final MutableLiveData<String> currentUserId = new MutableLiveData<>();

    private final LiveData<List<Beer>> filteredBeers;
    private final BeersRepository beersRepository;
    private final WishlistRepository wishlistRepository;
    private final FridgeRepository fridgeRepository;
    private final SearchesRepository searchesRepository;
    private final LiveData<List<Search>> myLatestSearches;

    public SearchViewModel() {
        beersRepository = new BeersRepository();
        wishlistRepository = new WishlistRepository();
        fridgeRepository = new FridgeRepository();
        searchesRepository = new SearchesRepository();
        filteredBeers = map(zip(searchTerm, getAllBeers()), SearchViewModel::filter);
        myLatestSearches = switchMap(currentUserId, SearchesRepository::getLatestSearchesByUser);

        currentUserId.setValue(getCurrentUser().getUid());
    }

    public LiveData<List<Beer>> getAllBeers() {
        return beersRepository.getAllBeers();
    }

    private static List<Beer> filter(Pair<String, List<Beer>> input) {
        String searchTerm1 = input.first;
        List<Beer> allBeers = input.second;
        if (Strings.isNullOrEmpty(searchTerm1)) {
            return allBeers;
        }
        if (allBeers == null) {
            return Collections.emptyList();
        }
        ArrayList<Beer> filtered = new ArrayList<>();
        for (Beer beer : allBeers) {
            if (beer.getName().toLowerCase().contains(searchTerm1.toLowerCase())) {
                filtered.add(beer);
            }
        }
        return filtered;
    }

    public LiveData<List<Search>> getMyLatestSearches() {
        return myLatestSearches;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm.setValue(searchTerm);
    }

    public LiveData<List<Beer>> getFilteredBeers() {
        return filteredBeers;
    }


    public void toggleItemInWishlist(String beerId) {
        wishlistRepository.toggleUserWishlistItem(getCurrentUser().getUid(), beerId);
    }

    public void toggleItemInFridge(String beerId) {
        fridgeRepository.toggleUserFridgeItem(getCurrentUser().getUid(), beerId);
    }

    public void addToSearchHistory(String term) {
        searchesRepository.addSearchTerm(term);
    }
}