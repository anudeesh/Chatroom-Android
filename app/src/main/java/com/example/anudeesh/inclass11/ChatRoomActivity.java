package com.example.anudeesh.inclass11;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {

    private ImageView logout, addImage, addMsg;
    private EditText msgText;
    final static int IMAGE_KEY = 0x03;
    private Uri galleryImage = Uri.EMPTY;
    private UploadTask uploadTask;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference mDatabase;
    private TextView userName;
    String uid;
    private ArrayList<Message> messageArrayList;
    private ArrayList<String> keysList;
    ListView myView;
    String uname="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://inclass11-49954.appspot.com");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        logout = (ImageView) findViewById(R.id.imageViewLogout);
        addImage = (ImageView) findViewById(R.id.imageViewAddPic);
        addMsg = (ImageView) findViewById(R.id.imageViewAddMsg);
        msgText = (EditText) findViewById(R.id.editTextMessage);
        userName = (TextView) findViewById(R.id.textViewName);
        myView = (ListView) findViewById(R.id.list_view);

        uid = getUid();

        mDatabase.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String f = dataSnapshot.child("First Name").getValue().toString();
                String l = dataSnapshot.child("Last Name").getValue().toString();
                userName.setText(f+" "+l);
                uname=f+" "+l;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        checkUserMessages();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intn = new Intent(ChatRoomActivity.this,MainActivity.class);
                startActivity(intn);
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imgIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imgIntent.setType("image/*");
                startActivityForResult(imgIntent,IMAGE_KEY);
            }
        });

        addMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mess = msgText.getText().toString();
                if(mess.isEmpty()) {
                    Toast.makeText(ChatRoomActivity.this,"Enter message",Toast.LENGTH_SHORT).show();
                } else {
                    createTextMessage(mess,"text");
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == IMAGE_KEY)
        {
            if(resultCode == RESULT_OK)
            {
                String uid = getUid();
                galleryImage = data.getData();
                StorageReference newRef = storageRef.child(uid+"/images/"+galleryImage.getLastPathSegment());
                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType("image/jpg")
                        .build();
                uploadTask = newRef.putFile(galleryImage,metadata);
                uploadTask.addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatRoomActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(ChatRoomActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                        createTextMessage(taskSnapshot.getDownloadUrl().toString(),"image");
                    }
                });
            }
        }
    }

    public void createTextMessage(final String text, final String type) {
        String key = mDatabase.child("msgs").push().getKey();
        Date date = new Date();
        SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");
        String newDate = sd.format(date);
        String name[] = userName.getText().toString().split(" ");
        Message msg = new Message(text,type,name[0],name[1],newDate,uid);
        Map<String, Object> msgValues = msg.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put("/expenses/" + key, expValues);
        childUpdates.put("messages/" + key, msgValues);

        mDatabase.updateChildren(childUpdates);
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void checkUserMessages() {
        mDatabase.child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messageArrayList = new ArrayList<Message>();
                keysList = new ArrayList<String>();
                for (DataSnapshot value : dataSnapshot.getChildren()) {
                    Message msg = value.getValue(Message.class);
                    String msgid = value.getKey();
                    Log.d("inside",msg.toString());
                    keysList.add(msgid);
                    messageArrayList.add(msg);
                }
                //if(messageArrayList.size()>0) {
                    showUserMessages();
                //}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void showUserMessages() {
        if(messageArrayList.size()>0) {
            MessageAdapter adapter = new MessageAdapter(ChatRoomActivity.this,R.layout.row_item_layout,messageArrayList,keysList,uname);
            Log.d("demo",messageArrayList.get(0).toString());
            myView.setAdapter(adapter);
            msgText.setText("");
        }
    }
}
