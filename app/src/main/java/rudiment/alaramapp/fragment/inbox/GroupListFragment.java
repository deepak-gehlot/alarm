package rudiment.alaramapp.fragment.inbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import rudiment.alaramapp.R;
import rudiment.alaramapp.activity.CreateGroupActivity;

public class GroupListFragment extends Fragment implements View.OnClickListener {

    private RecyclerView recyclerView;
    private TextView createGroupTxt;

    public GroupListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    private void init(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.groupList);
        createGroupTxt = (TextView) view.findViewById(R.id.createNewGroupTxt);
        createGroupTxt.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.createNewGroupTxt:
                startActivity(new Intent(getActivity(), CreateGroupActivity.class));
                break;
        }
    }
}
