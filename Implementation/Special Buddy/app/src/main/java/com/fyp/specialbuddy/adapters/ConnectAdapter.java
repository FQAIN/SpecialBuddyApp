package com.fyp.specialbuddy.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.fyp.specialbuddy.R;
import com.fyp.specialbuddy.chat.data.StaticConfig;
import com.fyp.specialbuddy.chat.data.ListFriend;
import com.fyp.specialbuddy.dashBoard.BuddiesRequestsSugFragment;
import com.fyp.specialbuddy.databinding.ItemConnectBuddyBinding;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.util.HashMap;
import java.util.Map;

public class ConnectAdapter extends RecyclerView.Adapter<ConnectAdapter.ConnectVH> {


    private ListFriend listFriend;
    private Context context;
    public static Map<String, Query> mapQuery;
    public static Map<String, DatabaseReference> mapQueryOnline;
    public static Map<String, ChildEventListener> mapChildListener;
    public static Map<String, ChildEventListener> mapChildListenerOnline;
    public static Map<String, Boolean> mapMark;
    private BuddiesRequestsSugFragment fragment;
    LovelyProgressDialog dialogWaitDeleting;

    public ConnectAdapter(Context context, ListFriend listFriend, BuddiesRequestsSugFragment fragment) {
        this.listFriend = listFriend;
        this.context = context;
        mapQuery = new HashMap<>();
        mapChildListener = new HashMap<>();
        mapMark = new HashMap<>();
        mapChildListenerOnline = new HashMap<>();
        mapQueryOnline = new HashMap<>();
        this.fragment = fragment;
        dialogWaitDeleting = new LovelyProgressDialog(context);
    }

    public void onSearch(ListFriend l) {
        listFriend.setListFriend(l.getListFriend());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConnectVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConnectVH(ItemConnectBuddyBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectVH h, @SuppressLint("RecyclerView") int position) {
        final String name = listFriend.getListFriend().get(position).name;
        final String id = listFriend.getListFriend().get(position).id;
        final String email = listFriend.getListFriend().get(position).email;
        final String avata = listFriend.getListFriend().get(position).avata;
        h.b.txtName.setText(name);
        h.b.txtEmail.setText(email);

        h.b.connectBtn.setOnClickListener(v -> {
            h.b.connectBtn.setClickable(false);
            h.b.connectBtn.setVisibility(View.GONE);

            final String idFriendRemoval = listFriend.getListFriend().get(position).id;

            connectFriend(idFriendRemoval, h.b);
        });
        if (listFriend.getListFriend().get(position).avata.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            h.b.iconAvata.setImageResource(R.drawable.default_avata);
        } else {
            byte[] decodedString = Base64.decode(listFriend.getListFriend().get(position).avata, Base64.DEFAULT);
            Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            h.b.iconAvata.setImageBitmap(src);
        }


        if (mapQueryOnline.get(id) == null && mapChildListenerOnline.get(id) == null) {
            mapQueryOnline.put(id, FirebaseDatabase.getInstance().getReference().child("user/" + id + "/status"));
            mapChildListenerOnline.put(id, new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getValue() != null && dataSnapshot.getKey().equals("isOnline")) {
                        Log.d("FriendsFragment add " + id, (boolean) dataSnapshot.getValue() + "");
                        notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getValue() != null && dataSnapshot.getKey().equals("isOnline")) {
                        Log.d("FriendsFragment change " + id, (boolean) dataSnapshot.getValue() + "");
                        notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            mapQueryOnline.get(id).addChildEventListener(mapChildListenerOnline.get(id));
        }


    }

    @Override
    public int getItemCount() {
        return listFriend.getListFriend() != null ? listFriend.getListFriend().size() : 0;

    }



    private void connectFriend(final String idFriend, ItemConnectBuddyBinding b) {
        if (idFriend != null) {

            FirebaseDatabase.getInstance().getReference().child("requests")
                    .child(idFriend).child(StaticConfig.UID).setValue(StaticConfig.UID).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    b.connectBtn.setText("Sent");
                }
            });
        } else {
            dialogWaitDeleting.dismiss();
            new LovelyInfoDialog(context)
                    .setTopColorRes(R.color.colorPrimary)
                    .setTitle("Error")
                    .setMessage("Error occurred")
                    .show();
        }
    }

    class ConnectVH extends RecyclerView.ViewHolder {

        ItemConnectBuddyBinding b;

        public ConnectVH(@NonNull ItemConnectBuddyBinding itemView) {
            super(itemView.getRoot());
            b = itemView;
        }
    }


}
