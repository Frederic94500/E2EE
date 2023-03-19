package fr.upec.e2ee.ui.about;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AboutViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AboutViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("APPLICATION E2EE\n" +
                "demarrer une conversation avec une personne\n" +
                "ajouter la personne dans l'annuaire\n" +
                "creer une conversation  en générant le message 1 et 2\n" +
                "vous pouvez demarrer le chiffrement et dechiffrement!\n");


    }

    public LiveData<String> getText() {
        return mText;
    }
}