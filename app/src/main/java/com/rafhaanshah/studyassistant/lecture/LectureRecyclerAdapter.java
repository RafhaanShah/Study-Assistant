package com.rafhaanshah.studyassistant.lecture;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.HelperUtils;
import com.rafhaanshah.studyassistant.R;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class LectureRecyclerAdapter extends RecyclerView.Adapter<LectureRecyclerAdapter.ViewHolder> {

    private ArrayList<File> values;
    private Context context;
    private File directory;
    private LectureListFragment lectureListFragment;

    LectureRecyclerAdapter(ArrayList<File> data, LectureListFragment fragment) {
        values = data;
        lectureListFragment = fragment;
    }

    @Override
    public LectureRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_lecture, parent, false);
        context = v.getContext();
        directory = new File(context.getFilesDir().getAbsolutePath() + File.separator + "lectures");
        return new LectureRecyclerAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final LectureRecyclerAdapter.ViewHolder holder, final int position) {
        final File lec = values.get(position);
        String size = new DecimalFormat("#.##").format((double) lec.length() / 1000000);
        holder.lectureTitle.setText(lec.getName().substring(0, lec.getName().lastIndexOf(".")));
        holder.lectureSize.setText(context.getString(R.string.mb, size));
        holder.lectureDate.setText(DateFormat.getDateInstance().format(lec.lastModified()));

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".com.rafhaanshah.studyassistant.GenericFileProvider", lec);
                intent.setDataAndType(uri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, context.getString(R.string.error_pdf), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
        });

        holder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                showPopupMenu(holder, lec);
                return true;
            }
        });
    }

    private void showPopupMenu(final LectureRecyclerAdapter.ViewHolder holder, final File lec) {
        PopupMenu popup = new PopupMenu(context, holder.relativeLayout, Gravity.RIGHT);
        popup.inflate(R.menu.menu_popup);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.popup_edit:
                        renameLecture(holder, lec);
                        return true;
                    case R.id.popup_delete:
                        deleteLecture(lec);
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void renameLecture(ViewHolder holder, final File lec) {
        HelperUtils.showSoftKeyboard(context);

        final EditText input = new EditText(context);
        input.setText(holder.lectureTitle.getText());
        input.setSelectAllOnFocus(true);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.rename_file));
        builder.setPositiveButton(context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setIcon(R.drawable.ic_create_black_24dp);
        builder.setView(input);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = input.getText().toString().trim();
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(context, context.getString(R.string.error_blank), Toast.LENGTH_LONG).show();
                } else {
                    File newFile = new File(directory.getAbsolutePath() + File.separator + text + ".pdf");
                    if (newFile.exists()) {
                        Toast.makeText(context, context.getString(R.string.error_rename), Toast.LENGTH_LONG).show();
                    } else if (!lec.renameTo(newFile)) {
                        Toast.makeText(context, context.getString(R.string.error_characters), Toast.LENGTH_LONG).show();
                    } else {
                        lectureListFragment.updateData();
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    private void deleteLecture(final File lec) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirm_delete))
                .setMessage(context.getString(R.string.delete_lecture))
                .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        lec.delete();
                        lectureListFragment.updateData();
                    }
                })
                .setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.ic_delete_black_24dp)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                    }
                })
                .show();
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
