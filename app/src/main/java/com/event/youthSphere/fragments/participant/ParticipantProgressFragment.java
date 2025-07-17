package com.event.youthSphere.fragments.participant;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.event.youthSphere.R;
import com.event.youthSphere.firebase.FirebaseParticipantHelper;
import com.event.youthSphere.models.activity.ActivityModel;
import com.event.youthSphere.models.users.ParticipantUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
public class ParticipantProgressFragment extends Fragment {

    private TextView txtName, txtTotalActivities, txtAvgScore, txtDomainDistribution;


    private TextView txtMonthTitle;
    private Button btnPrevMonth, btnNextMonth;
    private LinearLayout weekdayHeader;
    private GridLayout calendarGrid;
    private YearMonth displayedMonth;
    private final Set<String> validDays = new HashSet<>(Arrays.asList());


    @SuppressLint("NewApi")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_participant_progress, container, false);

        txtName = view.findViewById(R.id.txtName);
        txtTotalActivities = view.findViewById(R.id.txtTotalActivities);
        txtAvgScore = view.findViewById(R.id.txtAvgScore);
        txtDomainDistribution = view.findViewById(R.id.txtDomainDistribution);
        txtMonthTitle = view.findViewById(R.id.txtMonthTitle);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        weekdayHeader = view.findViewById(R.id.weekdayHeader);
        calendarGrid = view.findViewById(R.id.calendarGrid);

        displayedMonth = YearMonth.now();

        setupWeekdayHeader();
        setupCalendarGrid(displayedMonth);

        btnPrevMonth.setOnClickListener(v -> {
            displayedMonth = displayedMonth.minusMonths(1);
            setupCalendarGrid(displayedMonth);
        });

        btnNextMonth.setOnClickListener(v -> {
            displayedMonth = displayedMonth.plusMonths(1);
            setupCalendarGrid(displayedMonth);
        });

        fetchActivityDates(); // activities
        fetchUserProgress(); // fetch user progress like average rating

        return view;
    }

    private void fetchActivityDates() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseParticipantHelper.getRegisteredActivities(uid, (activities, err) -> {
            if (activities == null || err != null) return;

            validDays.clear();
            for (ActivityModel activity : activities) {
                if (activity.getStartDate() != null && activity.getStartDate().after(new Date())) { continue; }
                List<String> daysOfActivity = activity.getDays();
                validDays.addAll(daysOfActivity);
            }

            Log.d("ParticipantProgressFragment", "Valid Days: " + validDays);

            // UI updates must be run on UI thread
            requireActivity().runOnUiThread(() -> {
                setupWeekdayHeader();
                setupCalendarGrid(displayedMonth);
            });
        });
    }





    private void fetchUserProgress() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    ParticipantUser user = snapshot.toObject(ParticipantUser.class);
                    if (user == null) return;

                    txtName.setText(user.getFullName());

                    FirebaseParticipantHelper.getProgressData(user.getUid(), (progress, error) -> {
                        if (progress == null) return;

                        txtTotalActivities.setText("Activities Attended: " + progress.getTotalActivities());
                        txtAvgScore.setText("Avg Feedback Score: " + String.format(Locale.ENGLISH, "%.2f", progress.getAverageScore()));

                        StringBuilder domainStats = new StringBuilder();
                        Map<String, Float> domainAverages = progress.getDomainWiseAverage();

                        for (String domain : progress.getDomainParticipation().keySet()) {
                            domainStats.append(domain)
                                    .append(": ")
                                    .append(progress.getDomainParticipation().get(domain))
                                    .append(" | Avg: ")
                                    .append(String.format(Locale.ENGLISH, "%.1f", domainAverages.getOrDefault(domain, 0f)))
                                    .append("\n");
                        }

                        txtDomainDistribution.setText(domainStats.toString().trim());
                    });
                });
    }

    @SuppressLint("NewApi")
    private void setupWeekdayHeader() {
        weekdayHeader.removeAllViews();
        DayOfWeek[] daysOfWeek = DayOfWeek.values();
        for (int i = 0; i < 7; i++) {
            TextView tv = new TextView(getContext());
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            tv.setGravity(Gravity.CENTER);
            tv.setText(daysOfWeek[i].getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            tv.setTextSize(16);
            tv.setTextColor(Color.BLACK);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);

            if (validDays.contains(daysOfWeek[i].name())) {
                tv.setBackgroundResource(R.drawable.bg_column_highlight);
            }
            weekdayHeader.addView(tv);
        }
    }

    @SuppressLint("NewApi")
    private void setupCalendarGrid(YearMonth monthToShow) {
        calendarGrid.removeAllViews();

        txtMonthTitle.setText(monthToShow.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + monthToShow.getYear());

        LocalDate firstOfMonth = monthToShow.atDay(1);
        int firstDayOfWeekIndex = firstOfMonth.getDayOfWeek().getValue() % 7; // SUNDAY=0, MONDAY=1, ..., SATURDAY=6

        int daysInMonth = monthToShow.lengthOfMonth();

        // Fill leading blank cells
        for (int i = 0; i < firstDayOfWeekIndex; i++) {
            calendarGrid.addView(makeBlankCell());
        }

        // Fill days
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = monthToShow.atDay(day);
            int col = date.getDayOfWeek().getValue() % 7;
            TextView cell = makeDayCell(date.getDayOfMonth(), col);
            calendarGrid.addView(cell);
        }

        // Fill trailing blank cells if needed so last row is always 7 columns
        int totalCells = firstDayOfWeekIndex + daysInMonth;
        int trailingBlanks = (7 - (totalCells % 7)) % 7;
        for (int i = 0; i < trailingBlanks; i++) {
            calendarGrid.addView(makeBlankCell());
        }
    }

    private TextView makeBlankCell() {
        TextView tv = new TextView(getContext());
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 100; // cell height
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        tv.setLayoutParams(params);
        tv.setText("");
        return tv;
    }

    @SuppressLint("NewApi")
    private TextView makeDayCell(int dayOfMonth, int dayOfWeekColumn) {
        TextView tv = new TextView(getContext());
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 100; // cell height
        params.columnSpec = GridLayout.spec(dayOfWeekColumn, 1f);
        params.setMargins(4, 4, 4, 4);
        tv.setLayoutParams(params);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(15);
        tv.setTextColor(Color.BLACK);
        tv.setText(String.valueOf(dayOfMonth));

        DayOfWeek[] daysOfWeek = DayOfWeek.values();
        if (validDays.contains(daysOfWeek[dayOfWeekColumn].name())) {
            tv.setBackgroundResource(R.drawable.bg_column_highlight);
        } else {
            tv.setBackgroundColor(Color.TRANSPARENT);
        }
        return tv;
    }






}