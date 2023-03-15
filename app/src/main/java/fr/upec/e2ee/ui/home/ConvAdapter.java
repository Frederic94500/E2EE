package fr.upec.e2ee.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import fr.upec.e2ee.E2EE;
import fr.upec.e2ee.R;
import fr.upec.e2ee.mystate.MyConversations;
import fr.upec.e2ee.protocol.Conversation;

public class ConvAdapter extends BaseAdapter {
    private MyConversations myConversations;

    public ConvAdapter(MyConversations myConversations) {
        this.myConversations = myConversations;
    }

    @Override
    public int getCount() {
        return myConversations.getSize();
    }

    @Override
    public Object getItem(int position) {
        return myConversations.getConversation(position);
    }

    @Override
    public long getItemId(int position) {
        throw new IllegalArgumentException("Stub!");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(E2EE.getContext()).inflate(R.layout.fragment_home, parent, false);

        Conversation conversation = (Conversation) getItem(position);

        //TextView titleView = view.findViewById(view.findViewById(R.id.home_conv_list));
        return null;
    }
}
