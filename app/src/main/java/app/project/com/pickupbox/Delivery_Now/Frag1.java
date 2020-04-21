package app.project.com.pickupbox.Delivery_Now;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import app.project.com.pickupbox.R;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

public class Frag1 extends Fragment {

    private Button btnChoose;
    private Button btnUpload;
    private Button btnTake;//사진촬영은 tedpermission 해야함 일단 제외---오류가 너무많음
    private Button btnStart;

    private ImageView imageView;

    private Uri filePath;
    private String nick, userName;
    private int LOG_INOUT = 0;

    private static final int REQUEST_IMAGE_CAPTURE = 400;
    private String imageFilePath;


    public static Frag1 newInstance() {
        Frag1 fragment = new Frag1();
        Bundle args = new Bundle();


        fragment.setArguments(args); //이런식으로 bundle 값 생성해도돼

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /*ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.frag1, container, false);*/
        View v =  inflater.inflate(R.layout.frag1, container, false);
        initValues();
        if (LOG_INOUT == 1){
            if (CheckFile() == true){
                FirebaseStorage storage = FirebaseStorage.getInstance("gs://boxstorage-8f972.appspot.com");
                StorageReference storageRef = storage.getReference();
                StorageReference imageRef = storageRef.child("images/"+userName+"/"+userName+"_start.png");
                final long ONE_MEGABYTE = 1024 * 1024;
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(Frag1.this).load(uri).into(imageView);
                       /* Button bt1 = (Button) getView().findViewById(R.id.btn_take);
                        Button bt2 = (Button) getView().findViewById(R.id.btn_choice);
                        Button bt3 = (Button) getView().findViewById(R.id.btn_upload);
                        bt1.setVisibility(View.GONE);
                        bt2.setVisibility(View.GONE);
                        bt3.setVisibility(View.GONE);*/
                        LinearLayout layout=(LinearLayout)getView().findViewById(R.id.layout1);
                        layout.setVisibility(View.GONE);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getContext(), "이미지 다운로드 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                ImageView iv1 = (ImageView)v.findViewById(R.id.imageView);
                iv1.setVisibility(View.GONE);
                LinearLayout layout1=(LinearLayout)v.findViewById(R.id.layout1);
                layout1.setVisibility(View.GONE);
                LinearLayout layout2=(LinearLayout)v.findViewById(R.id.layout2);
                layout2.setVisibility(View.VISIBLE);

            }
        }else{ //로그인 되어있지 않다면


        }


        btnChoose= v.findViewById(R.id.btn_choice);
        btnUpload = v.findViewById(R.id.btn_upload);
        btnTake = v.findViewById(R.id.btn_take);
        btnStart=v.findViewById(R.id.btn_start);


        imageView = v.findViewById(R.id.imageView);


        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //이미지를 선택
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), 0);
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //업로드
                uploadFile();
            }
        });


        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //사진촬영
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {

                    }
                    if (photoFile != null) {
                        filePath = FileProvider.getUriForFile(getContext(), getActivity().getPackageName(), photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout layout2=(LinearLayout)getView().findViewById(R.id.layout2);
                layout2.setVisibility(View.GONE);
                ImageView iv1 = (ImageView)getView().findViewById(R.id.imageView);
                iv1.setVisibility(View.VISIBLE);
                LinearLayout layout1=(LinearLayout)getView().findViewById(R.id.layout1);
                layout1.setVisibility(View.VISIBLE);
            }
        });

        return v;
    }


    private void initValues() {//로그인 여부 확인/*
        SharedPreferences pref = getActivity().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit(); //SharedPreferences에 editor 생성.
        nick = pref.getString("email","def"); //이름이 email인 key값이 default값이라면 def, 아니라면 nick에 email값이 저장됨.

        if(nick != "def") { //로그인이 되어서 def가 아닌 다른 값이 들어온다면.
            userName = nick;
            LOG_INOUT = 1 ; //로그인 상태 변경 , 기본 0 // 로그인 되있을 시 1

        }else if(nick == "def"){ //로그인 안되서 default 값인 email이 나온다면
            Toast.makeText(getActivity(), "로그인을 먼저 해주세요.", Toast.LENGTH_SHORT).show();

        }
    }

    //사진촬영권한
    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(getActivity().getApplicationContext(), "권한이 허용됨",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(getActivity().getApplicationContext(), "권한이 거부됨",Toast.LENGTH_SHORT).show();
        }
    };

    boolean chck;
    private boolean CheckFile(){
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://boxstorage-8f972.appspot.com");
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("images");
        imageRef.child(userName).child(userName+"_start.png")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                chck = true;
                Log.d("테스트",Boolean.toString(chck));
                Toast.makeText(getContext(), "이미지 존재" ,Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                chck = false;
                Log.d("테스트",Boolean.toString(chck));
                Toast.makeText(getContext(), "이미지 존재 안함" ,Toast.LENGTH_SHORT).show();
            }
        });
        //Log.d("테스트",Boolean.toString(a));
        /*imageRef.child(userName+"/"+userName+"_start.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                chck = true;

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                chck = false;


            }
        });*/
        /*images/admin/admin_start.png*/

        //Log.d("테스트",Boolean.toString(chck));

        //Log.d("테스트",ima);
        //boolean a = imageRef.getName().contains(userName);  //사용자의 아이디가 포함되어있다면?

        //Toast.makeText(getContext(), Boolean.toString(a), Toast.LENGTH_SHORT).show();
        final long ONE_MEGABYTE = 1024 * 1024;
        return chck;

    }


    private void uploadFile() {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("업로드중...");
            progressDialog.show();

            //storage
            FirebaseStorage storage = FirebaseStorage.getInstance();

            //현재시간으로 사진 이름을 정해서 넣는 형태.
           /* SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd_hhmm_");
            Date now = new Date();
            String filename = formatter.format(now) +userName+".png";*/

            //storage 주소와 폴더 파일명을 지정해 준다.
            StorageReference storageRef = storage.getReferenceFromUrl("gs://boxstorage-8f972.appspot.com").child("images/" + userName+"/"+userName+"_start.png");
            /*images/admin/admin_start.png*/


            storageRef.putFile(filePath)
                    //성공
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            Toast.makeText(getActivity().getApplicationContext(), "업로드 완료", Toast.LENGTH_SHORT).show();

                            imageView.setImageResource(R.mipmap.ic_launcher);
                        }
                    })
                    //실패
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity().getApplicationContext(), "업로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    })
                    //진행
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests")
                            double progress = (100 * taskSnapshot.getBytesTransferred()) /  taskSnapshot.getTotalByteCount();

                            progressDialog.setMessage("Uploaded " + ((int) progress) + "% ...");
                        }
                    });
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
        }
    }

    //이미지 생성함수
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == RESULT_OK){
            filePath = data.getData();
            Log.d(TAG, "uri:" + String.valueOf(filePath));
            try {
                //Uri 파일을 Bitmap으로 .
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}