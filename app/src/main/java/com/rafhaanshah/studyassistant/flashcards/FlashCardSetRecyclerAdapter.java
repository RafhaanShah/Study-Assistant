package com.rafhaanshah.studyassistant.flashcards;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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

import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.utils.HelperUtils;

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
    private AlertDialog dialog;

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
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final FlashCardSet flashCardSet = filteredSets.get(position);

        holder.flashCardSetTitle.setText(flashCardSet.getTitle());
        holder.cardView.setCardBackgroundColor(HelperUtils.getColour(context, position));
        //holder.relativeLayout.setBackgroundColor((HelperUtils.getColour(context, position)));

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(FlashCardSetActivity.getStartIntent(context, flashCardSet.getTitle(), holder.getAdapterPosition()));
            }
        });
        setContextMenu(holder, flashCardSet);
    }

    @Override
    public int getItemCount() {
        return filteredSets.size();
    }

    private void setContextMenu(final ViewHolder holder, final FlashCardSet set) {
        holder.relativeLayout.setOnCreateContextMenuListener(
                new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        menu.add(context.getString(R.string.rename)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                renameFlashCardSet(set);
                                return true;
                            }
                        });
                        menu.add(context.getString(R.string.delete)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                deleteFlashCardSet(holder.getAdapterPosition());
                                return true;
                            }
                        });
                    }
                }
        );
    }

    void deleteFlashCardSet(final int position) {
        final FlashCardSet set = filteredSets.get(position);
        dialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirm_delete))
                .setMessage(context.getString(R.string.delete_set))
                .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(@NonNull Realm realm) {
                                set.deleteFromRealm();
                            }
                        });
                        notifyItemRemoved(position);
                        Snackbar.make(((Activity) context).findViewById(R.id.content), context.getString(R.string.deleted), Snackbar.LENGTH_SHORT).show();
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

    private void renameFlashCardSet(final FlashCardSet flashCardSet) {
        final EditText input = new EditText(context);
        input.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        input.setText(flashCardSet.getTitle());
        input.setSelectAllOnFocus(true);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
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
        dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String title = input.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    input.setError(context.getString(R.string.blank_input));
                } else {
                    FlashCardSet set = realm.where(FlashCardSet.class).equalTo(FlashCardSet.FlashCardSet_TITLE, title).findFirst();
                    if (set == null) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(@NonNull Realm realm) {
                                flashCardSet.setTitle(title);
                            }

                        });
                        dialog.dismiss();
                    } else {
                        input.setError(context.getString(R.string.already_exists));
                    }
                }
            }
        });
        HelperUtils.showSoftKeyboard(context, input);
    }

    private void resetList() {
        filteredSets = flashCardSets;
    }

    int getSetPosition(String title) {
        for (FlashCardSet f : filteredSets) {
            if (f.getTitle().equals(title)) {
                return filteredSets.indexOf(f);
            }
        }
        return 0;
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
            public void onChange(@NonNull RealmResults<FlashCardSet> flashCardSets) {
                notifyItemRangeChanged(0, flashCardSets.size());
            }
        });
    }

    void removeListener() {
        flashCardSets.removeAllChangeListeners();
    }

    void dismissDialog() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView flashCardSetTitle;
        private RelativeLayout relativeLayout;
        private CardView cardView;

        ViewHolder(View view) {
            super(view);
            flashCardSetTitle = view.findViewById(R.id.tv_flash_card_set_title);
            relativeLayout = view.findViewById(R.id.flash_card_layout);
            cardView = view.findViewById(R.id.flash_card_view);
        }
    }
}
