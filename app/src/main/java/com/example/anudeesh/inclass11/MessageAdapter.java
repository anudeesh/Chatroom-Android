package com.example.anudeesh.inclass11;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anudeesh on 11/14/2016.
 */
public class MessageAdapter extends ArrayAdapter<Message> {
    List<Message> mData;
    Context mContext;
    int mResource;
    ArrayList<Message> uMsgs;
    ArrayList<Message> comList;
    ArrayList<String> mkeys;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private String uid;
    private DatabaseReference mDatabase;
    String uname;

    public MessageAdapter(Context context, int resource, List<Message> objects, ArrayList<String> mkeys, String uname) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.mData = objects;
        this.mkeys = mkeys;
        this.uname = uname;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,parent,false);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uid = user.getUid();

        //uMsgs = new ArrayList<Message>();
        uMsgs = (ArrayList<Message>) mData;
        Message m = mData.get(position);
        final String mid = mkeys.get(position);
        TextView name = (TextView) convertView.findViewById(R.id.textViewNameMsg);
        TextView time = (TextView) convertView.findViewById(R.id.textViewTime);
        TextView msg = (TextView) convertView.findViewById(R.id.textViewMsgText);
        ImageView iv = (ImageView) convertView.findViewById(R.id.imageViewMsgPic);

        ImageView delete = (ImageView) convertView.findViewById(R.id.imageViewDeletePost);
        ImageView comment = (ImageView) convertView.findViewById(R.id.imageViewComment);

        final ListView mview = (ListView) convertView.findViewById(R.id.listViewComments);

        /*final Button comcancel = (Button) convertView.findViewById(R.id.buttonCommentCancel);
        final Button comok = (Button) convertView.findViewById(R.id.buttonCommentSend);*/

        /*comtext.setVisibility(View.GONE);
        comcancel.setVisibility(View.GONE);
        comok.setVisibility(View.GONE);*/

        mDatabase.child("messages").child(mid).child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                comList = new ArrayList<Message>();
                //keysList = new ArrayList<String>();
                for (DataSnapshot value : dataSnapshot.getChildren()) {
                    Message msg = value.getValue(Message.class);
                    comList.add(msg);
                }
                if(comList.size()>0) {
                    CommentAdapter adapter = new CommentAdapter(mContext,R.layout.row_comment_layout,comList,uname);
                    //Log.d("demo",messageArrayList.get(0).toString());
                    mview.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(m.getType().equals("image")) {
            //new DownloadImageTask(iv).execute("http://ec2-54-166-14-133.compute-1.amazonaws.com/api/file/" + m.getThumbnail());
            Picasso.with(mContext).load(m.getText()).into(iv);
            msg.setVisibility(View.GONE);
        } else {
            msg.setText(m.getText());
            iv.setVisibility(View.GONE);
        }
        name.setText(m.getFname() + " " + m.getLname());
        long now = System.currentTimeMillis();
        //long longTimeAgo  = timeStringtoMilis("2004-05-12 09:33:12");
        PrettyTime prettyTime = new PrettyTime();
        String d = prettyTime.format(new Date(m.getTime()));
        time.setText(d);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child("messages").child(mkeys.get(position)).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(mContext,"Text deleted", Toast.LENGTH_SHORT).show();
                            mData.remove(position);
                            mkeys.remove(position);
                        }else{
                            Toast.makeText(mContext,"Error deleting text", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater li = LayoutInflater.from(mContext);
                View promptsView = li.inflate(R.layout.prompt, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        mContext);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);
                final EditText comtext = (EditText) promptsView.findViewById(R.id.editTextCommentVal);
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        String cont = comtext.getText().toString();
                                        if(cont.isEmpty()) {
                                            Toast.makeText(mContext, "Enter comment", Toast.LENGTH_SHORT).show();
                                        } else {
                                            String key = mDatabase.child("comm").push().getKey();
                                            Date date = new Date();
                                            SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");
                                            String newDate = sd.format(date);
                                            String name[] = uname.split(" ");
                                            Message msg = new Message(cont,"text",name[0],name[1],newDate,uid);
                                            Map<String, Object> msgValues = msg.toMap();

                                            Map<String, Object> childUpdates = new HashMap<>();
                                            //childUpdates.put("/expenses/" + key, expValues);
                                            childUpdates.put("messages/"+mid+"/comments/" + key, msgValues);

                                            mDatabase.updateChildren(childUpdates);
                                            comtext.setText("");
                                            mDatabase.child("messages").child(mid).child("comments").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    comList = new ArrayList<Message>();
                                                    //keysList = new ArrayList<String>();
                                                    for (DataSnapshot value : dataSnapshot.getChildren()) {
                                                        Message msg = value.getValue(Message.class);
                                                        comList.add(msg);
                                                    }
                                                    if(comList.size()>0) {
                                                        CommentAdapter adapter = new CommentAdapter(mContext,R.layout.row_comment_layout,comList,uname);
                                                        //Log.d("demo",messageArrayList.get(0).toString());
                                                        mview.setAdapter(adapter);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

        /*comcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comtext.setVisibility(View.GONE);
                comcancel.setVisibility(View.GONE);
                comok.setVisibility(View.GONE);
            }
        });

        comok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cont = comtext.getText().toString();
                if(cont.isEmpty()) {
                    Toast.makeText(mContext, "Enter comment", Toast.LENGTH_SHORT).show();
                } else {
                    String key = mDatabase.child("comm").push().getKey();
                    Date date = new Date();
                    SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");
                    String newDate = sd.format(date);
                    String name[] = uname.split(" ");
                    Message msg = new Message(cont,"text",name[0],name[1],newDate,uid);
                    Map<String, Object> msgValues = msg.toMap();

                    Map<String, Object> childUpdates = new HashMap<>();
                    //childUpdates.put("/expenses/" + key, expValues);
                    childUpdates.put("messages/"+mid+"/comments/" + key, msgValues);

                    mDatabase.updateChildren(childUpdates);
                    comtext.setText("");

                }
            }
        });*/

        return convertView;
    }
}
