package ch.beerpro.data.repositories;

import android.util.Log;

import ch.beerpro.domain.models.Wish;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.LiveData;
import ch.beerpro.domain.models.PrivateNote;
import ch.beerpro.domain.utils.FirestoreQueryLiveData;
import ch.beerpro.domain.utils.FirestoreQueryLiveDataArray;

public class PrivateNoteRepository {

    public static LiveData<PrivateNote> getPrivateNote(String beerId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();
        return new FirestoreQueryLiveData<>(FirebaseFirestore.getInstance().collection(PrivateNote.COLLECTION)
                .document(PrivateNote.generateId(currentUserId, beerId)), PrivateNote.class);
    }

    public static void addPrivateNote(String beerId, String privateNote) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = currentUser.getUid();
        PrivateNote newPrivateNote = new PrivateNote(currentUserId, beerId, privateNote, new Date());

        String privateNoteId =  PrivateNote.generateId(currentUserId, beerId);
        DocumentReference privateNoteEntryQuery = db.collection(PrivateNote.COLLECTION).document(privateNoteId);

        privateNoteEntryQuery.set(newPrivateNote);
    }

}