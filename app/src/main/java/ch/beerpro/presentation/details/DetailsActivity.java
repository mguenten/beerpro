package ch.beerpro.presentation.details;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import ch.beerpro.GlideApp;
import ch.beerpro.R;
import ch.beerpro.data.repositories.PrivateNoteRepository;
import ch.beerpro.domain.models.Beer;
import ch.beerpro.domain.models.FridgeItem;
import ch.beerpro.domain.models.MyBeer;
import ch.beerpro.domain.models.PrivateNote;
import ch.beerpro.domain.models.Rating;
import ch.beerpro.domain.models.Wish;
import ch.beerpro.presentation.details.createrating.CreateRatingActivity;
import lombok.val;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import static ch.beerpro.presentation.utils.DrawableHelpers.setDrawableTint;

public class DetailsActivity extends AppCompatActivity implements OnRatingLikedListener {

    public static final String ITEM_ID = "item_id";
    private static final String TAG = "DetailsActivity";
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.nested_scroll_view)
    NestedScrollView nestedScrollView;

    @BindView(R.id.photo)
    ImageView photo;

    @BindView(R.id.avgRating)
    TextView avgRating;

    @BindView(R.id.numRatings)
    TextView numRatings;

    @BindView(R.id.ratingBar)
    RatingBar ratingBar;

    @BindView(R.id.name)
    TextView name;

    @BindView(R.id.wishlist)
    ToggleButton wishlist;

    @BindView(R.id.fridge)
    ToggleButton fridge;

    @BindView(R.id.manufacturer)
    TextView manufacturer;

    @BindView(R.id.category)
    TextView category;

    @BindView(R.id.addRatingBar)
    RatingBar addRatingBar;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private BottomSheetDialog actionDialog;

    private RatingsRecyclerViewAdapter adapter;

    private DetailsViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        toolbar.setTitleTextColor(Color.alpha(0));

        String beerId = getIntent().getExtras().getString(ITEM_ID);

        model = ViewModelProviders.of(this).get(DetailsViewModel.class);
        model.setBeerId(beerId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RatingsRecyclerViewAdapter(this, model.getCurrentUser());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        model.getBeer().observe(this, this::updateBeer);
        model.getRatings().observe(this, this::updateRatings);
        model.getWish().observe(this, this::toggleWishlistView);
        model.getFridgeItem().observe(this, this::toggleFridgeView);

        recyclerView.setAdapter(adapter);
        addRatingBar.setOnRatingBarChangeListener(this::addNewRating);

        View view = getLayoutInflater().inflate(R.layout.single_bottom_sheet_dialog, null);
        Button addPrivateNoteButton = view.findViewById(R.id.addPrivateNote);
        addPrivateNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putSerializable(ITEM_ID, model.getBeer().getValue().getId());

                DialogFragment newFragment = new EditPrivateNoteDialogFragment();
                newFragment.setArguments(args);
                newFragment.show(getSupportFragmentManager(), "privatenote");
            }
        });
        actionDialog = new BottomSheetDialog(this);
        actionDialog.setContentView(view);
    }

    private void addNewRating(RatingBar ratingBar, float v, boolean b) {
        startRatingActivity(v);
    }

    private void startRatingActivity(float ratingValue){
        Intent intent = new Intent(this, CreateRatingActivity.class);
        intent.putExtra(CreateRatingActivity.ITEM, model.getBeer().getValue());
        intent.putExtra(CreateRatingActivity.RATING, ratingValue);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, addRatingBar, "rating");
        startActivity(intent, options.toBundle());
    }

    @OnClick(R.id.actionsButton)
    public void showBottomSheetDialog() {
        actionDialog.show();
    }

    public void notifyImprovement(View view) {
        String beerName = model.getBeer().getValue().getName();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:m.thevenaz@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Betrifft Bier \"" + beerName + "\"");
        startActivity(intent);
    }

    private void updateBeer(Beer item) {
        name.setText(item.getName());
        manufacturer.setText(item.getManufacturer());
        category.setText(item.getCategory());
        name.setText(item.getName());
        GlideApp.with(this).load(item.getPhoto()).apply(new RequestOptions().override(120, 160).centerInside())
                .into(photo);
        ratingBar.setNumStars(5);
        ratingBar.setRating(item.getAvgRating());
        avgRating.setText(getResources().getString(R.string.fmt_avg_rating, item.getAvgRating()));
        numRatings.setText(getResources().getString(R.string.fmt_ratings, item.getNumRatings()));
        toolbar.setTitle(item.getName());
    }

    private void updateRatings(List<Rating> ratings) {
        adapter.submitList(new ArrayList<>(ratings));
        for (Rating rating : ratings){
            if(rating.getUserId().equals(model.getCurrentUser().getUid())){
                addRatingBar.setIsIndicator(true);
                addRatingBar.setOnRatingBarChangeListener(null);
                addRatingBar.setRating(rating.getRating());
                break;
            }
        }
    }

    @Override
    public void onRatingLikedListener(Rating rating) {
        model.toggleLike(rating);
    }

    public void onAddRatingClickedListener(View view){
        actionDialog.hide();
        startRatingActivity(0);
    }

    @OnClick(R.id.wishlist)
    public void onWishClickedListener(View view) {
        model.toggleItemInWishlist(model.getBeer().getValue().getId());
        /*
         * We won't get an update from firestore when the wish is removed, so we need to reset the UI state ourselves.
         * */
        if (!wishlist.isChecked()) {
            toggleWishlistView(null);
        }
    }

    private void toggleWishlistView(Wish wish) {
        if (wish != null) {
            int color = getResources().getColor(R.color.colorPrimary);
            setDrawableTint(wishlist, color);
            wishlist.setChecked(true);
        } else {
            int color = getResources().getColor(android.R.color.darker_gray);
            setDrawableTint(wishlist, color);
            wishlist.setChecked(false);
        }
    }

    @OnClick(R.id.fridge)
    public void onFridgeItemClickedListener(View view) {
        model.toggleItemInFridge(model.getBeer().getValue().getId());
        /*
         * We won't get an update from firestore when the fridgeItem is removed, so we need to reset the UI state ourselves.
         * */
        if (!fridge.isChecked()) {
            toggleFridgeView(null);
        }
    }

    private void toggleFridgeView(FridgeItem fridgeItem) {
        if (fridgeItem != null) {
            int color = getResources().getColor(R.color.colorPrimary);
            setDrawableTint(fridge, color);
            fridge.setChecked(true);
        } else {
            int color = getResources().getColor(android.R.color.darker_gray);
            setDrawableTint(fridge, color);
            fridge.setChecked(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class EditPrivateNoteDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String beerID = getArguments().getString(ITEM_ID);

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            EditText privateNoteEditText = new EditText(builder.getContext());
            privateNoteEditText.setLines(4);
            //model.getBeer().getValue().getId()
            LiveData<PrivateNote> newprivatenote = PrivateNoteRepository.getPrivateNote(beerID);

            Log.d("privatenote", "newprivatenote.getValue().getPrivateNoteValue() || " /*+ newprivatenote.getValue().toString()*/);
            if(newprivatenote.getValue().FIELD_PRIVATENOTE == null) {
                privateNoteEditText.setText("");
            } else {
                privateNoteEditText.setText(newprivatenote.getValue().FIELD_PRIVATENOTE);
            }
            //Start Asynchtask and check if data is available
            builder.setMessage("Private Notiz")
                    .setView(privateNoteEditText)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            PrivateNoteRepository.addPrivateNote(beerID, privateNoteEditText.getText().toString());
                            Log.d("privatenote", "privateNoteEditText.getText().toString() || " + privateNoteEditText.getText().toString());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}