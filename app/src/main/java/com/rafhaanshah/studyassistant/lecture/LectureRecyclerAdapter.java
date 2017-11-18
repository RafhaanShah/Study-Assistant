package com.rafhaanshah.studyassistant.lecture;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.MainActivity;
import com.rafhaanshah.studyassistant.R;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class LectureRecyclerAdapter extends RecyclerView.Adapter<LectureRecyclerAdapter.ViewHolder> {

    private ArrayList<File> values;
    private Context context;

    LectureRecyclerAdapter(ArrayList<File> data) {
        values = data;
    }

    @Override
    public LectureRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.lecture_item, parent, false);
        context = v.getContext();
        return new LectureRecyclerAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(LectureRecyclerAdapter.ViewHolder holder, final int position) {
        final File lec = values.get(position);
        holder.lectureTitle.setText(lec.getName().substring(0, lec.getName().lastIndexOf(".")));
        holder.lectureSize.setText(new DecimalFormat("#.##").format((double) lec.length() / 1048576) + " MB");
        holder.lectureDate.setText(DateFormat.getDateInstance().format(lec.lastModified()));


        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".com.rafhaanshah.studyassistant.GenericFileProvider", lec);
                intent.setDataAndType(uri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            }
        });

        holder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this lecture file?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ((MainActivity) context).deleteLecture(lec);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(R.drawable.ic_delete_black_24dp)
                        .show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    void updateData(ArrayList<File> items) {
        values = items;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView lectureTitle;
        private TextView lectureSize;
        private TextView lectureDate;
        private RelativeLayout relativeLayout;

        ViewHolder(View v) {
            super(v);
            lectureTitle = v.findViewById(R.id.lectureTitle);
            lectureSize = v.findViewById(R.id.lectureSize);
            lectureDate = v.findViewById(R.id.lectureDate);
            relativeLayout = v.findViewById(R.id.relativeLayout);
        }
    }
}
