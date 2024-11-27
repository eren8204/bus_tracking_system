package com.example.hackathon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PdfAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<PdfItem> pdfItems;

    public PdfAdapter(Context context, ArrayList<PdfItem> pdfItems) {
        this.context = context;
        this.pdfItems = pdfItems;
    }

    @Override
    public int getCount() {
        return pdfItems.size();
    }

    @Override
    public Object getItem(int position) {
        return pdfItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_pdf, parent, false);
        }

        PdfItem pdfItem = pdfItems.get(position);

        TextView fileNameTextView = convertView.findViewById(R.id.fileNameTextView);
        TextView addedDateTextView = convertView.findViewById(R.id.addedDateTextView);

        fileNameTextView.setText(pdfItem.getFileName());
        addedDateTextView.setText(pdfItem.getAddedDate());

        return convertView;
    }
}
