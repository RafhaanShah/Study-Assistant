package com.rafhaanshah.studyassistant.flashcards;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.HelperUtils;
import com.rafhaanshah.studyassistant.R;

import io.realm.Realm;
import io.realm.RealmList;

public class FlashCardSetRecyclerAdapter extends RecyclerView.Adapter<FlashCardSetRecyclerAdapter.ViewHolder> implements Filterable {
    private RealmList<FlashCardSet> values;
    private Context context;
    private Realm realm;

    FlashCardSetRecyclerAdapter(RealmList<FlashCardSet> data, Realm getRealm) {
        values = data;
        realm = getRealm;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_flash_card_set, parent, false);
        context = v.getContext();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int pos) {
        final int position = pos;
        final FlashCardSet item = values.get(position);

        holder.flashCardSetTitle.setText(item.getTitle());
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextScreen = new Intent(v.getContext(), FlashCardSetActivity.class);
                nextScreen.putExtra("item", item.getTitle());
                v.getContext().startActivity(nextScreen);
            }
        });

        holder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                showPopupMenu(holder, item, position);
                return true;
            }
        });
    }

    private void showPopupMenu(ViewHolder holder, final FlashCardSet item, final int position) {
        PopupMenu popup = new PopupMenu(context, holder.relativeLayout, Gravity.RIGHT);
        popup.inflate(R.menu.menu_popup);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.popup_edit:
                        renameFlashCardSet(item, position);
                        return true;
                    case R.id.popup_delete:
                        deleteFlashCardSet(item, position);
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void deleteFlashCardSet(final FlashCardSet item, final int position) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirm_delete))
                .setMessage(context.getString(R.string.delete_set))
                .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(@NonNull Realm realm) {
                                item.deleteFromRealm();
                            }
                        });
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


    private void renameFlashCardSet(final FlashCardSet item, final int position) {
        HelperUtils.showSoftKeyboard(context);

        final EditText input = new EditText(context);
        input.setText(item.getTitle());
        input.setSelectAllOnFocus(true);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.rename_flash_card_set));
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
                final String title = input.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(context, R.string.error_blank, Toast.LENGTH_SHORT).show();
                } else {
                    FlashCardSet set = realm.where(FlashCardSet.class).equalTo("title", title).findFirst();
                    if (set == null) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(@NonNull Realm realm) {
                                item.setTitle(title);
                            }

                        });
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, R.string.error_set_exists, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return values.size();
    }

    void updateData(RealmList<FlashCardSet> items) {
        values = items;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new FlashCardSetFilter(this);
    }

    public void filterResults(String query) {
        RealmList<FlashCardSet> set = new RealmList<>();
        if (!TextUtils.isEmpty(query)) {
            set.addAll(realm.where(FlashCardSet.class).contains("title", query).findAllSorted("title"));
            updateData(set);
        } else {
            set.addAll(realm.where(FlashCardSet.class).findAllSorted("title"));
            updateData(set);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView flashCardSetTitle;
        private RelativeLayout relativeLayout;

        ViewHolder(View v) {
            super(v);
            flashCardSetTitle = v.findViewById(R.id.flashCardSetTitle);
            relativeLayout = v.findViewById(R.id.relativeLayout);
        }
    }

    private class FlashCardSetFilter extends Filter {
        private final FlashCardSetRecyclerAdapter adapter;

        private FlashCardSetFilter(FlashCardSetRecyclerAdapter adapter) {
            super();
            this.adapter = adapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            return new FilterResults();
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.filterResults(constraint.toString());
        }
    }

}
