package ch.beerpro.domain.models;

import com.google.firebase.firestore.Exclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateNote implements Entity {

    public static final String COLLECTION = "privatenote";
    public static final String FIELD_BEER_ID = "beerId";
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_PRIVATENOTE = "privateNote";
    public static final String FIELD_CREATION_DATE = "creationDate";

    @Exclude
    private String id;
    private String beerId;
    private String userId;
    private String privateNoteValue;
    private Date creationDate;

    public PrivateNote(String userId, String beerId, String privateNoteValue, Date creationDate) {
        this.userId = userId;
        this.beerId = beerId;
        this.privateNoteValue = privateNoteValue;
        this.creationDate = creationDate;
    }

    public String getPrivateNoteValue() {
        return privateNoteValue;
    }

    public static String generateId(String userId, String beerId) {
        return String.format("%s_%s", userId, beerId);
    }
}
