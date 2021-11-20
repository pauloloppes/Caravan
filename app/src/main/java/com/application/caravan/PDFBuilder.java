package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.application.entities.Passenger;
import com.application.entities.Trip;
import com.application.utils.ConfirmationPassengerItemDTO;
import com.application.utils.DBLink;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tejpratapsingh.pdfcreator.activity.PDFCreatorActivity;
import com.tejpratapsingh.pdfcreator.utils.PDFUtil;
import com.tejpratapsingh.pdfcreator.views.PDFBody;
import com.tejpratapsingh.pdfcreator.views.PDFFooterView;
import com.tejpratapsingh.pdfcreator.views.PDFHeaderView;
import com.tejpratapsingh.pdfcreator.views.PDFTableView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFHorizontalView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFImageView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFLineSeparatorView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFPageBreakView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFTextView;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PDFBuilder extends PDFCreatorActivity {

    private Trip t;
    private DBLink dbLink;
    private List<Passenger> passengerlist;
    private int numPassengers;
    private int numProcessed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_pdfbuilder);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        t = (Trip) getIntent().getParcelableExtra("trip");
        if (t == null) {
            toastShow("Falha ao carregar dados da viagem");
            finish();
        }

        dbLink = new DBLink();
        passengerlist = new ArrayList<>();
        numPassengers = 0;
        numProcessed = 0;

        searchPassengersOnDB();



    }

    private void searchPassengersOnDB() {

        OnCompleteListener listenerComplete = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    numPassengers = task.getResult().size();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        searchPassengerByID(document.get("passageiro").toString());
                    }

                    //listViewWithCheckbox.setAdapter(listViewDataAdapter);

                } else
                    toastShow("Erro ao acessar documentos: "+task.getException());
            }
        };

        dbLink.getAllPassengersFromTrip(t.getId(), listenerComplete);

    }

    private void searchPassengerByID(String pasID) {

        OnCompleteListener listenerComplete = new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    Passenger p = doc.toObject(Passenger.class);
                    passengerlist.add(p);
                    sortLists();
                    numProcessed += 1;
                    if (numProcessed == numPassengers) {
                        callCreate();
                    }
                }
            }
        };

        dbLink.getPassengerById(pasID, listenerComplete);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sortLists() {
        if (passengerlist != null && passengerlist.size() > 1) {
            Collections.sort(passengerlist, new Comparator<Passenger>() {
                @Override
                public int compare(Passenger p1, Passenger p2) {
                    return p1.getNome().compareToIgnoreCase(p2.getNome());
                }
            });
        }
        /*if (passengerlist != null && passengerlist.size() > 1) {
            passengerlist.sort(Comparator.comparing(Passenger::getNome));
        }*/
    }

    private void callCreate(){
        createPDF("test", new PDFUtil.PDFUtilListener() {
            @Override
            public void pdfGenerationSuccess(File savedPDFFile) {
                //Toast.makeText(PDFBuilder.this, "PDF Created", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void pdfGenerationFailure(Exception exception) {
                //Toast.makeText(PDFBuilder.this, "PDF NOT Created", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected PDFHeaderView getHeaderView(int forPage) {

        PDFHeaderView headerView = new PDFHeaderView(getApplicationContext());

        PDFHorizontalView horizontalView = new PDFHorizontalView(getApplicationContext());

        PDFTextView pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString word = new SpannableString(t.getNome());
        word.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        pdfTextView.setText(word);
        pdfTextView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        pdfTextView.getView().setGravity(Gravity.CENTER_VERTICAL);
        pdfTextView.getView().setTypeface(pdfTextView.getView().getTypeface(), Typeface.BOLD);

        horizontalView.addView(pdfTextView);

        PDFImageView imageView = new PDFImageView(getApplicationContext());
        LinearLayout.LayoutParams imageLayoutParam = new LinearLayout.LayoutParams(
                60,
                60, 0);
        imageView.setImageScale(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageResource(R.mipmap.ic_launcher);
        imageLayoutParam.setMargins(0, 0, 10, 0);
        imageView.setLayout(imageLayoutParam);

        horizontalView.addView(imageView);

        headerView.addView(horizontalView);

        PDFLineSeparatorView lineSeparatorView1 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        headerView.addView(lineSeparatorView1);

        return headerView;
    }

    @Override
    protected PDFBody getBodyViews() {
        PDFBody pdfBody = new PDFBody();

        //PDFTextView pdfCompanyNameView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.H3);
        //pdfCompanyNameView.setText("Company Name");
        //pdfBody.addView(pdfCompanyNameView);
        //PDFLineSeparatorView lineSeparatorView1 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        //pdfBody.addView(lineSeparatorView1);
        //PDFTextView pdfAddressView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
        //pdfAddressView.setText("Address Line 1\nCity, State - 123456");
        //pdfBody.addView(pdfAddressView);

        /*PDFLineSeparatorView lineSeparatorView2 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        lineSeparatorView2.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                8, 0));
        pdfBody.addView(lineSeparatorView2);*/

        /*PDFLineSeparatorView lineSeparatorView3 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        pdfBody.addView(lineSeparatorView3);*/

        int[] widthPercent = {50, 25, 25}; // Sum should be equal to 100%
        String[] textInTable = {"1", "2", "3"};
        //PDFTextView pdfTableTitleView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
        //pdfTableTitleView.setText("List");
        //pdfBody.addView(pdfTableTitleView);

        //final PDFPageBreakView pdfPageBreakView = new PDFPageBreakView(getApplicationContext());
        //pdfBody.addView(pdfPageBreakView);

        PDFTableView.PDFTableRowView tableHeader = new PDFTableView.PDFTableRowView(getApplicationContext());
        PDFTextView pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
        pdfTextView.setText("Nome");
        tableHeader.addToRow(pdfTextView);
        pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
        pdfTextView.setText("Documento");
        tableHeader.addToRow(pdfTextView);
        pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
        pdfTextView.setText("Telefone");
        tableHeader.addToRow(pdfTextView);

        /*for (String s : textInTable) {
            PDFTextView pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
            pdfTextView.setText("Header Title: " + s);
            tableHeader.addToRow(pdfTextView);
        }*/

        PDFTableView.PDFTableRowView tableRowView1 = new PDFTableView.PDFTableRowView(getApplicationContext());
        for (String s : textInTable) {
            pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
            pdfTextView.setText(" ");
            tableRowView1.addToRow(pdfTextView);
        }

        PDFTableView tableView = new PDFTableView(getApplicationContext(), tableHeader, tableRowView1);

        for (Passenger p : passengerlist) {
            PDFTableView.PDFTableRowView tableRowView = new PDFTableView.PDFTableRowView(getApplicationContext());
            pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
            pdfTextView.setText(p.getNome());
            tableRowView.addToRow(pdfTextView);
            pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
            pdfTextView.setText(p.getIdentidade());
            tableRowView.addToRow(pdfTextView);
            pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
            pdfTextView.setText(p.getTelefone());
            tableRowView.addToRow(pdfTextView);
            tableView.addRow(tableRowView);
        }

        /*for (int i = 0; i < 40; i++) {
            // Create 10 rows
            PDFTableView.PDFTableRowView tableRowView = new PDFTableView.PDFTableRowView(getApplicationContext());
            for (String s : textInTable) {
                pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                pdfTextView.setText("Row " + (i + 2) + ": " + s);
                tableRowView.addToRow(pdfTextView);
            }
            tableView.addRow(tableRowView);
        }*/
        tableView.setColumnWidth(widthPercent);
        pdfBody.addView(tableView);

        PDFLineSeparatorView lineSeparatorView1 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.WHITE);
        pdfBody.addView(lineSeparatorView1);
        PDFLineSeparatorView lineSeparatorView4 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
        pdfBody.addView(lineSeparatorView4);

        //PDFTextView pdfIconLicenseView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.H3);
        //Spanned icon8Link = HtmlCompat.fromHtml("Icon from <a href='https://icons8.com'>https://icons8.com</a>", HtmlCompat.FROM_HTML_MODE_LEGACY);
        //pdfIconLicenseView.getView().setText(icon8Link);
        //pdfBody.addView(pdfIconLicenseView);

        return pdfBody;
    }

    @Override
    protected PDFFooterView getFooterView(int forPage) {
        PDFFooterView footerView = new PDFFooterView(getApplicationContext());

        PDFTextView pdfTextViewPage = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL);
        pdfTextViewPage.setText(String.format(Locale.getDefault(), "Página %d", forPage + 1));
        pdfTextViewPage.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 0));
        pdfTextViewPage.getView().setGravity(Gravity.CENTER_HORIZONTAL);

        footerView.addView(pdfTextViewPage);

        return footerView;
    }

    @Override
    protected void onNextClicked(File savedPDFFile) {

        File newFile = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"/"+t.getNome()+".pdf");
        if (savedPDFFile.renameTo(newFile)) {
            Toast.makeText(PDFBuilder.this, "Arquivo foi salvo na sua pasta Documentos!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(PDFBuilder.this, "Ocorreu um erro ao salvar seu arquivo!", Toast.LENGTH_LONG).show();
        }

        //Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        //pdfFile = savedPDFFile;
        //i.addCategory(Intent.CATEGORY_DEFAULT);
        //startActivityForResult(Intent.createChooser(i, "Escolha pasta para salvar"), SAVE_PDF);
        //finish();
        //Uri pdfUri = Uri.fromFile(savedPDFFile);

        //Intent intentPdfViewer = new Intent(PDFBuilder.this, PDFBuilder.class);
        //intentPdfViewer.putExtra(PDFBuilder.PDF_FILE_URI, pdfUri);

        //startActivity(intentPdfViewer);
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SAVE_PDF) {
            try {
                System.out.println("\t\t\tDIRETORIO: "+pdfFile.getPath());
                System.out.println("\t\t\tDESTINO: "+data.getDataString());
                File newFile = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"/carav.pdf");
                if (pdfFile.renameTo(newFile)) {
                    System.out.println(":::::DEU CERTO EU ACHO");
                } else {
                    System.out.println(":::::DEU ERRO AAAAAAA");
                }
                //Toast.makeText(PDFBuilder.this, "SALVOU EEEEE", Toast.LENGTH_SHORT).show();
                //FileWriter writer = new FileWriter(pdfFile);
                //writer.append(sBody);
                //writer.flush();
                //writer.close();
            } catch (Exception e) {
                System.out.println(":::::EXCEPTION: "+e.getMessage());
                Toast.makeText(PDFBuilder.this, "Erro ao salvar arquivo: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}