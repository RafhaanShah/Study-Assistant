package com.rafhaanshah.studyassistant.lecture;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.R;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class LectureRecyclerAdapter extends RecyclerView.Adapter<LectureRecyclerAdapter.ViewHolder> {

    private ArrayList<File> values;
    private Context context;
    private File directory;

    LectureRecyclerAdapter(ArrayList<File> data) {
        values = data;
    }

    @Override
    public LectureRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.lecture_item, parent, false);
        context = v.getContext();
        directory = new File(context.getFilesDir().getAbsolutePath() + File.separator + "lectures");
        return new LectureRecyclerAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final LectureRecyclerAdapter.ViewHolder holder, final int position) {
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
            public boolean onLongClick(final View v) {
                final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                final EditText input = new EditText(context);
                input.setText(holder.lectureTitle.getText());
                input.setSelectAllOnFocus(true);

                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                new AlertDialog.Builder(context)
                        .setTitle("Rename File")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                File newFile = new File(directory.getAbsolutePath() + File.separator + input.getText().toString() + ".pdf");
                                if (newFile.exists()) {
                                    Toast.makeText(context, "File with that name already exists", Toast.LENGTH_LONG).show();
                                } else if (!lec.renameTo(newFile)) {
                                    Toast.makeText(context, "Invalid characters entered", Toast.LENGTH_LONG).show();
                                } else {
                                    holder.lectureTitle.setText(input.getText().toString());
                                }
                                if (imm != null)
                                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (imm != null)
                                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                            }
                        })
                        .setIcon(R.drawable.ic_create_black_24dp)
                        .setView(input)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                Toast.makeText(context, "ON CANCEL", Toast.LENGTH_SHORT).show();
                                if (imm != null)
                                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                            }
                        })
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
