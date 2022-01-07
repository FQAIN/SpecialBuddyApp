package com.trichain.specialbuddy.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
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
import com.trichain.specialbuddy.R;
import com.trichain.specialbuddy.chat.data.StaticConfig;
import com.trichain.specialbuddy.chat.model.ListFriend;
import com.trichain.specialbuddy.dashBoard.BuddiesRequestsSugFragment;
import com.trichain.specialbuddy.databinding.ItemConnectBuddyBinding;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
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

//        ((View) h.b.txtName.getParent().getParent().getParent())
//                .setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
////                        h.b.txtMessage.setTypeface(Typeface.DEFAULT);
//                        h.b.txtName.setTypeface(Typeface.DEFAULT);
//                        Intent intent = new Intent(context, ChatActivity.class);
//                        intent.putExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND, name);
//                        ArrayList<CharSequence> idFriend = new ArrayList<CharSequence>();
//                        idFriend.add(id);
//                        intent.putCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID, idFriend);
//                        intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, idRoom);
//                        ChatActivity.bitmapAvataFriend = new HashMap<>();
//                        if (!avata.equals(StaticConfig.STR_DEFAULT_BASE64)) {
//                            byte[] decodedString = Base64.decode(avata, Base64.DEFAULT);
//                            ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
//                        } else {
//                            ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avata));
//                        }
//
//                        mapMark.put(id, null);
//                        fragment.startActivityForResult(intent, FriendsFragment.ACTION_START_CHAT);
//                    }
//                });
        h.b.connectBtn.setOnClickListener(v -> {
            h.b.connectBtn.setClickable(false);
            h.b.connectBtn.setVisibility(View.GONE);

            final String idFriendRemoval = listFriend.getListFriend().get(position).id;

            connectFriend(idFriendRemoval, h.b);
        });
//        ((View) h.b.txtName.getParent().getParent().getParent())
//                .setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View view) {
//                        String friendName = (String)h.b.txtName.getText();
//
//                        new AlertDialog.Builder(context)
//                                .setTitle("Delete Friend")
//                                .setMessage("Are you sure want to delete "+friendName+ "?")
//                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                        dialogInterface.dismiss();dialogWaitDeleting.setTitle("Deleting...")
//                                                .setCancelable(false)
//                                                .setTopColorRes(R.color.colorAccent)
//                                                .show();
//                                    }
//                                })
//                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                        dialogInterface.dismiss();
//                                    }
//                                }).show();
//
//                        return true;
//                    }
//                });


//        if (listFriend.getListFriend().get(position).message.text.length() > 0) {
//            //h.b.txtMessage.setVisibility(View.VISIBLE);
//            //h.b.txtTime.setVisibility(View.VISIBLE);
//            if (!listFriend.getListFriend().get(position).message.text.startsWith(id)) {
//                // h.txtMessage.setText(listFriend.getListFriend().get(position).message.text);
//                // h.txtMessage.setTypeface(Typeface.DEFAULT);
//                h.b.txtName.setTypeface(Typeface.DEFAULT);
//            } else {
//                // h.txtMessage.setText(listFriend.getListFriend().get(position).message.text.substring((id + "").length()));
//                // h.txtMessage.setTypeface(Typeface.DEFAULT_BOLD);
//                h.b.txtName.setTypeface(Typeface.DEFAULT_BOLD);
//            }
//            String time = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(listFriend.getListFriend().get(position).message.timestamp));
//            String today = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(System.currentTimeMillis()));
//            if (today.equals(time)) {
//                //h.b.txtTime.setText(new SimpleDateFormat("HH:mm").format(new Date(listFriend.getListFriend().get(position).message.timestamp)));
//            } else {
//                //h.b.txtTime.setText(new SimpleDateFormat("MMM d").format(new Date(listFriend.getListFriend().get(position).message.timestamp)));
//            }
//        } else {
//            // h.txtMessage.setVisibility(View.GONE);
//            //h.b.txtTime.setVisibility(View.GONE);
//            if (mapQuery.get(id) == null && mapChildListener.get(id) == null) {
//                mapQuery.put(id, FirebaseDatabase.getInstance().getReference().child("message/" + idRoom).limitToLast(1));
//                mapChildListener.put(id, new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                        HashMap mapMessage = (HashMap) dataSnapshot.getValue();
//                        if (mapMark.get(id) != null) {
//                            if (!mapMark.get(id)) {
//                                listFriend.getListFriend().get(position).message.text = id + mapMessage.get("text");
//                            } else {
//                                listFriend.getListFriend().get(position).message.text = (String) mapMessage.get("text");
//                            }
//                            notifyDataSetChanged();
//                            mapMark.put(id, false);
//                        } else {
//                            listFriend.getListFriend().get(position).message.text = (String) mapMessage.get("text");
//                            notifyDataSetChanged();
//                        }
//                        listFriend.getListFriend().get(position).message.timestamp = (long) mapMessage.get("timestamp");
//                    }
//
//                    @Override
//                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                    }
//
//                    @Override
//                    public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                    }
//
//                    @Override
//                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//                mapQuery.get(id).addChildEventListener(mapChildListener.get(id));
//                mapMark.put(id, true);
//            } else {
//                mapQuery.get(id).removeEventListener(mapChildListener.get(id));
//                mapQuery.get(id).addChildEventListener(mapChildListener.get(id));
//                mapMark.put(id, true);
//            }
//        }
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
                        listFriend.getListFriend().get(position).status.isOnline = (boolean) dataSnapshot.getValue();
                        notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getValue() != null && dataSnapshot.getKey().equals("isOnline")) {
                        Log.d("FriendsFragment change " + id, (boolean) dataSnapshot.getValue() + "");
                        listFriend.getListFriend().get(position).status.isOnline = (boolean) dataSnapshot.getValue();
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

        if (listFriend.getListFriend().get(position).status.isOnline) {
            h.b.iconAvata.setBorderWidth(10);
        } else {
            h.b.iconAvata.setBorderWidth(0);
        }
    }

    @Override
    public int getItemCount() {
        return listFriend.getListFriend() != null ? listFriend.getListFriend().size() : 0;

    }


    /**
     * COnnect friend
     *
     * @param idFriend
     * @param b
     */
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
