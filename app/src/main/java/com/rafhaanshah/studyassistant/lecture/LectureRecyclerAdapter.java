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
import java.util.Collections;
import java.util.Comparator;

public class LectureRecyclerAdapter extends RecyclerView.Adapter<LectureRecyclerAdapter.ViewHolder> {

    private ArrayList<File> files;
    private ArrayList<File> unFilteredFiles;
    private Context context;
    private int sorting;

    LectureRecyclerAdapter(int sort, ArrayList<File> newFiles) {
        sorting = sort;
        files = newFiles;
        unFilteredFiles = new ArrayList<>(0);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_lecture, parent, false);
        context = v.getContext();
        return new LectureRecyclerAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final LectureRecyclerAdapter.ViewHolder holder, int position) {
        final File lec = files.get(position);
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
                showPopupMenu(holder, lec, holder.getAdapterPosition());
                return true;
            }
        });
    }

    private void showPopupMenu(final LectureRecyclerAdapter.ViewHolder holder, final File lec, final int position) {
        PopupMenu popup = new PopupMenu(context, holder.relativeLayout, Gravity.END);
        popup.inflate(R.menu.menu_popup);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.popup_edit:
                        renameLecture(holder, lec, position);
                        return true;
                    case R.id.popup_delete:
                        deleteLecture(position);
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void renameLecture(ViewHolder holder, final File lec, final int position) {
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
                    File newFile = new File(HelperUtils.getLectureDirectory(context) + File.separator + text + ".pdf");
                    if (newFile.exists()) {
                        Toast.makeText(context, context.getString(R.string.error_rename), Toast.LENGTH_LONG).show();
                    } else if (!lec.renameTo(newFile)) {
                        Toast.makeText(context, context.getString(R.string.error_characters), Toast.LENGTH_LONG).show();
                    } else {
                        updateData(sorting, HelperUtils.getLectureFiles(context));
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    void deleteLecture(final int position) {
        final File lec = files.get(position);
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirm_delete))
                .setMessage(context.getString(R.string.delete_lecture))
                .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        files.remove(position);
                        notifyItemRemoved(position);
                        lec.delete();
                    }
                })
                .setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        notifyItemChanged(position);
                    }
                })
                .setIcon(R.drawable.ic_delete_black_24dp)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        notifyItemChanged(position);
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    void updateData(int sort, ArrayList<File> newFiles) {
        if (newFiles != null) {
            files = newFiles;
        }
        sortData(sort);
        notifyDataSetChanged();
    }

    private void sortData(int sorting) {
        switch (sorting) {
            case 0:
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File a, File b) {
                        return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
                    }
                });
                break;
            case 1:
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File a, File b) {
                        Long lng = (b.lastModified() - a.lastModified());
                        return lng.intValue();
                    }
                });
                break;
            case 2:
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File a, File b) {
                        Long lng = (b.length() - a.length());
                        return lng.intValue();
                    }
                });
                break;
        }
    }

    void filter(String query) {
        if (unFilteredFiles.size() == 0) {
            unFilteredFiles = files;
        }
        if (!TextUtils.isEmpty(query)) {
            ArrayList<File> filteredFiles = new ArrayList<>();
            for (File f : unFilteredFiles) {
                if (f.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredFiles.add(f);
                }
            }
            files = filteredFiles;
            notifyDataSetChanged();
        } else {
            files = unFilteredFiles;
            unFilteredFiles.clear();
            notifyDataSetChanged();
        }
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
