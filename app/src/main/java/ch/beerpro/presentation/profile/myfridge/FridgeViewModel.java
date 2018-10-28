package ch.beerpro.presentation.profile.myfridge;

import android.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ch.beerpro.data.repositories.BeersRepository;
import ch.beerpro.data.repositories.CurrentUser;
import ch.beerpro.data.repositories.FridgeRepository;
import ch.beerpro.domain.models.Beer;
import ch.beerpro.domain.models.FridgeItem;
import ch.beerpro.domain.models.Wish;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class FridgeViewModel extends ViewModel implements CurrentUser {
    private static final String TAG = "FridgeViewModel";

    private final MutableLiveData<String> currentUserId = new MutableLiveData<>();
    private final FridgeRepository fridgeRepository;
    private final BeersRepository beersRepository;

    public FridgeViewModel() {
        fridgeRepository = new FridgeRepository();
        beersRepository = new BeersRepository();

        currentUserId.setValue(getCurrentUser().getUid());
    }

    public LiveData<List<Pair<FridgeItem, Beer>>> getMyFridgeWithBeers() {
        return fridgeRepository.getMyFridgeWithBeers(currentUserId, beersRepository.getAllBeers());
    }

    public Task<Void> addBottleInFridge(String itemId) {
        return fridgeRepository.addBottleInUserFridge(getCurrentUser().getUid(), itemId);
    }

    public Task<Void> removeBottleFromFridge(String itemId) {
        return fridgeRepository.removeBottleFromUserFridge(getCurrentUser().getUid(), itemId);
    }
}
