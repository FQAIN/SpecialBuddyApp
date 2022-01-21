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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.fyp.specialbuddy.R;
import com.fyp.specialbuddy.adapters.ConnectAdapter;
import com.fyp.specialbuddy.adapters.PendingAdapter;
import com.fyp.specialbuddy.chat.data.StaticConfig;
import com.fyp.specialbuddy.chat.data.Friend;
import com.fyp.specialbuddy.chat.data.ListFriend;
import com.fyp.specialbuddy.chat.service.ServiceUtils;
import com.fyp.specialbuddy.databinding.FragmentBuddiesRequestsSugBinding;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class BuddiesRequestsSugFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private PendingAdapter adapter;
    private ConnectAdapter adapter2;
    private ListFriend dataListFriend = new ListFriend();
    private ListFriend dataNonListFriend = new ListFriend();
    private ListFriend dataReqFriend = new ListFriend();
    private ArrayList<String> listFriendID = null;
    private ArrayList<String> listNonFriendID = new ArrayList<>();
    private ArrayList<String> listRequestsID = new ArrayList<>();
    private LovelyProgressDialog dialogFindAllFriend;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CountDownTimer detectFriendOnline;
    public static int ACTION_START_CHAT = 1;
    private static final String TAG = "BuddiesReqFrag";

    public static final String ACTION_DELETE_FRIEND = "com.fyp.specialbuddy.DELETE_FRIEND";

    private BroadcastReceiver deleteFriendReceiver;

    private FragmentBuddiesRequestsSugBinding b;

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
        ListFriend listFriend = new ListFriend();
        if (s.length() == 0) {
            listFriend.setListFriend(dataListFriend.getListFriend());
        } else {
            listFriend.setListFriend(dataListFriend.filterFriends(s));
        }
        adapter.onSearch(listFriend);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        b = FragmentBuddiesRequestsSugBinding.inflate(inflater, container, false);

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
//                    listFriendID.add(friend.id);
                }
                detectFriendOnline.start();
            }
        }
        View layout = b.getRoot();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        b.recycleListFriend.setLayoutManager(linearLayoutManager);
        b.recycleListPending.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        adapter = new PendingAdapter(getContext(), dataReqFriend, this);
        adapter2 = new ConnectAdapter(getContext(), dataNonListFriend, this);
        b.recycleListPending.setAdapter(adapter);
        b.recycleListFriend.setAdapter(adapter2);

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
        Log.e(TAG, "getListFriendUId: ");
        FirebaseDatabase.getInstance().getReference().child("friend/" + StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "getListFriendUId-onDataChange: ");
                if (dataSnapshot.getValue() != null) {
                    HashMap mapRecord = (HashMap) dataSnapshot.getValue();
                    Iterator listKey = mapRecord.keySet().iterator();
                    while (listKey.hasNext()) {
                        String key = listKey.next().toString();
                        listFriendID.add(mapRecord.get(key).toString());
                    }
                    getAllFriendInfo(0);
                } else {
                    dialogFindAllFriend.dismiss();
                }
                getNonFriend();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

    private void getNonFriend() {
        Log.e(TAG, "getNonFriend: ");
        FirebaseDatabase.getInstance().getReference().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "getNonFriend-onDataChange: " + dataSnapshot.getValue());

                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot d : dataSnapshot.getChildren()
                    ) {
                        if (!listFriendID.contains(d.getKey()) && !d.getKey().contentEquals(StaticConfig.UID)) {
                            listNonFriendID.add(d.getKey());
                        }

                    }

                    getNonFriendInfo(0);
                } else {
                    Log.e(TAG, "onDataChange: isNull");
                    dialogFindAllFriend.dismiss();
                }
                getFriendRequests();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getFriendRequests() {
        Log.e(TAG, "getFriendRequests: ");
        FirebaseDatabase.getInstance().getReference().child("requests").child(StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "getFriendRequests onDataChange: " + dataSnapshot);
                if (dataSnapshot.getValue() != null) {
                    listRequestsID.clear();
                    for (DataSnapshot d : dataSnapshot.getChildren()
                    ) {
                        if (!listRequestsID.contains(d.getKey())) {
                            listRequestsID.add(d.getKey());
                            Log.e(TAG, "getFriendRequests onDataChange: " + d.getKey());
                        }

                    }

                    getReqFriendInfo(0);
                } else {
                    dialogFindAllFriend.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void getAllFriendInfo(final int index) {
        Log.e(TAG, "getAllFriendInfo: " + index);
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
                        user.id = id;
                        user.idRoom = id.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + id).hashCode() + "" : "" + (id + StaticConfig.UID).hashCode();
                        dataListFriend.getListFriend().add(user);
//                        FriendDB.getInstance(getContext()).addFriend(user);
                    }
                    getAllFriendInfo(index + 1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void getNonFriendInfo(final int index) {
        Log.e(TAG, "getNonFriendInfo: " + index);
        if (index == listNonFriendID.size()) {
            //save list friend
            adapter2.notifyDataSetChanged();
            dialogFindAllFriend.dismiss();
            mSwipeRefreshLayout.setRefreshing(false);
            detectFriendOnline.start();
        } else {
            final String id = listNonFriendID.get(index);
            FirebaseDatabase.getInstance().getReference().child("user/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Friend user = new Friend();
                        HashMap mapUserInfo = (HashMap) dataSnapshot.getValue();
                        user.name = (String) mapUserInfo.get("name");
                        user.email = (String) mapUserInfo.get("email");
                        user.avata = (String) mapUserInfo.get("avata");
                        user.id = id;
                        user.idRoom = id.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + id).hashCode() + "" : "" + (id + StaticConfig.UID).hashCode();
                        dataNonListFriend.getListFriend().add(user);
                    }
                    getNonFriendInfo(index + 1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void getReqFriendInfo(final int index) {
        Log.e(TAG, "getReqFriendInfo: " + index);
        if (index == listRequestsID.size()) {
            Log.e(TAG, "getReqFriendInfo-all: " + dataReqFriend.getListFriend().size());
            //save list friend
            adapter.notifyDataSetChanged();
            dialogFindAllFriend.dismiss();
            mSwipeRefreshLayout.setRefreshing(false);
            detectFriendOnline.start();
        } else {
            final String id = listRequestsID.get(index);
            FirebaseDatabase.getInstance().getReference().child("user/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Friend user = new Friend();
                        HashMap mapUserInfo = (HashMap) dataSnapshot.getValue();
                        user.name = (String) mapUserInfo.get("name");
                        user.email = (String) mapUserInfo.get("email");
                        user.avata = (String) mapUserInfo.get("avata");
                        user.id = id;
                        user.idRoom = id.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + id).hashCode() + "" : "" + (id + StaticConfig.UID).hashCode();
                        dataReqFriend.getListFriend().add(user);
//                        FriendDB.getInstance(getContext()).addFriend(user);
                    }
                    getReqFriendInfo(index + 1);
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