package fr.upec.e2ee.ui.identity;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class IdentityViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    View nice;

    public IdentityViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("AFFICHER CLE PUBLIC");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
