package fr.upec.e2ee.ui.home;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
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
    ListView listView;
    ListAdapter listAdapter;
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

        generateFragment();

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

        listView.setAdapter(null);
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            myState = MyState.load();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        generateFragment();
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            myState.save();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        listView.setAdapter(null);
        binding = null;
    }

    private void generateFragment() {
        binding.fabStartConv.setOnClickListener(view -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.nav_host_fragment_content_main, Message1Fragment.newInstance());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        listView = binding.homeConvList;
        TextView textView = binding.emptyConv;
        if (myState.getMyConversations().getSize() == 0) {
            listView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
        }

        listAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, myState.getMyConversations().nameConversations());
        listView.setClickable(true);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            binding.fabStartConv.setVisibility(View.GONE);
            ConversationFragment conversationFragment = ConversationFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putInt("Conv", position);
            conversationFragment.setArguments(bundle);

            FragmentManager homeFragment = getParentFragmentManager();
            homeFragment.beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, conversationFragment, "childConv")
                    .addToBackStack("home")
                    .commit();
        });
    }
}
