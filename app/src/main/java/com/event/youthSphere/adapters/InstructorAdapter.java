package com.event.youthSphere.adapters;

import android.view.*;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.event.youthSphere.R;
import com.event.youthSphere.models.users.InstructorUser;

import java.util.List;

/**
 * Adapter for displaying instructor items in a RecyclerView
 */
public class InstructorAdapter extends RecyclerView.Adapter<InstructorAdapter.InstructorViewHolder> {

    private List<InstructorUser> instructorList;
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    /**
     * Interface for handling edit click events on instructors
     */
    public interface OnEditClickListener {
        /**
         * Called when edit button is clicked
         * @param instructor The instructor to be edited
         */
        void onEditClick(InstructorUser instructor);
    }

    /**
     * Interface for handling delete click events on instructors
     */
    public interface OnDeleteClickListener {
        /**
         * Called when delete button is clicked
         * @param instructor The instructor to be deleted
         */
        void onDeleteClick(InstructorUser instructor);
    }

    /**
     * Constructor for InstructorAdapter
     * @param instructorList Initial list of instructors
     */
    public InstructorAdapter(List<InstructorUser> instructorList) {
        this.instructorList = instructorList;
    }

    /**
     * Updates the instructor list and notifies adapter
     * @param newList Updated list of instructors
     */
    public void updateList(List<InstructorUser> newList) {
        this.instructorList = newList;
        notifyDataSetChanged();
    }

    /**
     * Sets the edit click listener
     * @param listener The listener to be notified of edit events
     */
    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
    }

    /**
     * Sets the delete click listener
     * @param listener The listener to be notified of delete events
     */
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public InstructorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_instructor_item, parent, false);
        return new InstructorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstructorViewHolder holder, int position) {
        InstructorUser instructor = instructorList.get(position);
        holder.tvName.setText(instructor.getFullName());
        holder.tvEmail.setText(instructor.getEmail());

        String field = instructor.getFieldOfSpecialization();
        holder.tvField.setText(field == null || field.isEmpty()
                ? holder.itemView.getContext().getString(R.string.not_assigned)
                : field);

        String date = instructor.getEmploymentStartDate();
        holder.tvDate.setText(date == null || date.isEmpty()
                ? holder.itemView.getContext().getString(R.string.not_assigned)
                : date);

        holder.ivEdit.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(instructor);
            }
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(instructor);
            }
        });
    }

    @Override
    public int getItemCount() {
        return instructorList.size();
    }

    /**
     * ViewHolder class for instructor items
     */
    static class InstructorViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvField, tvDate;
        ImageView ivEdit, ivDelete;

        /**
         * Constructor for InstructorViewHolder
         * @param itemView The view for the instructor item
         */
        public InstructorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvInstructorName);
            tvEmail = itemView.findViewById(R.id.tvInstructorEmail);
            tvField = itemView.findViewById(R.id.tvFieldSpecialization);
            tvDate = itemView.findViewById(R.id.tvEmploymentDate);
            ivEdit = itemView.findViewById(R.id.btnEdit);
            ivDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}