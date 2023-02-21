package fr.upec.e2ee.ui.identity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class IdentityViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public IdentityViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
