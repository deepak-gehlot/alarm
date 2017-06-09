package rudiment.alaramapp.fragment.inbox;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import rudiment.alaramapp.R;
import rudiment.alaramapp.adapter.chat.TabPagerAdapter;

public class InboxFragment extends Fragment {

    public InboxFragment() {
        // Required empty public constructor
    }

    public static InboxFragment newInstance() {
        return new InboxFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inbox, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    private void init(View view) {
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);


        TextView tab1 = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.tab_row, null);
        TextView tab2 = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.tab_row, null);
        tab1.setText("CHAT");
        tab2.setText("GROUP");
        tab1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.chat_icon, 0, 0, 0);
        tab2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.group_icon, 0, 0, 0);
        tabLayout.getTabAt(0).setCustomView(tab1);
        tabLayout.getTabAt(1).setCustomView(tab2);
    }

    /**
     * set tabs to adapter
     *
     * @param viewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        TabPagerAdapter adapter = new TabPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new ChatListFragment(), "CHAT");
        adapter.addFragment(new GroupListFragment(), "GROUP");
        viewPager.setAdapter(adapter);
    }
}

