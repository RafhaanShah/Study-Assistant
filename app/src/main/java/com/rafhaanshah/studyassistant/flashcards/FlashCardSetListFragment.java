package com.rafhaanshah.studyassistant.flashcards;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.utils.HelperUtils;

import io.realm.Realm;
import io.realm.RealmList;

public class FlashCardSetListFragment extends Fragment {

    private FlashCardSetRecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private Realm realm;
    private AlertDialog dialog;

    public static FlashCardSetListFragment newInstance() {
        return new FlashCardSetListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flash_card_set_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final FloatingActionButton fab = view.findViewById(R.id.fab);

        recyclerView = view.findViewById(R.id.fragment_recycler_view);
        emptyText = view.findViewById(R.id.tv_empty);

        recyclerAdapter = new FlashCardSetRecyclerAdapter(getContext(), realm, recyclerView);
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
        }
        recyclerView.setAdapter(recyclerAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fab.hide();
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        recyclerAdapter.notifyDataSetChanged();
        recyclerAdapter.addListener();
        updateView();
    }

    @Override
    public void onPause() {
        recyclerAdapter.removeListener();
        recyclerAdapter.dismissDialog();
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        super.onDestroy();
        //MainApplication.getRefWatcher(getActivity()).watch(this);
    }

    public void newFlashCardSet() {
        final EditText input = new EditText(getContext());
        input.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getContext().getString(R.string.new_flash));
        builder.setPositiveButton(getContext().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton(getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
                    input.setError(getString(R.string.blank_input));
                } else {
                    FlashCardSet set = realm.where(FlashCardSet.class).equalTo(FlashCardSet.FlashCardSet_TITLE, title).findFirst();
                    if (set == null) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(@NonNull Realm realm) {
                                RealmList<String> cards = new RealmList<>();
                                RealmList<String> ans = new RealmList<>();
                                cards.add("");
                                ans.add("");
                                FlashCardSet flashCardSet = realm.createObject(FlashCardSet.class);
                                flashCardSet.setTitle(title);
                                flashCardSet.setCards(cards);
                                flashCardSet.setAnswers(ans);
                            }

                        });
                        updateView();
                        dialog.dismiss();
                        getContext().startActivity(FlashCardSetActivity.getStartIntent(getContext(), title, recyclerAdapter.getSetPosition(title)));
                    } else {
                        input.setError(getString(R.string.already_exists));
                    }
                }
            }
        });
        HelperUtils.showSoftKeyboard(getContext());
    }

    private void updateView() {
        if (recyclerAdapter.getItemCount() == 0) {
            emptyText.setVisibility(View.VISIBLE);
        } else if (emptyText.getVisibility() == View.VISIBLE) {
            emptyText.setVisibility(View.GONE);
        }
    }

    public void filter(String query) {
        recyclerAdapter.filter(query);
        if (TextUtils.isEmpty(query)) {
            updateView();
        }
    }

    public void scrollToTop() {
        HelperUtils.scrollToTop(getContext(), recyclerView);
    }
}