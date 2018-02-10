package com.rafhaanshah.studyassistant.flashcards;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.HelperUtils;
import com.rafhaanshah.studyassistant.R;

import io.realm.Realm;
import io.realm.RealmList;

public class FlashCardSetListFragment extends Fragment {

    private FlashCardSetRecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private Realm realm;

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recyclerView = view.findViewById(R.id.fragment_recycler_view);
        emptyText = view.findViewById(R.id.tv_empty);

        recyclerAdapter = new FlashCardSetRecyclerAdapter(getContext(), realm, recyclerView);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());

        recyclerView.addItemDecoration(dividerItemDecoration);
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

        setItemTouchHelper();
    }

    @Override
    public void onResume() {
        super.onResume();
        recyclerAdapter.animateList();
        recyclerAdapter.addListener();
        updateView();
    }

    @Override
    public void onPause() {
        super.onPause();
        recyclerAdapter.removeListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private void setItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                recyclerAdapter.deleteFlashCardSet(viewHolder.getAdapterPosition());
            }

            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;

                    Paint p = new Paint();
                    Bitmap icon;

                    if (dX > 0) {
                        icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_delete_white_24dp);
                        p.setColor(ContextCompat.getColor(getContext(), R.color.materialRed));
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), p);
                        c.drawBitmap(icon,
                                (float) itemView.getLeft() + Math.round(16 * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT)),
                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2, p);
                    } else {
                        icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_delete_white_24dp);
                        p.setColor(ContextCompat.getColor(getContext(), R.color.materialRed));
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), p);
                        c.drawBitmap(icon,
                                (float) itemView.getRight() - Math.round(16 * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT)) - icon.getWidth(),
                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2, p);
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void newFlashCardSet() {
        HelperUtils.showSoftKeyboard(getContext());

        final EditText input = new EditText(getContext());
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
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
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String title = input.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(getContext(), R.string.error_blank, Toast.LENGTH_SHORT).show();
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
                                FlashCardSet item = realm.createObject(FlashCardSet.class);
                                item.setTitle(title);
                                item.setCards(cards);
                                item.setAnswers(ans);
                            }

                        });
                        updateView();
                        dialog.dismiss();
                        getContext().startActivity(FlashCardSetActivity.getStartIntent(getContext(), title));
                    } else {
                        Toast.makeText(getContext(), R.string.error_set_exists, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
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
    }
}