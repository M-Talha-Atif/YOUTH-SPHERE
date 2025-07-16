package com.event.youthSphere.fragments.parent;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.event.youthSphere.R;
import com.event.youthSphere.adapters.ChildrenListAdapter;
import com.event.youthSphere.bottomsheet.parent.GiveFeedbackDialogFragment;
import com.event.youthSphere.firebase.FirebaseParentHelper;
import com.event.youthSphere.models.users.ParticipantUser;

import java.util.*;

public class ManageChildrenFragment extends Fragment {

    private View rootView;
    private Button btnToggleAddChild, btnAddChild;
    private Spinner spinnerChildren;
    private RecyclerView recyclerViewChildren;
    private LinearLayout addChildLayout;
    private ChildrenListAdapter adapter;

    private List<ParticipantUser> allParticipants = new ArrayList<>();
    private List<ParticipantUser> myChildren = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_manage_children, container, false);

        btnToggleAddChild = rootView.findViewById(R.id.btnToggleAddChild);
        btnAddChild = rootView.findViewById(R.id.btnAddChild);
        spinnerChildren = rootView.findViewById(R.id.spinnerChildren);
        recyclerViewChildren = rootView.findViewById(R.id.recyclerChildren);
        addChildLayout = rootView.findViewById(R.id.addChildLayout);

        recyclerViewChildren.setLayoutManager(new LinearLayoutManager(getContext()));

        // functional interface function implementation
        adapter = new ChildrenListAdapter(user -> {
            GiveFeedbackDialogFragment dialog = new GiveFeedbackDialogFragment(user);
            dialog.show(getParentFragmentManager(), "GiveFeedbackDialog");
        });

        recyclerViewChildren.setAdapter(adapter);

        loadChildren();

        btnToggleAddChild.setOnClickListener(v -> {
            if (addChildLayout.getVisibility() == View.VISIBLE) {
                addChildLayout.setVisibility(View.GONE);
                btnToggleAddChild.setText("Add Child");
            } else {
                addChildLayout.setVisibility(View.VISIBLE);
                btnToggleAddChild.setText("Cancel");
                loadAvailableParticipants();
            }
        });

        btnAddChild.setOnClickListener(v -> {
            ParticipantUser selected = (ParticipantUser) spinnerChildren.getSelectedItem();
            if (selected == null) return;

            FirebaseParentHelper.addChildToParent(selected.getUid(), (success, error) -> {
                if (success) {
                    Toast.makeText(getContext(), "Child added!", Toast.LENGTH_SHORT).show();
                    loadChildren();
                    addChildLayout.setVisibility(View.GONE);
                    btnToggleAddChild.setText("Add Child");
                }
                else {
                    Toast.makeText(getContext(), "Child already added by another parent", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return rootView;
    }

    private void loadChildren() {
        FirebaseParentHelper.getMyChildren((children, error) -> {
            if (error == null) {
                myChildren = children;
                adapter.submitList(new ArrayList<>(children));
            }
        });
    }

    private void loadAvailableParticipants() {
        FirebaseParentHelper.getAllAvailableParticipants((participants, error) -> {
            if (error == null && getContext() != null) {
                ArrayAdapter<ParticipantUser> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, participants);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerChildren.setAdapter(adapter);
            }
        });
    }
}
