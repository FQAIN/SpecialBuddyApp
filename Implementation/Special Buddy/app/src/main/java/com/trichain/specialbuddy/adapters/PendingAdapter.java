package com.trichain.specialbuddy.adapters;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.trichain.specialbuddy.R;
import com.trichain.specialbuddy.chat.data.StaticConfig;
import com.trichain.specialbuddy.chat.data.ListFriend;
import com.trichain.specialbuddy.dashBoard.BuddiesRequestsSugFragment;
import com.trichain.specialbuddy.databinding.ItemAddBuddyBinding;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.util.HashMap;
import java.util.Map;

public class PendingAdapter extends RecyclerView.Adapter<PendingAdapter.PendingVH> {


    private ListFriend listFriend;
    private Context context;
    public static Map<String, Query> mapQuery;
    public static Map<String, DatabaseReference> mapQueryOnline;
    public static Map<String, ChildEventListener> mapChildListener;
    public static Map<String, ChildEventListener> mapChildListenerOnline;
    public static Map<String, Boolean> mapMark;
    private BuddiesRequestsSugFragment fragment;
    LovelyProgressDialog dialogWaitDeleting;

    public PendingAdapter(Context context, ListFriend listFriend, BuddiesRequestsSugFragment fragment) {
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

    public void onSearch(ListFriend l){
        listFriend.setListFriend(l.getListFriend());
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public PendingVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PendingVH(ItemAddBuddyBinding.inflate(LayoutInflater.from(context),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull PendingVH h, @SuppressLint("RecyclerView") int position) {
        final String name = listFriend.getListFriend().get(position).name;
        final String id = listFriend.getListFriend().get(position).id;
        final String email = listFriend.getListFriend().get(position).email;
        final String avata = listFriend.getListFriend().get(position).avata;
        h.b.txtName.setText(name);
        h.b.txtEmail.setText(email);


        if (listFriend.getListFriend().get(position).avata.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            h.b.iconAvata.setImageResource(R.drawable.default_avata);
        } else {
            byte[] decodedString = Base64.decode(avata, Base64.DEFAULT);
            Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            h.b.iconAvata.setImageBitmap(src);
        }

        h.b.accept.setOnClickListener(v->{
            acceptFriend(id,true,h.b);
        });
        h.b.reject.setOnClickListener(v->{
            acceptFriend(id,false,h.b);
        });


    }

    @Override
    public int getItemCount() {
        return listFriend.getListFriend() != null ? listFriend.getListFriend().size() : 0;

    }

    private static final String TAG = "PendingAdapter";
    private void acceptFriend(final String idFriend,boolean accept, ItemAddBuddyBinding b) {
        if (idFriend != null) {

            if (accept){
                FirebaseDatabase.getInstance().getReference().child("friend")
                        .child(idFriend).child(StaticConfig.UID).setValue(StaticConfig.UID).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e(TAG, "onComplete: His" );
                    }
                });
                FirebaseDatabase.getInstance().getReference().child("friend")
                        .child(StaticConfig.UID).child(idFriend).setValue(idFriend).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        b.reject.setVisibility(View.GONE);
                        Log.e(TAG, "onComplete: Mine" );
                    }
                });
                FirebaseDatabase.getInstance().getReference().child("requests")
                        .child(StaticConfig.UID).child(idFriend).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Log.e(TAG, "onComplete: remove request" );
                    }
                });

            }else{

                FirebaseDatabase.getInstance().getReference().child("requests")
                        .child(StaticConfig.UID).child(idFriend).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        b.accept.setVisibility(View.GONE);
                        Log.e(TAG, "onComplete: remove request" );
                    }
                });
            }
        } else {
            dialogWaitDeleting.dismiss();
            new LovelyInfoDialog(context)
                    .setTopColorRes(R.color.colorPrimary)
                    .setTitle("Error")
                    .setMessage("Error occurred")
                    .show();
        }
    }

    class PendingVH extends RecyclerView.ViewHolder{

        ItemAddBuddyBinding b;
        public PendingVH(@NonNull ItemAddBuddyBinding itemView) {
            super(itemView.getRoot());
            b=itemView;
        }
    }
    
    
}
