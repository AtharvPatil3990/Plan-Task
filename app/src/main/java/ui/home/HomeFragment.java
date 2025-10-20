package ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.fragment.app.Fragment;

import com.android.plantask.R;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    TextView tvDisplayName, tvDisplayDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvDisplayName = view.findViewById(R.id.tvWelcomeText);
        tvDisplayDate = view.findViewById(R.id.tvDate);

        setAndGetName();
        setDate();
        return view;
    }
    private void setDate(){
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
        String date = sdf.format(calendar.getTime());

        tvDisplayDate.setText(date);
    }

    private void setAndGetName(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("PlanTaskPref", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("Username", null);

        if(userName == null){
            askUserName();
        }
        else
            tvDisplayName.setText("Welcome, " + userName);

    }

    private void askUserName(){

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("PlanTaskPref", Context.MODE_PRIVATE);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle("Enter your name");
        final TextInputEditText editText = new TextInputEditText(requireContext());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);

        dialogBuilder.setView(editText)
            .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = editText.getText().toString();
                    if(!name.isEmpty()){
                        tvDisplayName.setText("Welcome, " + name);
                        sharedPreferences.edit()
                                .putString("Username", name)
                                .apply();
                    }
                    else
                        tvDisplayName.setText("Welcome!");
                }
            });

        dialogBuilder.setCancelable(false)
                .show();

    }

}
