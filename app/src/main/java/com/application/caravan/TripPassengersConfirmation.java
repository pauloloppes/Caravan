package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.application.entities.Passenger;
import com.application.entities.Trip;
import com.application.utils.ConfirmationPassengerItemDTO;
import com.application.utils.CustomAdapterConfirmation;
import com.application.utils.CustomAdapterPassenger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TripPassengersConfirmation extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore databasePassengers;
    private FirebaseUser currentUser;
    private Trip t;
    private AppCompatButton buttonSelectAll;
    private AppCompatButton buttonDeselectAll;
    private AppCompatButton buttonConfirmationBack;
    private final List<ConfirmationPassengerItemDTO> initItemList = new ArrayList<ConfirmationPassengerItemDTO>();
    private final CustomAdapterConfirmation listViewDataAdapter = new CustomAdapterConfirmation(this, initItemList);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_passengers_confirmation);

        mAuth = FirebaseAuth.getInstance();
        databasePassengers = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        t = (Trip) getIntent().getParcelableExtra("trip");
        if (t == null) {
            toastShow("Falha ao carregar dados da viagem");
            finish();
        }

        // Get listview checkbox.
        final ListView listViewWithCheckbox = (ListView)findViewById(R.id.listConfirmation);
        buttonSelectAll = (AppCompatButton)findViewById(R.id.buttonSelectAll);
        buttonDeselectAll = (AppCompatButton)findViewById(R.id.buttonDeselectAll);
        buttonConfirmationBack = (AppCompatButton)findViewById(R.id.buttonConfirmationBack);
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
                }else
                {
                    itemCheckbox.setChecked(true);
                    itemDto.setChecked(true);
                }

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

    private void setAllPassengers(boolean status) {
        int size = initItemList.size();
        for(int i=0;i<size;i++)
        {
            ConfirmationPassengerItemDTO dto = initItemList.get(i);
            dto.setChecked(status);
        }

        listViewDataAdapter.notifyDataSetChanged();
    }

    private void searchPassengersOnDB(final ListView listViewWithCheckbox) {
        if (currentUser!=null) {
            databasePassengers.collection(currentUser.getUid())
                    .document("dados")
                    .collection("pasviagem")
                    .whereEqualTo("viagem",t.getId())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    searchPassengerByID(document.get("passageiro").toString(), listViewWithCheckbox);
                                }


                                //listViewWithCheckbox.setAdapter(listViewDataAdapter);

                            } else
                                toastShow("Erro ao acessar documentos: "+task.getException());
                        }
                    });
        } else {
            toastShow("Erro ao carregar usuário");
        }
    }

    private void sortLists() {
        if (initItemList != null && initItemList.size() > 1) {
            Collections.sort(initItemList, new Comparator<ConfirmationPassengerItemDTO>() {
                @Override
                public int compare(ConfirmationPassengerItemDTO p1, ConfirmationPassengerItemDTO p2) {
                    return p1.getItemText().compareTo(p2.getItemText());
                }
            });
        }
    }

    private void searchPassengerByID(String pasID,final ListView listViewWithCheckbox) {
        databasePassengers.collection(currentUser.getUid())
                .document("dados")
                .collection("passageiros")
                .document(pasID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                            listViewWithCheckbox.invalidateViews();
                        }
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

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
