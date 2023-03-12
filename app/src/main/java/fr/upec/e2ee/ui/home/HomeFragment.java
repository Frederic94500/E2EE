package fr.upec.e2ee.ui.home;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;
import java.security.GeneralSecurityException;

import fr.upec.e2ee.R;
import fr.upec.e2ee.databinding.FragmentHomeBinding;
import fr.upec.e2ee.mystate.MyState;
import fr.upec.e2ee.ui.conversation.ConversationFragment;
import fr.upec.e2ee.ui.message1.Message1Fragment;

public class HomeFragment extends Fragment {
    private MyState myState;
    private FragmentHomeBinding binding;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        setExitTransition(transitionInflater.inflateTransition(R.transition.fade));
        setEnterTransition(transitionInflater.inflateTransition(R.transition.slide_right));

        try {
            myState = MyState.load();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.fabStartConv.setOnClickListener(view -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.nav_host_fragment_content_main, Message1Fragment.newInstance());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        ListView listView = binding.homeConvList;
        TextView textView = binding.emptyConv;
        if (myState.getMyConversations().getSize() == 0) {
            listView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
        }

        listView.setClickable(true);
        listView.setAdapter(new ArrayAdapter<>(getContext(), R.layout.fragment_home, myState.getMyConversations().getMyConversations()));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ConversationFragment conversationFragment = ConversationFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putInt("Conv", position);
            conversationFragment.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, conversationFragment)
                    .addToBackStack(null)
                    .commit();
        });

        //Loop for each conversation
        /*FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .add()
                .detach(this)
                .attach()
                .addToBackStack(null)
                .commit();*/

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            myState.save();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        binding = null;
    }
}