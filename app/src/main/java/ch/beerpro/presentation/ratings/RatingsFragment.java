package ch.beerpro.presentation.ratings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import ch.beerpro.R;
import ch.beerpro.domain.models.FridgeItem;
import ch.beerpro.presentation.MainViewModel;
import ch.beerpro.presentation.details.DetailsActivity;
import ch.beerpro.domain.models.Rating;
import ch.beerpro.domain.models.Wish;

import androidx.fragment.app.Fragment;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

public class RatingsFragment extends Fragment
        implements OnRatingsItemInteractionListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "FeedFragment";

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    private RatingsRecyclerViewAdapter adapter;
    private MainViewModel model;

    public RatingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_ratings, container, false);
        ButterKnife.bind(this, rootView);

        model = ViewModelProviders.of(this).get(MainViewModel.class);
        model.getAllRatingsWithWishes().observe(this, this::updateRatings);

        val layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RatingsRecyclerViewAdapter(this, this, model.getCurrentUser());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), layoutManager.getOrientation()));

        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this);

        return rootView;
    }

    private void updateRatings(List<Pair<Rating, Wish>> ratings) {
        if (ratings != null) {
            adapter.submitList(new ArrayList<>(ratings));
        }
    }

    @Override
    public void onRatingLikedListener(Rating rating) {
        model.toggleLike(rating);
    }

    @Override
    public void onMoreClickedListener(Rating rating) {
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra(DetailsActivity.ITEM_ID, rating.getBeerId());
        startActivity(intent);
    }

    @Override
    public void onWishClickedListener(Rating item) {
        model.toggleItemInWishlist(item.getBeerId());
    }

    @Override
    public void onRefresh() {
        updateRatings(model.getAllRatingsWithWishes().getValue());
        swipeRefreshLayout.setRefreshing(false);
        adapter.notifyDataSetChanged();
    }
}
