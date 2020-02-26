package com.application.caravan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.application.utils.ConfirmationPassengerItemDTO;
import com.application.utils.CustomAdapterConfirmation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TripPassengersConfirmation extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore databasePassengers;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_passengers_confirmation);

        // Get listview checkbox.
        final ListView listViewWithCheckbox = (ListView)findViewById(R.id.listConfirmation);

        // Initiate listview data.
        final List<ConfirmationPassengerItemDTO> initItemList = this.getInitViewItemDtoList();

        // Create a custom list view adapter with checkbox control.
        final CustomAdapterConfirmation listViewDataAdapter = new CustomAdapterConfirmation(getApplicationContext(), initItemList);
        listViewDataAdapter.notifyDataSetChanged();

        // Set data adapter to list view.
        listViewWithCheckbox.setAdapter(listViewDataAdapter);

        // When list view item is clicked.
        listViewWithCheckbox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long l) {
                // Get user selected item.
                Object itemObject = adapterView.getAdapter().getItem(itemIndex);

                // Translate the selected item to DTO object.
                ConfirmationPassengerItemDTO itemDto = (ConfirmationPassengerItemDTO)itemObject;

                // Get the checkbox.
                CheckBox itemCheckbox = (CheckBox) view.findViewById(R.id.chkConfirmationPassenger);

                // Reverse the checkbox and clicked item check state.
                if(itemDto.isChecked())
                {
                    itemCheckbox.setChecked(false);
                    itemDto.setChecked(false);
                }else
                {
                    itemCheckbox.setChecked(true);
                    itemDto.setChecked(true);
                }

                //Toast.makeText(getApplicationContext(), "select item text : " + itemDto.getItemText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Return an initialize list of ListViewItemDTO.
    private List<ConfirmationPassengerItemDTO> getInitViewItemDtoList()
    {
        String itemTextArr[] = {"Android", "iOS", "Java", "JavaScript", "JDBC", "JSP", "Linux", "Python", "Servlet", "Windows"};

        List<ConfirmationPassengerItemDTO> ret = new ArrayList<ConfirmationPassengerItemDTO>();

        int length = itemTextArr.length;

        for(int i=0;i<length;i++)
        {
            String itemText = itemTextArr[i];

            ConfirmationPassengerItemDTO dto = new ConfirmationPassengerItemDTO();
            dto.setChecked(false);
            dto.setItemText(itemText);

            ret.add(dto);
        }

        return ret;
    }
}
