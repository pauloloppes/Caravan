package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.application.entities.Passenger;
import com.application.entities.Trip;
import com.application.utils.ConfirmationPassengerItemDTO;
import com.application.utils.CustomAdapterConfirmation;
import com.application.utils.CustomAdapterPassenger;
import com.application.utils.DBLink;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TripPassengersConfirmation extends AppCompatActivity {

    private Trip t;
    private TextView labelConfirmationMarked;
    private TextView labelConfirmationUnmarked;
    private TextView labelConfirmationPercentage;
    private ProgressBar barConfirmationPercentage;
    private AppCompatButton buttonSelectAll;
    private AppCompatButton buttonDeselectAll;
    private AppCompatButton buttonConfirmationBack;
    private final List<ConfirmationPassengerItemDTO> initItemList = new ArrayList<ConfirmationPassengerItemDTO>();
    private final CustomAdapterConfirmation listViewDataAdapter = new CustomAdapterConfirmation(this, initItemList);
    private DBLink dbLink;
    private int totalPas;
    private int checked;
    private int unchecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_passengers_confirmation);

        t = (Trip) getIntent().getParcelableExtra("trip");
        if (t == null) {
            toastShow("Falha ao carregar dados da viagem");
            finish();
        }

        dbLink = new DBLink();


        // Get listview checkbox.
        final ListView listViewWithCheckbox = (ListView)findViewById(R.id.listConfirmation);
        buttonSelectAll = (AppCompatButton)findViewById(R.id.buttonSelectAll);
        buttonDeselectAll = (AppCompatButton)findViewById(R.id.buttonDeselectAll);
        buttonConfirmationBack = (AppCompatButton)findViewById(R.id.buttonConfirmationBack);
        labelConfirmationMarked = (TextView) findViewById(R.id.labelConfirmationMarked);
        labelConfirmationPercentage = (TextView) findViewById(R.id.labelConfirmationPercentage);
        labelConfirmationUnmarked = (TextView) findViewById(R.id.labelConfirmationUnmarked);
        barConfirmationPercentage = (ProgressBar) findViewById(R.id.barConfirmationPercentage);
        searchPassengersOnDB(listViewWithCheckbox);

        // Initiate listview data.
        //initItemList = this.getInitViewItemDtoList();

        // Create a custom list view adapter with checkbox control.

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
                    checked-=1;
                    unchecked+=1;
                }else
                {
                    itemCheckbox.setChecked(true);
                    itemDto.setChecked(true);
                    checked+=1;
                    unchecked-=1;
                }

                updateScreen();

                //Toast.makeText(getApplicationContext(), "select item text : " + itemDto.getItemText(), Toast.LENGTH_SHORT).show();
            }
        });

        buttonSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllPassengers(true);
            }
        });

        buttonDeselectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllPassengers(false);
            }
        });

        buttonConfirmationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void enableScreen() {

        setAllPassengers(false);
        checked = 0;
        unchecked = totalPas;

    }

    private void updateScreen() {
        labelConfirmationMarked.setText("Presentes: "+checked);
        labelConfirmationUnmarked.setText("Ausentes: "+unchecked);
        int percentage = (int) Math.ceil(((double) checked / (double) totalPas) * 100);
        labelConfirmationPercentage.setText("Porcentagem de presentes: "+percentage+"%");
        barConfirmationPercentage.setProgress(percentage);
    }

    private void setAllPassengers(boolean status) {
        int size = initItemList.size();
        for(int i=0;i<size;i++)
        {
            ConfirmationPassengerItemDTO dto = initItemList.get(i);
            dto.setChecked(status);
        }
        if (status) {
            checked = totalPas;
            unchecked = 0;
        } else {
            checked = 0;
            unchecked = totalPas;
        }
        updateScreen();

        listViewDataAdapter.notifyDataSetChanged();
    }

    private void searchPassengersOnDB(final ListView listViewWithCheckbox) {

        OnCompleteListener listenerComplete = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    totalPas = task.getResult().size();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        searchPassengerByID(document.get("passageiro").toString(), listViewWithCheckbox);
                    }

                    //listViewWithCheckbox.setAdapter(listViewDataAdapter);

                } else
                    toastShow("Erro ao acessar documentos: "+task.getException());
            }
        };

        dbLink.getAllPassengersFromTrip(t.getId(), listenerComplete);

    }

    private void sortLists() {
        if (initItemList != null && initItemList.size() > 1) {
            Collections.sort(initItemList, new Comparator<ConfirmationPassengerItemDTO>() {
                @Override
                public int compare(ConfirmationPassengerItemDTO p1, ConfirmationPassengerItemDTO p2) {
                    return p1.getItemText().compareToIgnoreCase(p2.getItemText());
                }
            });

        }
    }

    private void searchPassengerByID(String pasID,final ListView listViewWithCheckbox) {

        OnCompleteListener listenerComplete = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    Passenger p = doc.toObject(Passenger.class);
                    ConfirmationPassengerItemDTO dto = new ConfirmationPassengerItemDTO();
                    dto.setChecked(false);
                    dto.setItemText(p.getNome());
                    initItemList.add(dto);
                    sortLists();
                    updateScreen();
                    if (initItemList.size() == totalPas) {
                        enableScreen();
                    }
                    listViewWithCheckbox.invalidateViews();
                }
            }
        };

        dbLink.getPassengerById(pasID, listenerComplete);

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

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
