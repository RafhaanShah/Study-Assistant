package com.rafhaanshah.studyassistant.flashcards;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.HelperUtils;
import com.rafhaanshah.studyassistant.R;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class FlashCardSetRecyclerAdapter extends RecyclerView.Adapter<FlashCardSetRecyclerAdapter.ViewHolder> {
    private RealmResults<FlashCardSet> flashCardSets;
    private RealmResults<FlashCardSet> filteredSets;
    private Context context;
    private Realm realm;
    private RecyclerView recyclerView;

    FlashCardSetRecyclerAdapter(Context getContext, Realm getRealm, RecyclerView getRecyclerView) {
        context = getContext;
        realm = getRealm;
        recyclerView = getRecyclerView;
        flashCardSets = realm.where(FlashCardSet.class).findAllSorted(FlashCardSet.FlashCardSet_TITLE, Sort.ASCENDING);
        filteredSets = flashCardSets;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_flash_card_set, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final FlashCardSet item = filteredSets.get(position);

        holder.flashCardSetTitle.setText(item.getTitle());
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(FlashCardSetActivity.getStartIntent(context, item.getTitle()));
                ((Activity) context).overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        holder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                showPopupMenu(holder, item, holder.getAdapterPosition());
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredSets.size();
    }

    private void showPopupMenu(ViewHolder holder, final FlashCardSet item, final int position) {
        PopupMenu popup = new PopupMenu(context, holder.relativeLayout, Gravity.END);
        popup.inflate(R.menu.activity_main_popup);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.popup_edit:
                        renameFlashCardSet(item);
                        return true;
                    case R.id.popup_delete:
                        deleteFlashCardSet(position);
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    void deleteFlashCardSet(final int position) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirm_delete))
                .setMessage(context.getString(R.string.delete_set))
                .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(@NonNull Realm realm) {
                                filteredSets.get(position).deleteFromRealm();
                            }
                        });
                        notifyItemRemoved(position);
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

    private void renameFlashCardSet(final FlashCardSet item) {
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
        builder.setIcon(R.drawable.ic_edit_black_24dp);
        builder.setView(input);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String title = input.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(context, R.string.error_blank, Toast.LENGTH_SHORT).show();
                } else {
                    FlashCardSet set = realm.where(FlashCardSet.class).equalTo(FlashCardSet.FlashCardSet_TITLE, title).findFirst();
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

    private void resetList() {
        filteredSets = flashCardSets;
    }

    void filter(String query) {
        if (!TextUtils.isEmpty(query)) {
            filteredSets = flashCardSets.where().contains(FlashCardSet.FlashCardSet_TITLE, query.toLowerCase(), Case.INSENSITIVE).findAllSorted(FlashCardSet.FlashCardSet_TITLE, Sort.ASCENDING);
        } else {
            resetList();
        }
        animateList();
    }

    void animateList() {
        final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_from_bottom);
        recyclerView.setLayoutAnimation(controller);
        notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    void addListener() {
        flashCardSets.addChangeListener(new RealmChangeListener<RealmResults<FlashCardSet>>() {
            @Override
            public void onChange(@NonNull RealmResults<FlashCardSet> items) {
                notifyItemRangeChanged(0, items.size());
            }
        });
    }

    void removeListener() {
        flashCardSets.removeAllChangeListeners();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView flashCardSetTitle;
        private RelativeLayout relativeLayout;

        ViewHolder(View view) {
            super(view);
            flashCardSetTitle = view.findViewById(R.id.tv_flash_card_set_title);
            relativeLayout = view.findViewById(R.id.flash_card_relative_layout);
        }
    }
}
