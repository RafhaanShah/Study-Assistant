package com.rafhaanshah.studyassistant.lecture;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.MainActivity;
import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.utils.HelperUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LectureRecyclerAdapter extends RecyclerView.Adapter<LectureRecyclerAdapter.ViewHolder> {

    private ArrayList<File> files;
    private ArrayList<File> filteredFiles;
    private RecyclerView recyclerView;
    private Context context;
    private int sorting;
    private AlertDialog dialog;

    LectureRecyclerAdapter(Context getContext, RecyclerView getRecyclerView, int sort, ArrayList<File> newFiles) {
        files = newFiles;
        recyclerView = getRecyclerView;
        sortData(sort, files);
        filteredFiles = files;
        sorting = sort;
        context = getContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_lecture, parent, false);
        return new LectureRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final LectureRecyclerAdapter.ViewHolder holder, final int position) {
        final File lec = filteredFiles.get(position);
        int offset = 9;
        String size = new DecimalFormat("#.##").format((double) lec.length() / 1000000);
        holder.lectureTitle.setText(lec.getName().substring(0, lec.getName().lastIndexOf(".")));
        holder.lectureSize.setText(context.getString(R.string.mb, size));
        holder.lectureDate.setText(DateFormat.getDateInstance().format(lec.lastModified()));
        holder.lectureLetter.setText(lec.getName().substring(0, 1).toUpperCase());
        holder.letterBackground.getBackground().setTint(HelperUtils.getColour(context, position + offset));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = FileProvider.getUriForFile(context, context.getString(R.string.file_provider_authority), lec);
                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setDataAndType(uri, MainActivity.TYPE_APPLICATION_PDF)
                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, context.getString(R.string.error_pdf), Toast.LENGTH_LONG).show();
                }
            }
        });
        setContextMenu(holder, lec);
    }

    @Override
    public int getItemCount() {
        return filteredFiles.size();
    }

    private void setContextMenu(final ViewHolder holder, final File lec) {
        holder.cardView.setOnCreateContextMenuListener(
                new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        menu.add(context.getString(R.string.rename)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                renameLecture(lec);
                                return true;
                            }
                        });
                        menu.add(context.getString(R.string.delete)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                deleteLecture(holder.getAdapterPosition());
                                return true;
                            }
                        });
                    }
                }
        );
    }

    private void renameLecture(final File lec) {
        final EditText input = new EditText(context);
        input.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        input.setText(lec.getName().substring(0, lec.getName().lastIndexOf(".")));
        input.setSelectAllOnFocus(true);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
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
        builder.setIcon(R.drawable.ic_edit_black_24dp);
        builder.setView(input);
        dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = input.getText().toString().trim();
                if (TextUtils.isEmpty(text)) {
                    input.setError(context.getString(R.string.blank_input));
                } else {
                    File newFile = new File(HelperUtils.getLectureDirectory(context) + File.separator + text + MainActivity.PDF);
                    if (newFile.exists()) {
                        input.setError(context.getString(R.string.already_exists));
                    } else if (!lec.renameTo(newFile)) {
                        input.setError(context.getString(R.string.error_characters));
                    } else {
                        updateData(sorting, HelperUtils.getLectureFiles(context));
                        dialog.dismiss();
                    }
                }
            }
        });
        HelperUtils.showSoftKeyboard(context, input);
    }

    void deleteLecture(final int position) {
        final File lec = filteredFiles.get(position);
        dialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirm_delete))
                .setMessage(context.getString(R.string.delete_lecture))
                .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        filteredFiles.remove(position);
                        ((Activity) context).closeContextMenu();
                        notifyItemRemoved(position);
                        Snackbar.make(((Activity) context).findViewById(R.id.content), context.getString(R.string.deleted), Snackbar.LENGTH_SHORT).show();
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
                .create();
        dialog.show();
    }

    void updateData(int sort, ArrayList<File> newFilteredFiles) {
        if (newFilteredFiles != null) {
            filteredFiles = newFilteredFiles;
            files = filteredFiles;
        }
        sorting = sort;
        sortData(sort, files);
        filteredFiles = files;
        animateList();
    }

    void animateList() {
        final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(controller);
        notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    private void sortData(int sort, ArrayList<File> unsortedFiles) {
        switch (sort) {
            case MainActivity.SORT_TITLE:
                Collections.sort(unsortedFiles, new Comparator<File>() {
                    @Override
                    public int compare(File a, File b) {
                        return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
                    }
                });
                break;
            case MainActivity.SORT_DATE:
                Collections.sort(unsortedFiles, new Comparator<File>() {
                    @Override
                    public int compare(File a, File b) {
                        Long lng = (b.lastModified() - a.lastModified());
                        return lng.intValue();
                    }
                });
                break;
            case MainActivity.SORT_SIZE:
                Collections.sort(unsortedFiles, new Comparator<File>() {
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
        if (!TextUtils.isEmpty(query)) {
            filteredFiles = new ArrayList<>(files.size());
            for (File f : files) {
                if (f.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredFiles.add(f);
                }
            }
        } else {
            filteredFiles = files;
        }
        animateList();
    }

    void dismissDialog() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView lectureTitle;
        private TextView lectureSize;
        private TextView lectureDate;
        private TextView lectureLetter;
        private CardView cardView;
        private RelativeLayout letterBackground;

        ViewHolder(View view) {
            super(view);
            lectureTitle = view.findViewById(R.id.tv_lecture_title);
            lectureSize = view.findViewById(R.id.tv_lecture_size);
            lectureDate = view.findViewById(R.id.lectureDate);
            lectureLetter = view.findViewById(R.id.tv_letter);
            cardView = view.findViewById(R.id.card_view);
            letterBackground = view.findViewById(R.id.letter_bg);
        }
    }
}
