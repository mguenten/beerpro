package ch.beerpro;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class HomeScreenProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    public HomeScreenProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_profile_screen, container, false);
        ImageView userProfileImageView = rootView.findViewById(R.id.userProfileImageView);
        TextView userProfileNameText = rootView.findViewById(R.id.userProfileNameText);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            userProfileNameText.setText(name);
            Uri photoUrl = user.getPhotoUrl();
            Picasso.get().load(photoUrl).transform(new CropCircleTransformation()).into(userProfileImageView);
        }

        return rootView;
    }

}