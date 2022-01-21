package com.fyp.specialbuddy.dashBoard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.fyp.specialbuddy.R;
import com.fyp.specialbuddy.adapters.ContactsAdapter;
import com.fyp.specialbuddy.chat.data.StaticConfig;
import com.fyp.specialbuddy.chat.data.Friend;
import com.fyp.specialbuddy.chat.data.ListFriend;
import com.fyp.specialbuddy.chat.service.ServiceUtils;
import com.fyp.specialbuddy.databinding.FragmentContactsBinding;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class ContactsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView recyclerListFrends;
    private ContactsAdapter adapter;
    private ListFriend dataListFriend = new ListFriend();
    private ArrayList<String> listFriendID = null;
    private LovelyProgressDialog dialogFindAllFriend;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CountDownTimer detectFriendOnline;
    public static int ACTION_START_CHAT = 1;

    public static final String ACTION_DELETE_FRIEND = "com.fyp.specialbuddy.DELETE_FRIEND";

    private BroadcastReceiver deleteFriendReceiver;

//    private ContactsViewModel galleryViewModel;
    private FragmentContactsBinding b;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.search) {
            if (b.searchEd.getVisibility() == View.GONE) {
                b.searchEd.setVisibility(View.VISIBLE);
                b.searchEd.requestFocus();
                b.searchEd.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        b.searchEd.setVisibility(View.GONE);
                    }
                });
                b.searchEd.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        filterList(s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            } else {

                b.searchEd.setVisibility(View.GONE);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private void filterList(CharSequence s) {
        ListFriend listFriend= new ListFriend();
        if (s.length()==0){
            listFriend.setListFriend(dataListFriend.getListFriend());
        }else {
            listFriend.setListFriend(dataListFriend.filterFriends(s));
        }
        adapter.onSearch(listFriend);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        b = FragmentContactsBinding.inflate(inflater, container, false);

        detectFriendOnline = new CountDownTimer(System.currentTimeMillis(), StaticConfig.TIME_TO_REFRESH) {
            @Override
            public void onTick(long l) {
                ServiceUtils.updateFriendStatus(getContext(), dataListFriend);
                ServiceUtils.updateUserStatus(getContext());
            }

            @Override
            public void onFinish() {

            }
        };
        if (dataListFriend == null) {
            if (dataListFriend.getListFriend().size() > 0) {
                listFriendID = new ArrayList<>();
                for (Friend friend : dataListFriend.getListFriend()) {
                    listFriendID.add(friend.id);
                }
                detectFriendOnline.start();
            }
        }
        View layout = b.getRoot();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerListFrends = (RecyclerView) layout.findViewById(R.id.recycleListFriend);
        recyclerListFrends.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        adapter = new ContactsAdapter(getContext(), dataListFriend, this);
        recyclerListFrends.setAdapter(adapter);
        dialogFindAllFriend = new LovelyProgressDialog(getContext());
        listFriendID = new ArrayList<>();
        dialogFindAllFriend.setCancelable(false)
                .setIcon(R.drawable.ic_add_friend)
                .setTitle("Get all buddies....")
                .setTopColorRes(R.color.colorPrimary)
                .show();
        getListFriendUId();

        deleteFriendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String idDeleted = intent.getExtras().getString("idFriend");
                for (Friend friend : dataListFriend.getListFriend()) {
                    if (idDeleted.equals(friend.id)) {
                        ArrayList<Friend> friends = dataListFriend.getListFriend();
                        friends.remove(friend);
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        };

        IntentFilter intentFilter = new IntentFilter(ACTION_DELETE_FRIEND);
        getContext().registerReceiver(deleteFriendReceiver, intentFilter);
        return layout;
    }

    @Override
    public void onRefresh() {
        listFriendID.clear();
        dataListFriend.getListFriend().clear();
        adapter.notifyDataSetChanged();
        detectFriendOnline.cancel();
        getListFriendUId();
    }

    private void getListFriendUId() {
        FirebaseDatabase.getInstance().getReference().child("friend/" + StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    listFriendID.clear();
                    HashMap mapRecord = (HashMap) dataSnapshot.getValue();
                    Iterator listKey = mapRecord.keySet().iterator();
                    while (listKey.hasNext()) {
                        String key = listKey.next().toString();
                        if (!listFriendID.contains(mapRecord.get(key).toString()))
                        listFriendID.add(mapRecord.get(key).toString());
                    }
                    getAllFriendInfo(0);
                } else {
                    dialogFindAllFriend.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private static final String TAG = "ContactsFragment";
    private void getAllFriendInfo(final int index) {
        Log.e(TAG, "getAllFriendInfo: "+index );
        if (index == listFriendID.size()) {
            //save list friend
            adapter.notifyDataSetChanged();
            dialogFindAllFriend.dismiss();
            mSwipeRefreshLayout.setRefreshing(false);
            detectFriendOnline.start();
        } else {
            final String id = listFriendID.get(index);
            FirebaseDatabase.getInstance().getReference().child("user/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Friend user = new Friend();
                        HashMap mapUserInfo = (HashMap) dataSnapshot.getValue();
                        user.name = (String) mapUserInfo.get("name");
                        user.email = (String) mapUserInfo.get("email");
                        user.avata = (String) mapUserInfo.get("avata");
                        user.phone = (String) mapUserInfo.get("phone");
                        user.id = id;
                        user.idRoom = id.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + id).hashCode() + "" : "" + (id + StaticConfig.UID).hashCode();
                        dataListFriend.getListFriend().add(user);
                    }
                    getAllFriendInfo(index + 1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}